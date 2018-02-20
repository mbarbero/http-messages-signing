/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.okhttp3;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.barbero.http.message.signing.HashKeyMap;
import tech.barbero.http.message.signing.SignatureHeaderVerifier;
import tech.barbero.http.message.signing.HttpMessageSigner;
import tech.barbero.http.message.signing.HttpMessageSigner.Algorithm;

public class TestOkHttp3RequestSignerInterceptor {

	@Test
	public void testInterceptor() throws IOException {
		HttpMessageSigner httpSignature = HttpMessageSigner.builder()
				.algorithm(Algorithm.RSA_SHA256)
				.keyMap(HashKeyMap.INSTANCE)
				.addHeaderToSign(HttpMessageSigner.REQUEST_TARGET)
				.addHeaderToSign("Date")
				.addHeaderToSign("Content-Length")
				.addHeaderToSign("Digest")
				.keyId("myKeyId").build();
		Request request = new Request.Builder().post(RequestBody.create(MediaType.parse("text/plain"), "Hello Worlds!"))
				.url("http://www.example.com/web/service?foo=bar")
				.addHeader("date", "20160320")
				.addHeader("Content-Length", "18")
				.addHeader("digest", "ab4509qsdhabf236G3==")
				.build();
		SignerInterceptor interceptor = new SignerInterceptor(httpSignature);
		AtomicBoolean pass = new AtomicBoolean(false);
		interceptor.intercept(new Chain() {
			@Override
			public Request request() {
				return request;
			}

			@Override
			public Response proceed(Request request) throws IOException {
				SignatureHeaderVerifier signatureVerifier = SignatureHeaderVerifier.builder().keyMap(HashKeyMap.INSTANCE).build();
				try {
					assertTrue(signatureVerifier.verify(RequestWrapper.from(request)));
					pass.set(true);
				} catch (GeneralSecurityException e) {
					fail(e);
				}
				return null;
			}

			@Override
			public Connection connection() {
				return null;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public Call call() {
				return null;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public int connectTimeoutMillis() {
				return 0;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public Chain withConnectTimeout(int timeout, TimeUnit unit) {
				return null;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public int readTimeoutMillis() {
				return 0;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public Chain withReadTimeout(int timeout, TimeUnit unit) {
				return null;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public int writeTimeoutMillis() {
				return 0;
			}

			// @Override source-incompatible change in OkHttp 3.9.0
			@SuppressWarnings("unused")
			public Chain withWriteTimeout(int timeout, TimeUnit unit) {
				return null;
			}
		});
		// if it fails, then #proceed have not been executed
		assertTrue(pass.get());
	}
}
