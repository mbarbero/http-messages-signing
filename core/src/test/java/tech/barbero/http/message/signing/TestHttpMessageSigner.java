/*******************************************************************************
 * Copyright (c) 2017 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikaël Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.http.message.signing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueFormatter;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.BasicLineParser;
import org.apache.http.util.CharArrayBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.barbero.http.message.signing.HttpMessageSignatureVerificationException;
import tech.barbero.http.message.signing.HttpMessageSignatureVerifier;
import tech.barbero.http.message.signing.HttpMessageSigner;
import tech.barbero.http.message.signing.HttpRequest;
import tech.barbero.http.message.signing.HttpMessageSigner.Algorithm;

public class TestHttpMessageSigner {

	protected MessageFactory messageFactory;

	@BeforeEach
	public void beforeAll() {
		messageFactory = createFactory();
	}

	protected MessageFactory createFactory() {
		return new MessageFactory.MockImpl();
	}
	
	@Test
	public void emptyPrivateKey() throws InvalidKeyException, NoSuchAlgorithmException {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSigner.builder()
				.algorithm(Algorithm.RSA_SHA256)
				.build();
		});
		assertEquals(e.getMessage(), "Missing required properties: keyId keyMap");
	}
	
	@Test
	public void signatureWithoutDate() throws InvalidKeyException, NoSuchAlgorithmException {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSigner.builder()
				.algorithm(Algorithm.RSA_SHA256)
				.keyMap(HashKeyMap.INSTANCE)
				.keyId("myKeyId")
				.addHeaderToSign("XXXX")
				.build();
		});
		assertEquals(e.getMessage(), "HttpMessageSigner should be configured to sign the '"+HttpMessageSigner.HEADER_DATE+"' header");
	}
	
	@Test
	public void signatureWithoutRequestTarget() throws InvalidKeyException, NoSuchAlgorithmException {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSigner.builder()
				.algorithm(Algorithm.RSA_SHA256)
				.keyMap(HashKeyMap.INSTANCE)
				.keyId("myKeyId")
				.addHeaderToSign("date")
				.build();
		});
		assertEquals(e.getMessage(), "HttpMessageSigner should be configured to sign the '"+HttpMessageSigner.REQUEST_TARGET+"' header");
	}
	
	@Test
	public void signatureWithUnknownHeader() throws GeneralSecurityException {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSigner httpSigner = HttpMessageSigner.builder()
				.algorithm(Algorithm.RSA_SHA256)
				.keyMap(HashKeyMap.INSTANCE)
				.keyId("myKeyId")
				.addHeaderToSign("date")
				.addHeaderToSign(HttpMessageSigner.REQUEST_TARGET)
				.addHeaderToSign("UnknownHeader")
				.build();
			httpSigner.sign(createDummyRequest());
		});
		assertEquals(e.getMessage(), "The following headers cannot be found in the message: 'UnknownHeader'");
	}
	
	@Test
	public void publicKeySignature() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		HttpMessageSigner httpSigner = HttpMessageSigner.builder().algorithm(Algorithm.RSA_SHA256).keyMap(HashKeyMap.INSTANCE).keyId("key-id").build();
		
		HttpRequest signedRequest = httpSigner.sign(createDummyRequest());
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(HashKeyMap.INSTANCE).build();
		assertTrue(signatureVerifier.verify(signedRequest));
	}
	
	@Test
	public void publicDuplicatedHeaderToSign() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		HttpMessageSigner httpSigner = HttpMessageSigner.builder().algorithm(Algorithm.RSA_SHA256).keyMap(HashKeyMap.INSTANCE).keyId("key-id").addHeaderToSign(HttpMessageSigner.REQUEST_TARGET).addHeaderToSign("Date").addHeaderToSign("X2").addHeaderToSign("Date").build();
		assertIterableEquals(Arrays.asList("(request-target)", "Date", "X2"), httpSigner.headersToSign());
	}

	@Test
	public void privateKeySignature() throws GeneralSecurityException, HttpMessageSignatureVerificationException {
		HttpMessageSigner httpSigner = HttpMessageSigner.builder().algorithm(Algorithm.HMAC_SHA256).keyMap(HashKeyMap.INSTANCE).keyId("key-id").build();
		
		HttpRequest signedRequest = httpSigner.sign(createDummyRequest());
		HttpMessageSignatureVerifier signatureVerifier = HttpMessageSignatureVerifier.builder().keyMap(HashKeyMap.INSTANCE).build();
		assertTrue(signatureVerifier.verify(signedRequest));
	}

	@Test
	public void defaultTestFromRFC() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSigner messageSigner = HttpMessageSigner.builder().algorithm(Algorithm.RSA_SHA256).keyMap(rfcData.keyMap()).keyId(RFCData.KEY_ID).build();
		String signatureHeader = messageSigner.sign(rfcData.request()).headerValues(HttpMessageSigner.HEADER_SIGNATURE).stream().collect(Collectors.joining(","));
		assertHeaderEquals(RFCData.SIGNATURE_HEADER__DEFAULT_TEST, HttpMessageSigner.HEADER_SIGNATURE + ": " + signatureHeader);
	}
	
	@Test
	public void basicTestFromRFC() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSigner messageSigner = HttpMessageSigner.builder().algorithm(Algorithm.RSA_SHA256).keyMap(rfcData.keyMap()).keyId(RFCData.KEY_ID)
				.addHeaderToSign(HttpMessageSigner.REQUEST_TARGET)
				.addHeaderToSign("host")
				.addHeaderToSign("date")
				.build();
		String signatureHeader = messageSigner.sign(rfcData.request()).headerValues(HttpMessageSigner.HEADER_SIGNATURE).stream().collect(Collectors.joining(","));
		assertHeaderEquals(RFCData.SIGNATURE_HEADER__BASIC_TEST, HttpMessageSigner.HEADER_SIGNATURE + ": " + signatureHeader);
	}
	
	@Test
	public void allHeadersTestFromRFC() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		HttpMessageSigner messageSigner = HttpMessageSigner.builder().algorithm(Algorithm.RSA_SHA256).keyMap(rfcData.keyMap()).keyId(RFCData.KEY_ID)
				.addHeaderToSign(HttpMessageSigner.REQUEST_TARGET)
				.addHeaderToSign("host")
				.addHeaderToSign("date")
				.addHeaderToSign("content-type")
				.addHeaderToSign("digest")
				.addHeaderToSign("content-length")
				.build();
		String signatureHeader = messageSigner.sign(rfcData.request()).headerValues(HttpMessageSigner.HEADER_SIGNATURE).stream().collect(Collectors.joining(","));
		assertHeaderEquals(RFCData.SIGNATURE_HEADER__ALL_HEADERS_TEST, HttpMessageSigner.HEADER_SIGNATURE + ": " + signatureHeader);
	}
	
	private static void assertHeaderEquals(String expected, String actual) {
		assertEquals(
				formatHeader(expected),
				formatHeader(actual)
			);
	}

	private static String formatHeader(String value) {
		return BasicLineFormatter.formatHeader(parseHeader(value), new BasicLineFormatter() {
			public CharArrayBuffer formatHeader(CharArrayBuffer buffer, Header header) {
				if (header == null) {
					throw new IllegalArgumentException("Header may not be null");
				}
				CharArrayBuffer result = initBuffer(buffer);
				doFormatHeader(result, header);
				return result;
			}

			protected void doFormatHeader(final CharArrayBuffer buffer, final Header header) {
				final String name = header.getName();
				final String value = formatHeaderElements(header.getValue());

				int len = name.length() + 2;
				if (value != null) {
					len += value.length();
				}
				buffer.ensureCapacity(len);

				buffer.append(name);
				buffer.append(": ");
				if (value != null) {
					buffer.append(value);
				}
			}
		});
	}
	
	private static String formatHeaderElements(String value) {
		return BasicHeaderValueFormatter.formatElements(parseHeaderElements(value), false, BasicHeaderValueFormatter.DEFAULT);
	}
	
	private static HeaderElement[] parseHeaderElements(String value) {
		return BasicHeaderValueParser.parseElements(value, BasicHeaderValueParser.DEFAULT);
	}

	private static  Header parseHeader(String value) {
		return BasicLineParser.parseHeader(value, BasicLineParser.DEFAULT);
	}
	
	private HttpRequest createDummyRequest() {
		HttpRequest request = messageFactory.createRequest("post", URI.create("http://localhost/service"));
		request.addHeader("Date", LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1)).toString());
		request.addHeader("XXXX", "VVVV");
		request.addHeader("X2", "t1=v1 , t2=\"v2\"");
		request.addHeader("X2", "t4=\"v1, V3 v2\"; p=8.9");
		request.addHeader("X-Other-Header", "one, two  ,   three");
		request.addHeader("Content-Type", "first/content-type , another/type ");
		request.addHeader("X-Forwarded-Proto", "http");
		request.addHeader("Content-Type", "second/content-type");
		return request;
	}
}
