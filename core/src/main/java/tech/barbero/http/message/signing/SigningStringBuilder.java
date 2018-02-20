/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.util.Collections;
import java.util.List;
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

	String signingString(HttpMessage message) {
		return signingStringParts(message).stream().collect(Collectors.joining("\n"));
	}

	private static String headerSigningString(HttpMessage message, String header) {
		final String ret;
		if (HttpMessageSigner.REQUEST_TARGET.equals(header)) {
			if (!(message instanceof HttpRequest)) {
				throw new IllegalStateException("Header '" + HttpMessageSigner.REQUEST_TARGET + "' can only be used with HTTP Request.");
			}
			ret = requestTargetHeaderSigningString((HttpRequest) message);
		} else if (HttpMessageSigner.RESPONSE_STATUS.equals(header)) {
			if (!(message instanceof HttpResponse)) {
				throw new IllegalStateException("Header '" + HttpMessageSigner.RESPONSE_STATUS + "' can only be used with HTTP Response.");
			}
			ret = responseStatusHeaderSigningString((HttpResponse) message);
		} else {
			ret = headerSigningString(header, message.headerValues(header));
		}
		return ret;
	}

	private List<String> signingStringParts(HttpMessage message) {
		checkHeaders(message);
		final List<String> ret;
		if (this.headersToSign.isEmpty()) {
			ret = Collections.singletonList(headerSigningString(HttpMessageSigner.HEADER_DATE, message.headerValues(HttpMessageSigner.HEADER_DATE)));
		} else {
			ret = this.headersToSign.stream()
					.map(h -> headerSigningString(message, h))
					.collect(Collectors.toList());
		}
		return ret;
	}

	private void checkHeaders(HttpMessage message) {
		if (this.headersToSign.isEmpty()) {
			if (message.headerValues(HttpMessageSigner.HEADER_DATE).isEmpty()) {
				throw new IllegalStateException("A HTTP message must contain at least a date header to be signed");
			}
		} else {
			List<String> notFound = this.headersToSign.stream()
					.filter(h -> !HttpMessageSigner.REQUEST_TARGET.equals(h))
					.filter(h -> !HttpMessageSigner.RESPONSE_STATUS.equals(h))
					.filter(h -> message.headerValues(h).isEmpty())
					.collect(Collectors.toList());
			if (!notFound.isEmpty()) {
				throw new IllegalStateException("The following headers cannot be found in the message: "
						+ notFound.stream().map(s -> ("'" + s + "'")).collect(Collectors.joining(", ")));
			}
		}
	}

	private static String responseStatusHeaderSigningString(HttpResponse response) {
		return headerSigningString(HttpMessageSigner.RESPONSE_STATUS, Integer.toString(response.statusCode()));
	}

	private static String requestTargetHeaderSigningString(HttpRequest request) {
		String query = request.uri().getQuery();
		return headerSigningString(HttpMessageSigner.REQUEST_TARGET, request.method().toLowerCase() + ' ' + request.uri().getPath() + (query != null ? "?" + query : ""));
	}

	/**
	 * Create the header field string by concatenating the lowercased header field name followed with an ASCII colon `:`, an ASCII space ` `, and the header field value.
	 * Leading and trailing optional whitespace (OWS) in the header field value are omitted (as specified in RFC7230 [RFC7230], Section 3.2.4).
	 *
	 * @param header
	 *          the name of the header.
	 * @param value
	 *          the value of the header.
	 * @return the header field string properly formatted.
	 */
	private static String headerSigningString(String header, String value) {
		return header.toLowerCase().trim() + ": " + value.trim();
	}

	/**
	 * If there are multiple instances of the same header field, all header field values associated with the header field MUST be concatenated, separated by a ASCII comma
	 * and an ASCII space `, `, and used in the order in which they will appear in the transmitted HTTP message.
	 *
	 * @param header
	 *          the name of the header.
	 * @param values
	 *          the values of the header.
	 * @return the header field string properly formatted.
	 */
	private static String headerSigningString(String header, List<String> values) {
		return header.toLowerCase().trim() + ": " + values.stream().map(String::trim).collect(Collectors.joining(", "));
	}
}
