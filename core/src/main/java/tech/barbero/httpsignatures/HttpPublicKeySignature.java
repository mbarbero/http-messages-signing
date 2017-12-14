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

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.ArrayList;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class HttpPublicKeySignature extends HttpSignature {

	abstract Signature signature();
	
	HttpPublicKeySignature() {
	}

	@Override
	protected String algorithm() {
		switch (signature().getAlgorithm()) {
			case "SHA1withRSA":
				return "rsa-sha1";
			case "SHA256withRSA":
				return "rsa-sha256";
			case "SHA256withECDSA":
				return "ecdsa-sha256";
			default:
				throw new IllegalStateException("Unsupported signature algorithm '"+signature().getAlgorithm()+"'");
		}
	}
	
	@Override
	protected byte[] doSign(byte[] input) throws GeneralSecurityException {
		signature().update(input);
		return signature().sign();
	}
	
	static Builder builder() {
		return new AutoValue_HttpPublicKeySignature.Builder()
				.headersToSign(new ArrayList<String>());
	}
	
	@AutoValue.Builder
	public static abstract class Builder extends AbstractHttpSignatureBuilder<Builder, HttpPublicKeySignature> {		
		abstract Builder signature(Signature signature);
	}
}
