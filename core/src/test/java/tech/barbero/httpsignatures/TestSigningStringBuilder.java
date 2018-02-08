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
package tech.barbero.httpsignatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSigningStringBuilder {

	private static Instant INSTANT = LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1));
	
	protected MessageFactory messageFactory;

	@BeforeEach
	public void beforeAll() {
		messageFactory = createFactory();
	}

	protected MessageFactory createFactory() {
		return new MessageFactory.MockImpl();
	}
	
	@Test
	public void testNoHeaders() throws InvalidKeyException, NoSuchAlgorithmException {
		String signingString = SigningStringBuilder
				.noHeader()
				.signingString(createDummyRequest());
		assertEquals("date: " + INSTANT.toString(), signingString);
	}
	
	@Test
	public void testNoHeadersToSignNoDateHeaderInRequest() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			SigningStringBuilder.forHeaders(Arrays.asList()).signingString(createRequestWithNoDate());
		});
		assertEquals(e.getMessage(), "A HTTP message must at least contain a date header to be signed");
	}
	
	@Test
	public void testURIWithQuery() throws InvalidKeyException, NoSuchAlgorithmException {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.REQUEST_TARGET))
				.signingString(messageFactory.createRequest("HEAD", URI.create("http://localhost/service?user=me&toasted=42")));
		assertEquals("(request-target): head /service?user=me&toasted=42", signingString);
	}
	
	@Test
	public void testDateHeader() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("date"))
				.signingString(createDummyRequest());
		assertEquals("date: " + INSTANT.toString(), signingString);
	}
	
	@Test
	public void testMissingHeader() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			SigningStringBuilder.forHeaders(Arrays.asList("missing")).signingString(createDummyRequest());
		});
		assertEquals(e.getMessage(), "The following headers cannot be found in the request: 'missing'");
	}
	
	@Test
	public void testDateAndRequestTarget() throws InvalidKeyException, NoSuchAlgorithmException {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("date", HttpMessageSigner.REQUEST_TARGET))
				.signingString(createDummyRequest());
		assertEquals("date: " + INSTANT.toString()+"\n(request-target): post /service", signingString);
	}
	
	@Test
	public void testHeadersWithDifferentCapitalization() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("xXXx"))
				.signingString(createDummyRequest());
		assertEquals("xxxx: VVVV", signingString);
	}
	
	@Test
	public void testHeadersWithDifferentCapitalization2() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("DATE", "xXXx"))
				.signingString(createDummyRequest());
		assertEquals("date: " + INSTANT.toString() + "\nxxxx: VVVV", signingString);
	}

	@Test
	public void testReverseOrderedHeaders() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("xxxx", "date"))
				.signingString(createDummyRequest());
		assertEquals("xxxx: VVVV\ndate: " + INSTANT.toString(), signingString);
	}

	@Test
	public void testWithSpacesInHeaderValue() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("X-Other-Header", "XXXX", "date"))
				.signingString(createDummyRequest());
		assertEquals("x-other-header: one, two  ,   three\nxxxx: VVVV\ndate: " + INSTANT.toString(), signingString);
	}
	
	@Test
	public void testWithMultiHeaders() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("Content-Type", "xxxx", "date"))
				.signingString(createDummyRequest());
		assertEquals("content-type: first/content-type , another/type, second/content-type\nxxxx: VVVV\ndate: " + INSTANT.toString(), signingString);
	}
	
	@Test
	public void testWithMutliHeadersAndParam() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("X2", "XXXX", "DATE"))
				.signingString(createDummyRequest());
		assertEquals("x2: t1=v1 , t2=\"v2\", t4=\"v1, V3 v2\"; p=8.9\nxxxx: VVVV\ndate: " + INSTANT.toString(), signingString);
	}
	
	@Test 
	public void testResponseStatus() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.RESPONSE_STATUS, "date"))
				.signingString(createDummyResponse());
		assertEquals("(response-status): 320\ndate: " + INSTANT.toString(), signingString);
	}
	
	@Test
	public void testRFC_default() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder.noHeader().signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__DEFAULT_TEST, signingString);
	}
	
	@Test
	public void testRFC_basic() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.REQUEST_TARGET, "host", "date"))
				.signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__BASIC_TEST, signingString);
	}
	
	@Test
	public void testRFC_allHeaders() throws GeneralSecurityException {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.REQUEST_TARGET, "host", "date", "content-type", "digest", "content-length"))
				.signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__ALL_HEADERS_TEST, signingString);
	}
	
	private HttpResponse createDummyResponse() {
		return messageFactory.createResponse(320)
				.addHeader("X-From-Proxy", "proxy value")
				.addHeader("Date", INSTANT.toString());
	}
	
	private HttpRequest createRequestWithNoDate() {
		return messageFactory.createRequest("post", URI.create("http://localhost/service"))
				.addHeader("XXXX", "VVVV")
				.addHeader("Content-Type", "first/content-type , another/type ");
	}
	
	private HttpRequest createDummyRequest() {
		return messageFactory.createRequest("post", URI.create("http://localhost/service"))
				.addHeader("Date", INSTANT.toString())
				.addHeader("XXXX", "VVVV")
				.addHeader("X2", "t1=v1 , t2=\"v2\"")
				.addHeader("X2", "t4=\"v1, V3 v2\"; p=8.9")
				.addHeader("X-Other-Header", "one, two  ,   three")
				.addHeader("Content-Type", "first/content-type , another/type ")
				.addHeader("X-Forwarded-Proto", "http")
				.addHeader("Content-Type", "second/content-type");
	}	
}
