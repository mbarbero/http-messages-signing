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
package tech.barbero.httpsignatures.ahc4;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.RequestContent;
import org.junit.BeforeClass;
import org.junit.Test;

import tech.barbero.httpsignatures.HttpSignature;

public class TestAHC {

	private final class RequestFixedDate implements HttpRequestInterceptor {
		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			request.setHeader(HTTP.DATE_HEADER, oneInstant.toString());
		}
	}

	private final class RequestDigest implements HttpRequestInterceptor {
		private final MessageDigest md;

		private RequestDigest(MessageDigest md) {
			this.md = md;
		}

		@Override
		public void process(HttpRequest request, HttpContext context) throws IOException {
			if (request instanceof HttpEntityEnclosingRequest) {
				try(InputStream is = ((HttpEntityEnclosingRequest) request).getEntity().getContent()) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) != -1) {
						md.update(buffer, 0, length);
					}
					byte[] digest = md.digest();
					request.addHeader("Digest", md.getAlgorithm() + "=" + Base64.getEncoder().encodeToString(digest));
				}
			}
		}
	}

	private static KeyPair keyPair;
	
	private static SecureRandom csprng;
	
	private static Instant oneInstant;
	
	@BeforeClass
	public static void beforeClass() throws NoSuchAlgorithmException {
		oneInstant = LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1));
		csprng = SecureRandom.getInstance("NativePRNG");
		keyPair = createKeyPair();
	}
	
	private static KeyPair createKeyPair() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024, csprng);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Signature getSignature(String algorithm, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException {
		Signature signature = Signature.getInstance(algorithm);
		signature.initSign(privateKey);
		return signature;
	}

	@Test
	public void testInterceptor() throws GeneralSecurityException, HttpException, IOException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature)
				.addHeaderToSign(HttpSignature.REQUEST_TARGET)
				.addHeaderToSign("Date")
				.addHeaderToSign("Content-Length")
				.addHeaderToSign("Digest")
				.keyId("MyId").build();
		HttpProcessor httpProcessor = createProcessor(httpSignature);
		
		HttpContext context = new BasicHttpContext();
		HttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("GET", "/");
		request.setEntity(new StringEntity("Hello World!"));
		httpProcessor.process(request, context);
		
		Header[] signatureHeaders = request.getHeaders(HttpSignature.HEADER_SIGNATURE);
		assertEquals("keyId=MyId", signatureHeaders[0].getValue());
		assertEquals("algorithm=rsa-sha256", signatureHeaders[1].getValue());
		assertEquals("headers=\"(request-target) date content-length digest\"", signatureHeaders[2].getValue());

		String signingString = "(request-target): get /\ndate: "+oneInstant.toString()+"\ncontent-length: 12\ndigest: SHA-256=f4OxZX/x/FO5LcGBSKHWXfwtSx+j1ncoSt3SABJtkGk=";
		signature.update(signingString.getBytes());
		assertEquals("signature="+Base64.getEncoder().encodeToString(signature.sign()), signatureHeaders[3].getValue());
	}

	private HttpProcessor createProcessor(HttpSignature httpSignature) throws NoSuchAlgorithmException, InvalidKeyException {
		BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
		httpProcessor.addInterceptor(new RequestContent());
		httpProcessor.addInterceptor(new RequestFixedDate());
		httpProcessor.addInterceptor(new RequestDigest(MessageDigest.getInstance("SHA-256")));
		httpProcessor.addInterceptor(new HttpRequestSignatureInterceptor(httpSignature));
		return httpProcessor;
	}
}
