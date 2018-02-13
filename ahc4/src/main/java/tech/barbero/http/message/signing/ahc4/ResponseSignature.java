/*******************************************************************************
 * Copyright (c) 2017 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.http.message.signing.ahc4;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import tech.barbero.http.message.signing.HttpMessageSigner;
import tech.barbero.http.message.signing.ahc4.MessageWrapper.Response;

/**
 * ResponseSignature is responsible for adding <code>Signature</code> header to
 * the outgoing responses. The content of the signature is defined by the
 * injected {@link HttpMessageSigner} object.
 * 
 * @since 1.0
 */
public final class ResponseSignature implements HttpResponseInterceptor {

	private final HttpMessageSigner messageSigner;

	/**
	 * Creates a new signing response interceptor.
	 * 
	 * @param messageSigner
	 *            the message signer to be used to create the signature header.
	 */
	public ResponseSignature(HttpMessageSigner messageSigner) {
		this.messageSigner = messageSigner;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		try {
			messageSigner.sign(new Response(response));
		} catch (GeneralSecurityException e) {
			throw new HttpException("Can't sign HTTP response '" + response + "'", e);
		}
	}

}