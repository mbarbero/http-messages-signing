/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestHttpMessageVerifier {

	protected MessageFactory messageFactory;

	@BeforeEach
	public void beforeAll() {
		messageFactory = createFactory();
	}

	protected MessageFactory createFactory() {
		return new MessageFactory.MockImpl();
	}

	@Test
	public void defaultRFCTest() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(rfcData.keyMap()).build();
		HttpRequest request = rfcData.request();
		request.addHeader(HttpMessageSigner.HEADER_SIGNATURE, RFCData.SIGNATURE_HEADER_VALUE__DEFAULT_TEST);
		assertTrue(signatureVerifier.verify(request));
	}

	@Test
	public void basicRFCTest() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(rfcData.keyMap()).build();
		HttpRequest request = rfcData.request();
		request.addHeader(HttpMessageSigner.HEADER_SIGNATURE, RFCData.SIGNATURE_HEADER_VALUE__BASIC_TEST);
		assertTrue(signatureVerifier.verify(request));
	}

	@Test
	public void allHeadersRFCTest() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(rfcData.keyMap()).build();
		HttpRequest request = rfcData.request();
		request.addHeader(HttpMessageSigner.HEADER_SIGNATURE, RFCData.SIGNATURE_HEADER_VALUE__ALL_HEADERS_TEST);
		assertTrue(signatureVerifier.verify(request));
	}

	@Test
	public void failingSignature() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(rfcData.keyMap()).build();
		HttpRequest request = rfcData.request();
		request.addHeader(HttpMessageSigner.HEADER_SIGNATURE,
				RFCData.SIGNATURE_HEADER_VALUE__ALL_HEADERS_TEST.replaceFirst("signature=\"(.*)\"", "signature=\"" + toB64(randomByteArray(128)) + "\""));
		assertFalse(signatureVerifier.verify(request));
	}

	@Test
	public void failingBadKey() {
		assertThrows(InvalidKeyException.class, () -> {
			RFCData rfcData = new RFCData(messageFactory);
			HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(new KeyMap() {
				@Override
				public SecretKey getSecretKey(String keyId) {
					return null;
				}

				@Override
				public PublicKey getPublicKey(String keyId) {
					return new PublicKey() {

						private static final long serialVersionUID = 1L;

						@Override
						public String getFormat() {
							return "rsa";
						}

						@Override
						public byte[] getEncoded() {
							return randomByteArray(256);
						}

						@Override
						public String getAlgorithm() {
							return "rsa-sha256";
						}
					};
				}

				@Override
				public PrivateKey getPrivateKey(String keyId) {
					return null;
				}
			}).build();
			HttpRequest request = rfcData.request();
			request.addHeader(HttpMessageSigner.HEADER_SIGNATURE, RFCData.SIGNATURE_HEADER_VALUE__ALL_HEADERS_TEST);
			signatureVerifier.verify(request);
		});
	}

	@Test
	public void failingBadRequest() {
		HttpRequest request = messageFactory.createRequest("POST", URI.create("http://example.com/foo/bar"));
		request.addHeader("Signature", "keyId=id,signature=AAAA=");
		HttpMessageSignatureVerificationException e = assertThrows(HttpMessageSignatureVerificationException.class, () -> {
			HttpMessageSignatureVerifier.builder().keyMap(new HashKeyMap()).build().verify(request);
		});
		assertEquals(e.getMessage(), "Unable to verify request '" + request.toString() + "'");
	}

	protected static String toB64(byte[] arr) {
		return Base64.getEncoder().encodeToString(arr);
	}

	protected static byte[] randomByteArray(int arraySize) {
		byte[] arr = new byte[arraySize];
		new Random(73).nextBytes(arr);
		return arr;
	}

	@Test
	public void testSecretKey() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(HashKeyMap.INSTANCE).build();
		HttpRequest request = messageFactory.createRequest("POST", URI.create("http://example.com/post/service?data=4"));
		request.addHeader("Date", LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1)).toString());
		request.addHeader("XXXX", "VVVV");
		String signatureHeader = "keyId=\"user1\",algorithm=\"hmac-sha256\","
				+ "headers=\"(request-target) date XXXX\","
				+ "signature=\"Yji0QwbY0CBsS/xFWAfXANZoFWGFtBoghmXoWmqEHes=\"";
		request.addHeader(HttpMessageSigner.HEADER_SIGNATURE, signatureHeader);
		assertTrue(signatureVerifier.verify(request));
	}
}
