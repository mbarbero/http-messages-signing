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

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import tech.barbero.http.message.signing.HttpRequest;
import tech.barbero.http.message.signing.HttpResponse;
import tech.barbero.http.message.signing.MessageFactory;
import tech.barbero.http.message.signing.ahc4.MessageWrapper.Request;
import tech.barbero.http.message.signing.ahc4.MessageWrapper.Response;

public class AHCMessageFactory implements MessageFactory {

	@Override
	public HttpRequest createRequest(String method, URI uri) {
		return new Request(new BasicHttpEntityEnclosingRequest(method, uri.toString()));
	}

	@Override
	public HttpResponse createResponse(int statusCode) {
		return new Response(new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, "Thanks you for all the fish!")));
	}

}
