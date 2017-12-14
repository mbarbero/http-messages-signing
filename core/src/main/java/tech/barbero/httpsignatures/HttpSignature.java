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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Mac;

public abstract class HttpSignature {

	public static final String REQUEST_TARGET = "(request-target)";
	public static final String RESPONSE_STATUS = "(response-status)";
	
	public static final String HEADER_SIGNATURE = "Signature";
	
	static final String PARAM_KEY_ID = "keyId";
	static final String PARAM_ALGORITHM = "algorithm";
	static final String PARAM_HEADERS = "headers";
	static final String PARAM_SIGNATURE = "signature";
	
	static final String HEADER_DATE = "Date";
	
	HttpSignature() {
		// implementation classes cannot exist outside of this package
	}
	
	public static HttpSignature.Builder builder(Mac mac) {
		return HttpSecretKeySignature.builder().mac(mac);
	}
	
	public static HttpSignature.Builder builder(Signature signature) {
		return HttpPublicKeySignature.builder().signature(signature);
	}
	
	public abstract String keyId();
	public abstract List<String> headersToSign();
	
	protected abstract String algorithm();
	
	protected abstract byte[] doSign(byte[] input) throws GeneralSecurityException;
	
	public <R extends Request<R>> R sign(R request) throws GeneralSecurityException {
		if (headersToSign().contains(RESPONSE_STATUS)) {
			throw new IllegalStateException("'"+RESPONSE_STATUS+"' cannot be used when signing a request.");
		}
		return signMessage(request, SigningStringBuilder.signingString(headersToSign(), request));
	}
	
	public <R extends Response<R>> R sign(R response) throws GeneralSecurityException {
		if (headersToSign().contains(REQUEST_TARGET)) {
			throw new IllegalStateException("'"+REQUEST_TARGET+"' cannot be used when signing a response.");
		}
		return signMessage(response, SigningStringBuilder.signingString(headersToSign(), response));
	}
	
	private <M extends Message<M>> M signMessage(M message, String signingString) throws GeneralSecurityException {
		checkHeaders(message);
		M ret = message.addHeader(HEADER_SIGNATURE, param(PARAM_KEY_ID, keyId()))
			.addHeader(HEADER_SIGNATURE, param(PARAM_ALGORITHM, algorithm()));
		
		if (!headersToSign().isEmpty()) {
			ret.addHeader(HEADER_SIGNATURE, param(PARAM_HEADERS, quote(join(headersToSign().iterator(), " ", s -> s.trim().toLowerCase()))));
		}
			
		return ret.addHeader(HEADER_SIGNATURE, param(PARAM_SIGNATURE, signature(signingString)));
	}
	
	private String signature(String signingString) throws GeneralSecurityException {
		return base64(doSign(signingString.getBytes(StandardCharsets.US_ASCII)));
	}

	private void checkHeaders(Message<?> message) {
		if (headersToSign().isEmpty()) {
			if (message.headerValues(HEADER_DATE).isEmpty()) {
				throw new IllegalStateException("A Http message must at least contain a date header to be signed");
			}
		} else {
			List<String> notFound = headersToSign().stream()
					.filter(h -> !REQUEST_TARGET.equals(h))
					.filter(h -> !RESPONSE_STATUS.equals(h))
					.filter(h -> message.headerValues(h).isEmpty())
					.collect(Collectors.toList());
			if (!notFound.isEmpty()) {
				throw new IllegalStateException("The following headers cannot be found in the request: " +
						join(notFound.iterator(), ", ", s -> "'" + s + "'"));
			}
		}
	}
	
	private static String base64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	private static String param(String param, String value) {
		return param + '=' + value;
	}
	
	private static String quote(String str) {
		return '"' + str + '"';
	}
	
	public interface Message<M extends Message<M>> {
		List<String> headerValues(String name);
		M addHeader(String name, String value);
	}
	
	public interface Request<R extends Request<R>> extends Message<R> {
		String method();
		URI uri();
	}
	
	public interface Response<R extends Response<R>> extends Message<R> {
		int statusCode();
	}

	public interface Builder {
		Builder keyId(String keyId);
		Builder addHeaderToSign(String header);
		HttpSignature build();
	}
}
