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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import tech.barbero.httpsignatures.HttpMessage;

public abstract class AHCMessage implements HttpMessage {

	public abstract org.apache.http.HttpMessage delegate();

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
	public AHCMessage addHeader(String name, String value) {
		delegate().addHeader(name, value);
		return this;
	}

}