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

class OkHttp3RequestWrapper implements HttpRequest {

	private Request delegate;

	private OkHttp3RequestWrapper(Request delegate) {
		this.delegate = delegate;
	}

	static OkHttp3RequestWrapper from(Request request) {
		return new OkHttp3RequestWrapper(Objects.requireNonNull(request));
	}

	Request delegate() {
		return delegate;
	}

	@Override
	public List<String> headerValues(String name) {
		return delegate.headers(name);
	}

	@Override
	public void addHeader(String name, String value) {
		delegate = delegate.newBuilder().addHeader(name, value).build();
	}

	@Override
	public String method() {
		return delegate.method();
	}

	@Override
	public URI uri() {
		return delegate.url().uri();
	}

}
