/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

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
	public void addHeader(String name, String value) {
		headers.computeIfAbsent(normalizeHeaderName(name), k -> new ArrayList<>()).add(value);
	}

	private static String normalizeHeaderName(String name) {
		return name.toLowerCase();
	}

	@Override
	public List<String> headerValues(String name) {
		return headers.getOrDefault(normalizeHeaderName(name), Collections.emptyList());
	}
}
