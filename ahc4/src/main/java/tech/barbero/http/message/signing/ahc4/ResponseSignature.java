/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * ResponseSignature is responsible for adding <code>Signature</code> header to the outgoing responses. The content of
 * the signature is defined by the injected {@link HttpMessageSigner} object.
 *
 * @since 1.0
 */
public final class ResponseSignature implements HttpResponseInterceptor {

	private final HttpMessageSigner messageSigner;

	/**
	 * Creates a new signing response interceptor.
	 *
	 * @param messageSigner
	 *          the message signer to be used to create the signature header.
	 */
	public ResponseSignature(HttpMessageSigner messageSigner) {
		this.messageSigner = messageSigner;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		try {
			this.messageSigner.sign(new Response(response));
		} catch (GeneralSecurityException e) {
			throw new HttpException("Can't sign HTTP response '" + response + "'", e);
		}
	}

}
