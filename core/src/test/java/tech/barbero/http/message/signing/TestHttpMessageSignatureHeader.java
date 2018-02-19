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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import tech.barbero.http.message.signing.HttpMessageSigner.Algorithm;

public class TestHttpMessageSignatureHeader {

	@Test
	public void emptyHeader() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValue("");
		});
		assertEquals("Missing required properties: keyId algorithm signature", e.getMessage());
	}

	@Test
	public void emptyHeaderList() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValuesList(Arrays.asList());
		});
		assertEquals("Missing required properties: keyId algorithm signature", e.getMessage());
	}

	@Test
	public void missingKeyId() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValue(
					"algorithm=\"rsa-sha256\",signature=\"XXXXXXXXXXXXXXXX==\"");
		});
		assertEquals("Missing required properties: keyId", e.getMessage());
	}

	@Test
	public void missingAlgorithm() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValue(
					"keyId=\"rsa-key-1\",signature=\"XXXXXXXXXXXXXXXX==\"");
		});
		assertEquals("Missing required properties: algorithm", e.getMessage());
	}

	@Test
	public void missingSignature() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValue(
					"algorithm=\"rsa-sha256\",keyId=\"rsa-key-1\"");
		});
		assertEquals("Missing required properties: signature", e.getMessage());
	}

	@Test
	public void missingHeaders() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"algorithm=\"rsa-sha256\",keyId=\"rsa-key-1\",signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("date"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void missingHeaders2() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"algorithm=\"rsa-sha256\",headers=\"mmmm\",keyId=\"rsa-key-1\",signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("mmmm"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void singleHeader1() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\","
						+ "headers=\"(request-target) host date digest content-length\","
						+ "signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("(request-target)", "host", "date", "digest", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void singleHeader2() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValuesList(Arrays.asList(
				"keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\","
						+ "headers=\"(request-target) host date digest content-length\","
						+ "signature=\"XXXXXXXXXXXXXXXX==\""));
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("(request-target)", "host", "date", "digest", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void multipleSignedHeaders() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValuesList(Arrays.asList(
				"keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\","
						+ "headers=\"host date host content-length\","
						+ "signature=\"XXXXXXXXXXXXXXXX==\""));
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("host", "date", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void multipleHeader() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValuesList(Arrays.asList(
				"keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\"",
				"headers=\"(request-target) host date digest content-length\"",
				"signature=\"XXXXXXXXXXXXXXXX==\""));
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("(request-target)", "host", "date", "digest", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void multipleHeaderWithVariousSpaces() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\","
						+ "headers=\"(request-target) host date digest content-length\","
						+ "signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("(request-target)", "host", "date", "digest", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void unsupportedAlgorithm() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			HttpMessageSignatureHeaderElements.fromHeaderValue(
					"keyId=\"rsa-key-1\",algorithm=\"rsa-sha512\","
							+ "headers=\"(request-target) host date digest content-length\","
							+ "signature=\"XXXXXXXXXXXXXXXX==\"");
		});
		assertEquals("Unsupported algorithm 'rsa-sha512'", e.getMessage());
	}

	@Test
	public void withUnknownElement() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"pirate=3l33t,keyId=\"rsa-key-1\",algorithm=\"rsa-sha256\","
						+ "headers=\"(request-target) host date digest content-length\",again=\"overflooow\","
						+ "signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals("rsa-key-1", signatureHeader.keyId());
		assertEquals(Algorithm.RSA_SHA256, signatureHeader.algorithm());
		assertIterableEquals(Arrays.asList("(request-target)", "host", "date", "digest", "content-length"), signatureHeader.signedHeaders());
		assertEquals("XXXXXXXXXXXXXXXX==", signatureHeader.signature());
	}

	@Test
	public void twoAlgorithms() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"algorithm=\"rsa-sha256\",keyId=\"rsa-key-1\",algorithm=\"rsa-sha1\",signature=\"XXXXXXXXXXXXXXXX==\"");
		assertEquals(Algorithm.RSA_SHA1, signatureHeader.algorithm());
	}

	@Test
	public void threeKeyId() {
		HttpMessageSignatureHeaderElements signatureHeader = HttpMessageSignatureHeaderElements.fromHeaderValue(
				"algorithm=\"rsa-sha256\",keyId=\"rsa-key-1\",keyId=\"rsa-key-2\", ,,,signature=\"XXXXXXXXXXXXXXXX==\",keyId=rsa-key-3");
		assertEquals("rsa-key-3", signatureHeader.keyId());
	}
}
