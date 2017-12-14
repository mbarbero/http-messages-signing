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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpRequest;

import tech.barbero.httpsignatures.HttpSignature.Request;

class AHCRequest<HR extends HttpRequest> implements Request<AHCRequest<HR>> {

	private final HR delegate;
	
	private AHCRequest(HR request) {
		this.delegate = request;
	}
	
	public static <HR extends HttpRequest> AHCRequest<HR> from(HR request) {
		return new AHCRequest<>(request);
	}
	
	public HR delegate() {
		return delegate;
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
	public AHCRequest<HR> addHeader(String name, String value) {
		delegate().addHeader(name, value);
		return this;
	}
}
