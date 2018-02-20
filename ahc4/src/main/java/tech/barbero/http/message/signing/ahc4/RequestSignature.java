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
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import tech.barbero.http.message.signing.HttpMessageSigner;
import tech.barbero.http.message.signing.ahc4.MessageWrapper.Request;

/**
 * RequestSignature interceptor is responsible for adding <code>Signature</code> header to the outgoing requests. The
 * content of the signature is defined by the injected {@link HttpMessageSigner} object.
 *
 * @since 1.0
 */
public final class RequestSignature implements HttpRequestInterceptor {

	private final HttpMessageSigner messageSigner;

	/**
	 * Creates a new signing request interceptor.
	 *
	 * @param messageSigner
	 *          the message signer to be used to create the signature header.
	 */
	public RequestSignature(HttpMessageSigner messageSigner) {
		this.messageSigner = messageSigner;
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		try {
			this.messageSigner.sign(new Request(request));
		} catch (GeneralSecurityException e) {
			throw new HttpException("Can't sign HTTP request '" + request + "'", e);
		}
	}

}
