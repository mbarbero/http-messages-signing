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

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import tech.barbero.http.message.signing.HttpRequest;
import tech.barbero.http.message.signing.HttpResponse;
import tech.barbero.http.message.signing.MessageFactory;

public class OkHttp3MessageFactory implements MessageFactory {

	@Override
	public HttpRequest createRequest(String method, URI uri) {
		Builder builder = new Request.Builder().url(uri.toString());
		if (method.equalsIgnoreCase("post")) {
			builder.method(method.toUpperCase(), RequestBody.create(MediaType.parse("text/plain"), "Hello World!"));
		} else {
			builder.method(method.toUpperCase(), null);
		}
		return RequestWrapper.from(builder.build());
	}

	@Override
	public HttpResponse createResponse(int statusCode) {
		throw new UnsupportedOperationException();
	}

}
