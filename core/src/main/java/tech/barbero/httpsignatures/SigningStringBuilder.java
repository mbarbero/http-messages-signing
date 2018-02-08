/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.httpsignatures;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class SigningStringBuilder {

	private final List<String> headersToSign;
	
	private SigningStringBuilder(List<String> headersToSign) {
		this.headersToSign = headersToSign;
	}
	
	static SigningStringBuilder noHeader() {
		return forHeaders(Collections.emptyList());
	}
	
	static SigningStringBuilder forHeaders(List<String> headersToSign) {
		return new SigningStringBuilder(headersToSign);
	}
	
	String signingString(HttpRequest request) {
		return signingStringParts(request, (h) -> {
			if (HttpMessageSigner.REQUEST_TARGET.equals(h)) {
				String query = request.uri().getQuery();
				return signedHeader(HttpMessageSigner.REQUEST_TARGET, request.method().toLowerCase() + ' ' + request.uri().getPath() + (query != null ? "?" + query : ""));
			} else {
				return signedHeader(h, request.headerValues(h));
			}
		}).stream().collect(Collectors.joining("\n"));
	}
	
	String signingString(HttpResponse response) {
		return signingStringParts(response, (h) -> {
			if (HttpMessageSigner.RESPONSE_STATUS.equals(h)) {
				return signedHeader(HttpMessageSigner.RESPONSE_STATUS, Integer.toString(response.statusCode()));
			} else {
				return signedHeader(h, response.headerValues(h));
			}
		}).stream().collect(Collectors.joining("\n"));
	}
	
	private List<String> signingStringParts(HttpMessage message, Function<String, String> headerMapper) {
		checkHeaders(message);
		if (headersToSign.isEmpty()) {
			return Collections.singletonList(signedHeader(HttpMessageSigner.HEADER_DATE, message.headerValues(HttpMessageSigner.HEADER_DATE)));
		} else {
			return headersToSign.stream()
				.map(h -> headerMapper.apply(h))
				.collect(Collectors.toList());
		}
	}
	
	private void checkHeaders(HttpMessage message) {
		if (headersToSign.isEmpty()) {
			if (message.headerValues(HttpMessageSigner.HEADER_DATE).isEmpty()) {
				throw new IllegalStateException("A HTTP message must at least contain a date header to be signed");
			}
		} else {
			List<String> notFound = headersToSign.stream()
					.filter(h -> !HttpMessageSigner.REQUEST_TARGET.equals(h))
					.filter(h -> !HttpMessageSigner.RESPONSE_STATUS.equals(h))
					.filter(h -> message.headerValues(h).isEmpty())
					.collect(Collectors.toList());
			if (!notFound.isEmpty()) {
				throw new IllegalStateException("The following headers cannot be found in the request: " +
						notFound.stream().map(s -> ("'" + s + "'")).collect(Collectors.joining(", ")));
			}
		}
	}
	
	/**
	 * Create the header field string by concatenating the lowercased header field
	 * name followed with an ASCII colon `:`, an ASCII space ` `, and the header
	 * field value. Leading and trailing optional whitespace (OWS) in the header
	 * field value are omitted (as specified in RFC7230 [RFC7230], Section 3.2.4).
	 * 
	 * @param header
	 * @param value
	 * @return
	 */
	private static String signedHeader(String header, String value) {
		return header.toLowerCase().trim() + ": " + value.trim();
	}
	
	/**
	 * If there are multiple instances of the same header field, all header field
	 * values associated with the header field MUST be concatenated, separated by a
	 * ASCII comma and an ASCII space `, `, and used in the order in which they will
	 * appear in the transmitted HTTP message.
	 * 
	 * @param header
	 * @param values
	 * @return
	 */
	private static String signedHeader(String header, List<String> values) {
		return header.toLowerCase().trim() + ": " + values.stream().map(String::trim).collect(Collectors.joining(", "));
	}
}
