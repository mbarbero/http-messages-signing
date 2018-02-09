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

import tech.barbero.http.message.signing.AutoValue_HttpMessageSignatureVerifier;

@AutoValue
public abstract class HttpMessageSignatureVerifier {
	
	HttpMessageSignatureVerifier() {}
	abstract KeyMap keyMap();
	abstract Optional<Provider> securityProvider();
	
	public boolean verify(HttpMessage signedMessage) throws HttpMessageSignatureVerificationException, GeneralSecurityException {
		try {
			HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValuesList(signedMessage.headerValues(HttpMessageSigner.HEADER_SIGNATURE));
			String signingString = SigningStringBuilder.forHeaders(signatureHeader.signedHeaders()).signingString(signedMessage);
			switch (signatureHeader.algorithm().type()) {
				case PUBLIC_KEY:
					return verifyPublicKey(signingString, signatureHeader);
				case SECRET_KEY:
					return verifySecretKey(signingString, signatureHeader);
				default:
					throw new HttpMessageSignatureVerificationException("Unknown HTTP message signature algorithm type '" + signatureHeader.algorithm( )+ ":" + signatureHeader.algorithm().type() + "'");
			}
		} catch (Exception e) {
			Throwables.throwIfInstanceOf(e, GeneralSecurityException.class);
			throw new HttpMessageSignatureVerificationException("Unable to verify request '"+signedMessage.toString()+"'", e);
		}
	}
	
	private boolean verifySecretKey(String signingString, HttpMessageSignatureHeaderElements signatureHeader) throws GeneralSecurityException {
		Mac mac = createMac(signatureHeader);
		mac.init(keyMap().getSecretKey(signatureHeader.keyId()));
		mac.update(signingString.getBytes(StandardCharsets.US_ASCII));
		return Arrays.equals(mac.doFinal(), Base64.getDecoder().decode(signatureHeader.signature()));
	}
	
	private Mac createMac(HttpMessageSignatureHeaderElements signatureHeader) throws NoSuchAlgorithmException {
		if (securityProvider().isPresent()) {
			return signatureHeader.algorithm().createMac(securityProvider().get());
		} else {
			return signatureHeader.algorithm().createMac();
		}
	}

	private boolean verifyPublicKey(String signingString, HttpMessageSignatureHeaderElements signatureHeader) throws GeneralSecurityException {
		Signature jSignature = createSignature(signatureHeader);
		jSignature.initVerify(keyMap().getPublicKey(signatureHeader.keyId()));
		jSignature.update(signingString.getBytes(StandardCharsets.US_ASCII));
		return jSignature.verify(Base64.getDecoder().decode(signatureHeader.signature()));
	}
	
	private Signature createSignature(HttpMessageSignatureHeaderElements signatureHeader) throws NoSuchAlgorithmException {
		if (securityProvider().isPresent()) {
			return signatureHeader.algorithm().createSignature(securityProvider().get());
		} else {
			return signatureHeader.algorithm().createSignature();
		}
	}
	
	public static Builder builder() {
		return new AutoValue_HttpMessageSignatureVerifier.Builder();
	}
	
	@AutoValue.Builder
	public static abstract class Builder {
		Builder() {}
		public abstract Builder keyMap(KeyMap keyMap);
		public abstract Builder securityProvider(Provider provider);
		public abstract HttpMessageSignatureVerifier build();
	}
}
