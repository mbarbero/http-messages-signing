/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.ahc4;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import tech.barbero.http.message.signing.HttpMessage;
import tech.barbero.http.message.signing.HttpRequest;
import tech.barbero.http.message.signing.HttpResponse;

abstract class MessageWrapper<M extends org.apache.http.HttpMessage> implements HttpMessage {

	private final M delegate;

	MessageWrapper(M delegate) {
		this.delegate = delegate;
	}

	// doesn't need to be more visible than package-private as the
	// delegate will get modified in place.
	M delegate() {
		return this.delegate;
	}

	@Override
	public List<String> headerValues(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Argument 'name' must not be null of empty");
		}
		return Arrays.stream(delegate().getHeaders(name))
			.map(h -> h.getValue())
			.collect(Collectors.toList());
	}

	@Override
	public void addHeader(String name, String value) {
		delegate().addHeader(name, value);
	}

	static class Request extends MessageWrapper<org.apache.http.HttpRequest> implements HttpRequest {

		Request(org.apache.http.HttpRequest request) {
			super(request);
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

	static class Response extends MessageWrapper<org.apache.http.HttpResponse> implements HttpResponse {

		Response(org.apache.http.HttpResponse delegate) {
			super(delegate);
		}

		@Override
		public int statusCode() {
			return delegate().getStatusLine().getStatusCode();
		}
	}

}
