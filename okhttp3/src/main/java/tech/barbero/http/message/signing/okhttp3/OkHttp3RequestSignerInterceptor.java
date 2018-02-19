/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.okhttp3;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.Response;
import tech.barbero.http.message.signing.HttpMessageSigner;

public class OkHttp3RequestSignerInterceptor implements Interceptor {

	private final HttpMessageSigner messageSigner;

	public OkHttp3RequestSignerInterceptor(HttpMessageSigner messageSigner) {
		this.messageSigner = Objects.requireNonNull(messageSigner);
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		OkHttp3RequestWrapper request = OkHttp3RequestWrapper.from(chain.request());
		try {
			messageSigner.sign(request);
			return chain.proceed(request.delegate());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Can't sign HTTP message '" + chain.request() + "'", e);
		}
	}

}
