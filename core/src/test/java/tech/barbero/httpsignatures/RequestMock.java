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
package tech.barbero.httpsignatures;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.barbero.httpsignatures.HttpSignature.Request;

public class RequestMock implements Request<RequestMock> {

	private final String method;
	private final URI uri;
	private final Map<String, List<String>> headers;

	public RequestMock(String method, URI uri) {
		this.method = method;
		this.uri = uri;
		this.headers = new HashMap<>();
	}
	
	@Override
	public String method() {
		return method;
	}

	@Override
	public URI uri() {
		return uri;
	}

	@Override
	public RequestMock addHeader(String name, String value) {
		headers.computeIfAbsent(normalizeHeaderName(name), k -> new ArrayList<>()).add(value);	
		return this;
	}
	
	private static String normalizeHeaderName(String name) {
		return name.toLowerCase();
	}

	@Override
	public List<String> headerValues(String name) {
		return headers.getOrDefault(normalizeHeaderName(name), Collections.emptyList());
	}

}
