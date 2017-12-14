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
import java.util.ArrayList;

import javax.crypto.Mac;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class HttpSecretKeySignature extends HttpSignature {

	abstract Mac mac();
	
	HttpSecretKeySignature() {
	}

	@Override
	protected String algorithm() {
		switch (mac().getAlgorithm()) {
			case "HmacSHA256":
				return "hmac-sha256";
			default:
				throw new IllegalStateException("Unsupported MAC algorithm '"+mac().getAlgorithm()+"'");
		}
	}
	
	@Override
	protected byte[] doSign(byte[] input) throws GeneralSecurityException {
		mac().update(input);
		return mac().doFinal();
	}
	
	static Builder builder() {
		return new AutoValue_HttpSecretKeySignature.Builder()
				.headersToSign(new ArrayList<String>());
	}
	
	@AutoValue.Builder
	public static abstract class Builder extends AbstractHttpSignatureBuilder<Builder, HttpSecretKeySignature> {
		abstract Builder mac(Mac mac);		
	}
}
