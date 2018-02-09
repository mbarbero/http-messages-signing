/*******************************************************************************
 * Copyright (c) 2017 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikaël Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.net.URI;

import tech.barbero.http.message.signing.HttpRequest;

class RequestMock extends MessageMock implements HttpRequest {

	private final String method;
	private final URI uri;

	RequestMock(String method, URI uri) {
		this.method = method;
		this.uri = uri;
	}
	
	@Override
	public String method() {
		return method;
	}

	@Override
	public URI uri() {
		return uri;
	}
}
