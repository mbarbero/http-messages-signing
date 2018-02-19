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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSigningStringBuilder {

	private static final Instant INSTANT = LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1));

	protected MessageFactory messageFactory;

	@BeforeEach
	public void beforeAll() {
		messageFactory = createFactory();
	}

	protected MessageFactory createFactory() {
		return new MessageFactory.MockImpl();
	}

	@Test
	public void testNoHeaders() {
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
		assertEquals(e.getMessage(), "A HTTP message must contain at least a date header to be signed");
	}

	@Test
	public void testURIWithQuery() {
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
		assertEquals(e.getMessage(), "The following headers cannot be found in the message: 'missing'");
	}

	@Test
	public void testDateAndRequestTarget() {
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList("date", HttpMessageSigner.REQUEST_TARGET))
				.signingString(createDummyRequest());
		assertEquals("date: " + INSTANT.toString() + "\n(request-target): post /service", signingString);
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
	public void testRFCDefault() {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder.noHeader().signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__DEFAULT_TEST, signingString);
	}

	@Test
	public void testRFCBasic() {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.REQUEST_TARGET, "host", "date"))
				.signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__BASIC_TEST, signingString);
	}

	@Test
	public void testRFCAllHeaders() {
		RFCData rfcData = new RFCData(messageFactory);
		String signingString = SigningStringBuilder
				.forHeaders(Arrays.asList(HttpMessageSigner.REQUEST_TARGET, "host", "date", "content-type", "digest", "content-length"))
				.signingString(rfcData.request());
		assertEquals(RFCData.SIGNING_STRING__ALL_HEADERS_TEST, signingString);
	}

	private HttpResponse createDummyResponse() {
		HttpResponse response = messageFactory.createResponse(320);
		response.addHeader("X-From-Proxy", "proxy value");
		response.addHeader("Date", INSTANT.toString());
		return response;
	}

	private HttpRequest createRequestWithNoDate() {
		HttpRequest request = messageFactory.createRequest("post", URI.create("http://localhost/service"));
		request.addHeader("XXXX", "VVVV");
		request.addHeader("Content-Type", "first/content-type , another/type ");
		return request;
	}

	private HttpRequest createDummyRequest() {
		HttpRequest request = messageFactory.createRequest("post", URI.create("http://localhost/service"));
		request.addHeader("Date", INSTANT.toString());
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
