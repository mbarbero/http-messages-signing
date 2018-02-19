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

public interface MessageFactory {

	HttpRequest createRequest(String method, URI uri);

	HttpResponse createResponse(int statusCode);

	class MockImpl implements MessageFactory {

		@Override
		public HttpRequest createRequest(String method, URI uri) {
			return new RequestMock(method, uri);
		}

		@Override
		public HttpResponse createResponse(int statusCode) {
			return new ResponseMock(statusCode);
		}

	}
}
