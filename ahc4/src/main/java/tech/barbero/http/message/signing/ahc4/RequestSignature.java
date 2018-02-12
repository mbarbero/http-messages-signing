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
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import tech.barbero.http.message.signing.HttpMessageSigner;
import tech.barbero.http.message.signing.ahc4.MessageWrapper.Request;

/**
 * RequestSignature interceptor is responsible for adding <code>Signature</code>
 * header to the outgoing requests. The content of the signature is defined by
 * the injected {@link HttpMessageSigner} object.
 * 
 * @since 1.0
 */
public final class RequestSignature implements HttpRequestInterceptor {

	private final HttpMessageSigner messageSigner;

	/**
	 * Creates a new signing request interceptor.
	 * 
	 * @param messageSigner
	 *            the message signer to be used to create the signature header.
	 */
	public RequestSignature(HttpMessageSigner messageSigner) {
		this.messageSigner = messageSigner;
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		try {
			messageSigner.sign(new Request(request));
		} catch (GeneralSecurityException e) {
			throw new HttpException("Can't sign HTTP request '" + request + "'", e);
		}
	}

}
