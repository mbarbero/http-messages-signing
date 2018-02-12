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
package tech.barbero.http.message.signing;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.crypto.Mac;

import com.google.auto.value.AutoValue;

import tech.barbero.http.message.signing.AutoValue_HttpMessageSigner;

@AutoValue
public abstract class HttpMessageSigner {

	public static final String REQUEST_TARGET = "(request-target)";
	public static final String RESPONSE_STATUS = "(response-status)";
	
	public static final String HEADER_SIGNATURE = "Signature";
	
	static final String PARAM_KEY_ID = "keyId";
	static final String PARAM_ALGORITHM = "algorithm";
	static final String PARAM_HEADERS = "headers";
	static final String PARAM_SIGNATURE = "signature";
	
	static final String HEADER_DATE = "Date";
	
	private final Optional<SecureRandom> secureRandom;
	
	HttpMessageSigner() {
		// implementation classes cannot exist outside of this package
		SecureRandom strongSecureRandom = null;
		try {
			strongSecureRandom = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
		secureRandom = Optional.ofNullable(strongSecureRandom);
	}
	
	public static HttpMessageSigner.Builder builder() {
		return new AutoValue_HttpMessageSigner.Builder()
				.headersToSign(new ArrayList<String>());
	}
	
	abstract String keyId();
	abstract KeyMap keyMap();
	abstract List<String> headersToSign();
	
	abstract Algorithm algorithm();
	abstract Optional<Provider> provider(); 
	abstract SigningStringBuilder signingStringBuilder();

	public <M extends HttpMessage> M sign(M message) throws GeneralSecurityException {
		String signingString = signingStringBuilder().signingString(message);
		message.addHeader(HEADER_SIGNATURE, param(PARAM_KEY_ID, keyId()));
		message.addHeader(HEADER_SIGNATURE, param(PARAM_ALGORITHM, algorithm().algorithmName()));
		
		if (!headersToSign().isEmpty()) {
			String paramHeaders = headersToSign().stream().map(String::trim).map(String::toLowerCase).collect(Collectors.joining(" ", "\"", "\""));
			message.addHeader(HEADER_SIGNATURE, param(PARAM_HEADERS, paramHeaders));
		}
			
		message.addHeader(HEADER_SIGNATURE, param(PARAM_SIGNATURE, sign(signingString)));
		return message;
	}
	
	private static String param(String param, String value) {
		return param + '=' + value;
	}
	
	private String sign(String signingString) throws GeneralSecurityException {
		byte[] signedBytes = sign(signingString.getBytes(StandardCharsets.US_ASCII));
		return Base64.getEncoder().encodeToString(signedBytes);
	}

	private byte[] sign(byte[] input) throws GeneralSecurityException {
		switch (algorithm().type()) {
			case PUBLIC_KEY:
				return signWithPublicKeyAlgorithm(input);
			case SECRET_KEY:
				return signWithSecretKeyAlgorithm(input);
		}
		throw new IllegalStateException("Unknown algorithm type '"+algorithm().type()+"'");
	}
	
	private byte[] signWithSecretKeyAlgorithm(byte[] input) throws NoSuchAlgorithmException, InvalidKeyException {
		final Mac mac;
		if (provider().isPresent()) {
			mac = algorithm().createMac(provider().get());
		} else {
			mac = algorithm().createMac();
		}
		mac.init(keyMap().getSecretKey(keyId()));
		mac.update(input);
		return mac.doFinal();
	}

	private byte[] signWithPublicKeyAlgorithm(byte[] input) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		final Signature signature;
		if (provider().isPresent()) {
			signature = algorithm().createSignature(provider().get());
		} else {
			signature = algorithm().createSignature();
		}
		
		if (secureRandom.isPresent()) {
			signature.initSign(keyMap().getPrivateKey(keyId()), secureRandom.get());
		} else {
			signature.initSign(keyMap().getPrivateKey(keyId()));
		}
		
		signature.update(input);
		return signature.sign();
	}

	public enum Algorithm {
		/* Public-key algorithm */
		RSA_SHA1("rsa-sha1", "SHA1withRSA", Type.PUBLIC_KEY),
		RSA_SHA256("rsa-sha256", "SHA256withRSA", Type.PUBLIC_KEY),
		ECDSA_SHA256("ecdsa-sha256", "SHA256withECDSA", Type.PUBLIC_KEY),
		/* Secret-key algorithm */
		HMAC_SHA256("hmac-sha256", "HmacSHA256", Type.SECRET_KEY);
		
		private static final EnumSet<Algorithm> ALL_OF = EnumSet.allOf(Algorithm.class);
		
		public enum Type {
			PUBLIC_KEY,
			SECRET_KEY;
		}
		
		private final String algorithmName;
		private final String javaAlgorithmName;
		private final Type type;
		 
		private Algorithm(String algorithmName, String javaAlgorithmName, Type type) {
			this.algorithmName = algorithmName;
			this.javaAlgorithmName = javaAlgorithmName;
			this.type = type;
		}
		
		public String algorithmName() {
			return algorithmName;
		}
		
		public Type type() {
			return type;
		}
		
		public Signature createSignature() throws NoSuchAlgorithmException {
			return Signature.getInstance(javaAlgorithmName);
		}
		
		public Signature createSignature(Provider provider) throws NoSuchAlgorithmException {
			return Signature.getInstance(javaAlgorithmName, provider);
		}
		
		public Mac createMac() throws NoSuchAlgorithmException {
			return Mac.getInstance(javaAlgorithmName);
		}
		
		public Mac createMac(Provider provider) throws NoSuchAlgorithmException {
			return Mac.getInstance(javaAlgorithmName, provider);
		}
		
		static Optional<Algorithm> findFirst(Predicate<Algorithm> p) {
			return ALL_OF.stream().filter(p).findFirst();
		}
	}
	
	@AutoValue.Builder
	public static abstract class Builder {

		public abstract Builder keyId(String keyId);
		public abstract Builder keyMap(KeyMap keyMap);
		public abstract Builder algorithm(Algorithm algorithm);
		public abstract Builder provider(Provider provider);
		
		abstract Builder headersToSign(List<String> headers);
		abstract List<String> headersToSign();
		abstract Builder signingStringBuilder(SigningStringBuilder signingStringBuilder);

		public Builder addHeaderToSign(String header) {
			if (!headersToSign().contains(header)) {
				headersToSign().add(Objects.requireNonNull(header));
			}
			return this;
		}

		abstract HttpMessageSigner autoBuild();
		
		public HttpMessageSigner build() {
			signingStringBuilder(SigningStringBuilder.forHeaders(headersToSign()));
			HttpMessageSigner ret = autoBuild();
			if (!headersToSign().isEmpty()) {
				if (headersToSign().stream().noneMatch(h -> HEADER_DATE.equalsIgnoreCase(h))) {
					throw new IllegalStateException("HttpMessageSigner should be configured to sign the '"+HEADER_DATE+"' header");
				}
				
				if (headersToSign().stream().noneMatch(h -> REQUEST_TARGET.equalsIgnoreCase(h))) {
					throw new IllegalStateException("HttpMessageSigner should be configured to sign the '"+REQUEST_TARGET+"' header");
				}
			}
			ret.algorithm(); // check supported algorithm name
			return ret;
		}

	}
}
