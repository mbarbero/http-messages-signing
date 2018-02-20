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
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;

import com.google.auto.value.AutoValue;
import com.google.common.base.Throwables;

/**
 * An utility class to verify the {@code Signature} header of HTTP messages. *
 * <p>
 * This class is immutable and thread-safe. Once configured by its builder, it can be reused as many times as desired.
 */
@AutoValue
public abstract class SignatureHeaderVerifier {

	SignatureHeaderVerifier() {
	}

	abstract KeyMap keyMap();

	abstract Optional<Provider> securityProvider();

	/**
	 * Verify the {@code Signature} header from the given HTTP message.
	 *
	 * @param message
	 *          the message to verify.
	 * @return true if the {@code Signature} header exists in the message and is verified, false otherwise.
	 * @throws GeneralSecurityException
	 *           when the underlying Java Cryptography Architecture fails to verify the signature.
	 */
	public boolean verify(HttpMessage message) throws GeneralSecurityException {
		try {
			SignatureHeaderElements signatureHeader = SignatureHeaderElements
					.fromHeaderValuesList(message.headerValues(HttpMessageSigner.HEADER_SIGNATURE));
			String signingString = SigningStringBuilder.forHeaders(signatureHeader.signedHeaders()).signingString(message);
			switch (signatureHeader.algorithm().type()) {
				case PUBLIC_KEY:
					return verifyPublicKey(signingString, signatureHeader);
				case SECRET_KEY:
					return verifySecretKey(signingString, signatureHeader);
			}
			throw new GeneralSecurityException("Unknown HTTP message signature algorithm type '"
					+ signatureHeader.algorithm() + ":"
					+ signatureHeader.algorithm().type() + "'");
		} catch (Exception e) {
			Throwables.throwIfInstanceOf(e, GeneralSecurityException.class);
			throw new GeneralSecurityException("Unable to verify message '" + message.toString() + "'", e);
		}
	}

	private boolean verifySecretKey(String signingString, SignatureHeaderElements signatureHeader) throws GeneralSecurityException {
		Mac mac = createMac(signatureHeader);
		mac.init(keyMap().getSecretKey(signatureHeader.keyId()));
		mac.update(signingString.getBytes(StandardCharsets.US_ASCII));
		return Arrays.equals(mac.doFinal(), Base64.getDecoder().decode(signatureHeader.signature()));
	}

	private Mac createMac(SignatureHeaderElements signatureHeader) throws NoSuchAlgorithmException {
		final Mac ret;
		if (securityProvider().isPresent()) {
			ret = signatureHeader.algorithm().createMac(securityProvider().get());
		} else {
			ret = signatureHeader.algorithm().createMac();
		}
		return ret;
	}

	private boolean verifyPublicKey(String signingString, SignatureHeaderElements signatureHeader) throws GeneralSecurityException {
		Signature jSignature = createSignature(signatureHeader);
		jSignature.initVerify(keyMap().getPublicKey(signatureHeader.keyId()));
		jSignature.update(signingString.getBytes(StandardCharsets.US_ASCII));
		return jSignature.verify(Base64.getDecoder().decode(signatureHeader.signature()));
	}

	private Signature createSignature(SignatureHeaderElements signatureHeader) throws NoSuchAlgorithmException {
		final Signature ret;
		if (securityProvider().isPresent()) {
			ret = signatureHeader.algorithm().createSignature(securityProvider().get());
		} else {
			ret = signatureHeader.algorithm().createSignature();
		}
		return ret;
	}

	/**
	 * Returns a new {@link SignatureHeaderVerifier} builder.
	 *
	 * @return a new {@link SignatureHeaderVerifier} builder.
	 */
	public static Builder builder() {
		return new AutoValue_SignatureHeaderVerifier.Builder();
	}

	/**
	 * A {@link SignatureHeaderVerifier} builder.
	 */
	@AutoValue.Builder
	public abstract static class Builder {
		Builder() {
		}

		/**
		 * The key map to be used to find the public/secret key associated with the {@code keyId} in the message.
		 *
		 * @param keyMap
		 *          The key map to be used to find the public/secret key associated with the {@code keyId} in the message.
		 *
		 * @return this builder for daisy chain.
		 */
		public abstract Builder keyMap(KeyMap keyMap);

		/**
		 * The optional Java Security Provider to be used to find the implementation of the cryptographic algorithms.
		 *
		 * @param provider
		 *          The Java Security Provider to be used to find the implementation of the cryptographic algorithms.
		 * @return this builder for daisy chain.
		 */
		public abstract Builder securityProvider(Provider provider);

		/**
		 * Returns a newly configured {@code SignatureHeaderVerifier}.
		 *
		 * @return a newly configured {@code SignatureHeaderVerifier}.
		 */
		public abstract SignatureHeaderVerifier build();
	}
}
