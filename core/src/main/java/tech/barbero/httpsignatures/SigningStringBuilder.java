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
package tech.barbero.httpsignatures;

import static tech.barbero.httpsignatures.Utils.join;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tech.barbero.httpsignatures.HttpSignature.Request;
import tech.barbero.httpsignatures.HttpSignature.Response;

abstract class SigningStringBuilder<M extends HttpSignature.Message<M>> {
	
	private final List<String> headersToSign;
	
	private SigningStringBuilder(List<String> headersToSign) {
		this.headersToSign = headersToSign;
	}
	
	static <R extends Request<R>> String signingString(List<String> headersToSign, R request) {
		return new SigningStringBuilder.OfRequest<R>(headersToSign).signingString(request);
		
	}
	
	static <R extends Response<R>> String signingString(List<String> headersToSign, R response) {
		return new SigningStringBuilder.OfResponse<R>(headersToSign).signingString(response);
	}
	
	
	String signingString(M message) {
		return join(signingStringParts(message).iterator(), "\n", s -> s);
	}
	
	protected abstract String mapToSignedHeader(String header, M message);
	
	private List<String> signingStringParts(M message) {
		if (headersToSign.isEmpty()) {
			return Collections.singletonList(signedHeader(HttpSignature.HEADER_DATE, message.headerValues(HttpSignature.HEADER_DATE)));
		} else {
			return headersToSign.stream()
				// remove empty headers (except special values)
				.filter(header -> HttpSignature.REQUEST_TARGET.equals(header) || HttpSignature.RESPONSE_STATUS.equals(header) || !message.headerValues(header).isEmpty())
				.map(h -> mapToSignedHeader(h, message))
				.collect(Collectors.toList());
		}
	}

	static class OfRequest<R extends HttpSignature.Request<R>> extends SigningStringBuilder<R> {
		OfRequest(List<String> headersToSign) {
			super(headersToSign);
		}

		@Override
		protected String mapToSignedHeader(String header, R request) {
			if (HttpSignature.REQUEST_TARGET.equals(header)) {
				String query = request.uri().getQuery();
				return signedHeader(HttpSignature.REQUEST_TARGET, request.method().toLowerCase() + ' ' + request.uri().getPath() + (query != null ? "?" + query : ""));
			} else {
				return signedHeader(header, request.headerValues(header));
			}
		}
	}
	
	static class OfResponse<R extends HttpSignature.Response<R>> extends SigningStringBuilder<R> {
		OfResponse(List<String> headersToSign) {
			super(headersToSign);
		}

		@Override
		protected String mapToSignedHeader(String header, R response) {
			if (HttpSignature.RESPONSE_STATUS.equals(header)) {
				return signedHeader(HttpSignature.RESPONSE_STATUS, Integer.toString(response.statusCode()));
			} else {
				return signedHeader(header, response.headerValues(header));
			}
		}
	}
	
	/**
	 * Create the header field string by concatenating the lowercased header field
	 * name followed with an ASCII colon `:`, an ASCII space ` `, and the header
	 * field value. Leading and trailing optional whitespace (OWS) in the header
	 * field value MUST be omitted (as specified in RFC7230 [RFC7230], Section 3.2.4
	 * [7]).
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
		return header.toLowerCase().trim() + ": " + join(values.iterator(), ", ", s -> s.trim());
	}
	
}