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
		return OkHttp3RequestWrapper.from(builder.build());
	}

	@Override
	public HttpResponse createResponse(int statusCode) {
		throw new UnsupportedOperationException();
	}

}
