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

import tech.barbero.httpsignatures.HttpResponse;

public class AHCResponse extends AHCMessage implements HttpResponse {

private final org.apache.http.HttpResponse delegate;
	
	private AHCResponse(org.apache.http.HttpResponse request) {
		this.delegate = request;
	}

	@Override
	public org.apache.http.HttpResponse delegate() {
		return delegate;
	}

	public static AHCResponse from(org.apache.http.HttpResponse request) {
		if (request instanceof AHCResponse) {
			return (AHCResponse) request;
		}
		return new AHCResponse(request);
	}

	@Override
	public AHCResponse addHeader(String name, String value) {
		super.addHeader(name, value);
		return this;
	}
	
	@Override
	public int statusCode() {
		return delegate().getStatusLine().getStatusCode();
	}


}
