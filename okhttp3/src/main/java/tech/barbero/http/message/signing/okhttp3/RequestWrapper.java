/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.okhttp3;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import okhttp3.Request;
import tech.barbero.http.message.signing.HttpRequest;

class RequestWrapper implements HttpRequest {

	private Request delegate;

	private RequestWrapper(Request delegate) {
		this.delegate = delegate;
	}

	static RequestWrapper from(Request request) {
		return new RequestWrapper(Objects.requireNonNull(request));
	}

	Request delegate() {
		return this.delegate;
	}

	@Override
	public List<String> headerValues(String name) {
		return this.delegate.headers(name);
	}

	@Override
	public void addHeader(String name, String value) {
		this.delegate = this.delegate.newBuilder().addHeader(name, value).build();
	}

	@Override
	public String method() {
		return this.delegate.method();
	}

	@Override
	public URI uri() {
		return this.delegate.url().uri();
	}

}
