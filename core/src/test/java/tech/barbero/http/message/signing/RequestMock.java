/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.net.URI;

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
