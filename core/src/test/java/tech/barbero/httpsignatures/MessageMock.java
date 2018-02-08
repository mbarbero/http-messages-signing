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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class MessageMock implements HttpMessage {

	private final Map<String, List<String>> headers;
	
	MessageMock() {
		this.headers = new HashMap<>();
	}
	
	@Override
	public HttpMessage addHeader(String name, String value) {
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
