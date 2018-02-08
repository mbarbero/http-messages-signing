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

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import tech.barbero.httpsignatures.HttpRequest;
import tech.barbero.httpsignatures.HttpResponse;
import tech.barbero.httpsignatures.MessageFactory;

public class AHCMessageFactory implements MessageFactory {

	@Override
	public HttpRequest createRequest(String method, URI uri) {
		return AHCRequest.from(new BasicHttpEntityEnclosingRequest(method, uri.toString()));
	}

	@Override
	public HttpResponse createResponse(int statusCode) {
		return AHCResponse.from(new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, "Thanks you for all the fish!")));
	}

}
