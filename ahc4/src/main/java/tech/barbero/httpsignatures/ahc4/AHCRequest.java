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

import java.net.URI;
import java.net.URISyntaxException;

import tech.barbero.httpsignatures.HttpRequest;

public class AHCRequest extends AHCMessage implements HttpRequest {

	private final org.apache.http.HttpRequest delegate;
	
	private AHCRequest(org.apache.http.HttpRequest request) {
		this.delegate = request;
	}

	@Override
	public org.apache.http.HttpRequest delegate() {
		return delegate;
	}

	public static AHCRequest from(org.apache.http.HttpRequest request) {
		if (request instanceof AHCRequest) {
			return (AHCRequest) request;
		}
		return new AHCRequest(request);
	}

	@Override
	public AHCRequest addHeader(String name, String value) {
		super.addHeader(name, value);
		return this;
	}
	
	@Override
	public String method() {
		return delegate().getRequestLine().getMethod();
	}

	@Override
	public URI uri() {
		try {
			return new URI(delegate().getRequestLine().getUri());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


}
