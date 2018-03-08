/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Mac;

import com.google.auto.value.AutoValue;

/**
 * A utility class to add a Signature header to an {@link HttpMessage}.
 * <p>
 * This class is immutable and thread-safe. Once configured by its builder, it can be reused as many times as desired.
 */
@AutoValue
public abstract class HttpMessageSigner {

	/**
	 * Special header name for requesting to sign the target of a request.
	 */
	public static final String REQUEST_TARGET = "(request-target)";

	/**
	 * Special header name for requesting to sign the status code of a response.
	 */
	public static final String RESPONSE_STATUS = "(response-status)";

	/**
	 * The string value of the {@code Signature} header.
	 */
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
		} catch (@SuppressWarnings("unused") NoSuchAlgorithmException e) {
			// we handle the fact that secureRandom can be null.
		}
		this.secureRandom = Optional.ofNullable(strongSecureRandom);
	}

	/**
	 * Returns a new builder of {@code HttpMessageSigner}.
	 *
	 * @return a new builder of {@code HttpMessageSigner}.
	 */
	public static HttpMessageSigner.Builder builder() {
		return new AutoValue_HttpMessageSigner.Builder()
				.headersToSign(new ArrayList<String>());
	}

	abstract String keyId();

	abstract KeyMap keyMap();

	abstract List<String> headersToSign();

	abstract Algorithm algorithm();

	abstract Optional<Provider> securityProvider();

	abstract SigningStringBuilder signingStringBuilder();

	/**
	 * Sign (i.e. add a {@code Signature header} and returns the given HTTP message. It uses the algorithm, headers and key
	 * as specified to the builder used to create this object.
	 * <p>
	 * This implementation modifies the given message. It returns it for convenience.
	 *
	 * @param message
	 *          the HTTP message to be signed
	 * @return the message given in parameter with a new {@code Signature} header.
	 * @throws GeneralSecurityException
	 *           when the requested cryptographic algorithm is not available in the environment, or if the key retrieved
	 *           from the {@link KeyMap} is inappropriate for the requested cryptographic algorithm.
	 */
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
		throw new IllegalStateException("Unknown algorithm type '" + algorithm().type() + "'");
	}

	private byte[] signWithSecretKeyAlgorithm(byte[] input) throws NoSuchAlgorithmException, InvalidKeyException {
		final Mac mac;
		if (securityProvider().isPresent()) {
			mac = algorithm().createMac(securityProvider().get());
		} else {
			mac = algorithm().createMac();
		}
		mac.init(keyMap().getSecretKey(keyId()));
		mac.update(input);
		return mac.doFinal();
	}

	private byte[] signWithPublicKeyAlgorithm(byte[] input) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		final Signature signature;
		if (securityProvider().isPresent()) {
			signature = algorithm().createSignature(securityProvider().get());
		} else {
			signature = algorithm().createSignature();
		}

		if (this.secureRandom.isPresent()) {
			signature.initSign(keyMap().getPrivateKey(keyId()), this.secureRandom.get());
		} else {
			signature.initSign(keyMap().getPrivateKey(keyId()));
		}

		signature.update(input);
		return signature.sign();
	}

	/**
	 * Cryptographic algorithms which can be used for signing an HTTP message.
	 */
	public enum Algorithm {
		/* Public-key algorithm */
		/**
		 * RSA public key algorithm with SHA-1 digest algorithm.
		 */
		RSA_SHA1("rsa-sha1", "SHA1withRSA", Type.PUBLIC_KEY),
		/**
		 * RSA public key algorithm with SHA-256 digest algorithm.
		 */
		RSA_SHA256("rsa-sha256", "SHA256withRSA", Type.PUBLIC_KEY),
		/**
		 * ECDSA public key algorithm with SHA-256 digest algorithm.
		 */
		ECDSA_SHA256("ecdsa-sha256", "SHA256withECDSA", Type.PUBLIC_KEY),
		/* Secret-key algorithm */
		/**
		 * Hmac secret key algorithm with SHA-256 hash function.
		 */
		HMAC_SHA256("hmac-sha256", "HmacSHA256", Type.SECRET_KEY);

		enum Type {
			PUBLIC_KEY,
			SECRET_KEY;
		}

		private final String algorithmName;
		private final String javaAlgorithmName;
		private final Type type;

		Algorithm(String algorithmName, String javaAlgorithmName, Type type) {
			this.algorithmName = algorithmName;
			this.javaAlgorithmName = javaAlgorithmName;
			this.type = type;
		}

		String algorithmName() {
			return this.algorithmName;
		}

		Type type() {
			return this.type;
		}

		Signature createSignature() throws NoSuchAlgorithmException {
			return Signature.getInstance(this.javaAlgorithmName);
		}

		Signature createSignature(Provider provider) throws NoSuchAlgorithmException {
			return Signature.getInstance(this.javaAlgorithmName, provider);
		}

		Mac createMac() throws NoSuchAlgorithmException {
			return Mac.getInstance(this.javaAlgorithmName);
		}

		Mac createMac(Provider provider) throws NoSuchAlgorithmException {
			return Mac.getInstance(this.javaAlgorithmName, provider);
		}
	}

	/**
	 * A builder of {@code HttpMeesageSigner}.
	 */
	@AutoValue.Builder
	public abstract static class Builder {

		/**
		 * The value of the keyId to be specified in the {@code Signature header}.
		 *
		 * @param keyId
		 *          The value of the keyId to be specified in the {@code Signature header}.
		 * @return this builder for daisy chain.
		 */
		public abstract Builder keyId(String keyId);

		/**
		 * The key map to be used to find the public/secret key associated with the {@code keyId}.
		 *
		 * @param keyMap
		 *          The key map to be used to find the public/secret key associated with the {@code keyId}.
		 *
		 * @return this builder for daisy chain.
		 */
		public abstract Builder keyMap(KeyMap keyMap);

		/**
		 * The algorithm to be used to sign the HTTP messages.
		 *
		 * @param algorithm
		 *          The algorithm to be used to sign the HTTP messages.
		 * @return this builder for daisy chain.
		 */
		public abstract Builder algorithm(Algorithm algorithm);

		/**
		 * The optional Java Security Provider to be used to find the implementation of the cryptographic algorithms.
		 *
		 * @param provider
		 *          The Java Security Provider to be used to find the implementation of the cryptographic algorithms.
		 * @return this builder for daisy chain.
		 */
		public abstract Builder securityProvider(Provider provider);

		abstract Builder headersToSign(List<String> headers);

		abstract List<String> headersToSign();

		abstract Builder signingStringBuilder(SigningStringBuilder signingStringBuilder);

		/**
		 * Adds the given header to the list of header to take into account while creating the signature of the HTTP message.
		 * Headers will be signed in the order they have been added to this builder.
		 *
		 * @param header
		 *          The header name to be added to the list of headers to be signed.
		 * @return this builder for daisy chain.
		 */
		public Builder addHeaderToSign(String header) {
			if (!headersToSign().contains(header)) {
				headersToSign().add(Objects.requireNonNull(header));
			}
			return this;
		}

		abstract HttpMessageSigner autoBuild();

		/**
		 * Returns a newly configured {@code HttpMessageSigner}.
		 *
		 * @return a newly configured {@code HttpMessageSigner}.
		 */
		public HttpMessageSigner build() {
			signingStringBuilder(SigningStringBuilder.forHeaders(headersToSign()));
			HttpMessageSigner ret = autoBuild();
			if (!headersToSign().isEmpty()) {
				if (headersToSign().stream().noneMatch(HEADER_DATE::equalsIgnoreCase)) {
					throw new IllegalStateException("HttpMessageSigner should be configured to sign the '" + HEADER_DATE + "' header");
				}

				if (headersToSign().stream().noneMatch(REQUEST_TARGET::equalsIgnoreCase)) {
					throw new IllegalStateException("HttpMessageSigner should be configured to sign the '" + REQUEST_TARGET + "' header");
				}
			}
			return ret;
		}

	}
}
