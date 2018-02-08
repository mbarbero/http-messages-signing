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
package tech.barbero.httpsignatures.ahc4;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import tech.barbero.httpsignatures.HttpMessageSigner;

public final class HttpRequestSignerInterceptor implements HttpRequestInterceptor {

	private final HttpMessageSigner httpSignature;

	public HttpRequestSignerInterceptor(HttpMessageSigner httpSignature) {
		this.httpSignature = httpSignature;
	}
	
	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		try {
			httpSignature.sign(AHCRequest.from(request));
		} catch (GeneralSecurityException e) {
			throw new HttpException("Can't sign HTTP message '" + request + "'", e);
		}
	}

}
