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

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHttpSignature {

	private static KeyPair keyPair;
	
	private static Key secretKey;
	
	private static SecureRandom csprng;
	
	private static Instant oneInstant;
	
	@BeforeClass
	public static void beforeClass() throws NoSuchAlgorithmException {
		oneInstant = LocalDateTime.of(2016, 3, 20, 13, 20, 0).toInstant(ZoneOffset.ofHours(1));
		csprng = SecureRandom.getInstance("NativePRNG");
		keyPair = createKeyPair();
		secretKey = createSecretKey();
	}
	
	private static Key createSecretKey() {
		byte[] bytes = new byte[256];
		csprng.nextBytes(bytes);
		return new SecretKeySpec(bytes, "HmacSHA256");
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
	
	@Test(expected=IllegalStateException.class)
	public void testEmptyPrivateKeyHttpSignature() throws InvalidKeyException, NoSuchAlgorithmException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature.builder(signature).build();
	}
	
	@Test
	public void testHttpSignature() throws InvalidKeyException, NoSuchAlgorithmException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("date: " + oneInstant.toString(), signingString);
	}
	
	@Test
	public void testHttpSignatureWithRequestTarget() throws InvalidKeyException, NoSuchAlgorithmException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).addHeaderToSign("date").addHeaderToSign(HttpSignature.REQUEST_TARGET).keyId("myKeyId").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("date: " + oneInstant.toString()+"\n(request-target): post /service", signingString);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testHttpSignatureWithoutDate() throws InvalidKeyException, NoSuchAlgorithmException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("XXXX").build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testHttpSignatureWithUnknownHeader() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("date").addHeaderToSign("UnknownHeader").build();
		httpSignature.sign(createDummyRequest(oneInstant));
	}
	
	@Test
	public void testHttpSignatureWithDateHeader() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("date").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("date: " + oneInstant.toString(), signingString);
	}
	
	@Test
	public void testHttpSignatureWithHeaders() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("date").addHeaderToSign("xXXx").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("date: " + oneInstant.toString() + "\nxxxx: VVVV", signingString);
	}
	
	@Test
	public void testHttpSignatureWithHeaders2() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("xXXx").addHeaderToSign("date").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("xxxx: VVVV\ndate: " + oneInstant.toString(), signingString);
	}

	@Test
	public void testHttpSignatureWithHeaders3() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("X-Other-Header").addHeaderToSign("xXXx").addHeaderToSign("date").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("x-other-header: one, two  ,   three\nxxxx: VVVV\ndate: " + oneInstant.toString(), signingString);
	}
	
	@Test
	public void testHttpSignatureWithHeaders4() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("Content-Type").addHeaderToSign("xXXx").addHeaderToSign("date").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("content-type: first/content-type , another/type, second/content-type\nxxxx: VVVV\ndate: " + oneInstant.toString(), signingString);
	}
	
	@Test
	public void testHttpSignatureWithHeaders5() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("myKeyId").addHeaderToSign("X2").addHeaderToSign("xXXx").addHeaderToSign("date").build();
		String signingString = SigningStringBuilder.signingString(httpSignature.headersToSign(), createDummyRequest(oneInstant));
		Assert.assertEquals("x2: t1=v1 , t2=\"v2\", t4=\"v1, V3 v2\"; p=8.9\nxxxx: VVVV\ndate: " + oneInstant.toString(), signingString);
	}
	
	@Test
	public void testSignature1() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		RequestMock request = createDummyRequest(oneInstant);
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("key-id").build();
		
		RequestMock signedRequest = httpSignature.sign(request);
		List<String> signatureHeaderValues = signedRequest.headerValues("signature");
		Assert.assertEquals("keyId=key-id", signatureHeaderValues.get(0));
		Assert.assertEquals("algorithm=rsa-sha256", signatureHeaderValues.get(1));
		Assert.assertTrue(signatureHeaderValues.get(2).startsWith("signature="));
	}
	
	@Test
	public void testSignature2() throws GeneralSecurityException {
		Mac mac = getMac("HmacSHA256", secretKey);
		RequestMock request = createDummyRequest(oneInstant);
		HttpSignature httpSignature = HttpSignature.builder(mac).keyId("user1").build();
		
		RequestMock signedRequest = httpSignature.sign(request);
		List<String> signatureHeaderValues = signedRequest.headerValues("signature");
		Assert.assertEquals("keyId=user1", signatureHeaderValues.get(0));
		Assert.assertEquals("algorithm=hmac-sha256", signatureHeaderValues.get(1));
		Assert.assertTrue(signatureHeaderValues.get(2).startsWith("signature="));
	}
	
	@Test
	public void testPublicKeySignature() throws GeneralSecurityException {
		Signature signature = getSignature("SHA256withRSA", keyPair.getPrivate());
		HttpSignature httpSignature = HttpSignature.builder(signature).keyId("key-id").build();
		
		RequestMock request = httpSignature.sign(createDummyRequest(oneInstant));
		HttpSignatureHeaderElements signatureHeaderElements = HttpSignatureHeaderElements.fromHeadersList(request.headerValues("signature"));
		signature = getSignature("SHA256withRSA", keyPair.getPublic());
		signature.update(SigningStringBuilder.signingString(httpSignature.headersToSign(), request).getBytes());
		Assert.assertTrue(signature.verify(Base64.getDecoder().decode(signatureHeaderElements.signature())));
	}

	@Test
	public void testPrivateKeySignature() throws GeneralSecurityException {
		Mac mac = getMac("HmacSHA256", secretKey);
		HttpSignature httpSignature = HttpSignature.builder(mac).keyId("key-id").build();
		
		RequestMock request = httpSignature.sign(createDummyRequest(oneInstant));
		HttpSignatureHeaderElements signatureHeaderElements = HttpSignatureHeaderElements.fromHeadersList(request.headerValues("signature"));
		mac = getMac("HmacSHA256", secretKey);
		mac.update(SigningStringBuilder.signingString(httpSignature.headersToSign(), request).getBytes());
		Assert.assertArrayEquals(mac.doFinal(), Base64.getDecoder().decode(signatureHeaderElements.signature()));
	}

	@Test
	public void defaultTestFromRFC() throws GeneralSecurityException {
		KeyPair rfcKeyPair = getRFCKeyPair();
		RequestMock request = getRFCRequest();
		
		HttpSignature httpSignature = HttpSignature.builder(getSignature("SHA256withRSA", rfcKeyPair.getPrivate())).keyId("Test").build();
		Assert.assertEquals("date: Sun, 05 Jan 2014 21:31:40 GMT", SigningStringBuilder.signingString(httpSignature.headersToSign(), request));
		
		request = httpSignature.sign(request);
		List<String> headerValues = request.headerValues("Signature");
		Assert.assertEquals("keyId=Test", headerValues.get(0));
		Assert.assertEquals("algorithm=rsa-sha256", headerValues.get(1));
		Assert.assertEquals("signature=SjWJWbWN7i0wzBvtPl8rbASWz5xQW6mcJmn+ibttBqtifLN7Sazz" + 
				"6m79cNfwwb8DMJ5cou1s7uEGKKCs+FLEEaDV5lp7q25WqS+lavg7T8hc0GppauB" + 
				"6hbgEKTwblDHYGEtbGmtdHgVCk9SuS13F0hZ8FD0k/5OxEPXe5WozsbM=", headerValues.get(2));
		
		HttpSignatureHeaderElements signatureHeaderElements = HttpSignatureHeaderElements.fromHeadersList(request.headerValues("signature"));
		Signature signature = getSignature("SHA256withRSA", rfcKeyPair.getPublic());
		signature.update(SigningStringBuilder.signingString(httpSignature.headersToSign(), request).getBytes());
		Assert.assertTrue(signature.verify(Base64.getDecoder().decode(signatureHeaderElements.signature())));
	}
	
	@Test
	public void allHeadersTestFromRFC() throws GeneralSecurityException {
		KeyPair rfcKeyPair = getRFCKeyPair();
		RequestMock request = getRFCRequest();
		
		HttpSignature httpSignature = HttpSignature.builder(getSignature("SHA256withRSA", rfcKeyPair.getPrivate()))
				.addHeaderToSign(HttpSignature.REQUEST_TARGET)
				.addHeaderToSign("Host")
				.addHeaderToSign("Date")
				.addHeaderToSign("Content-Type")
				.addHeaderToSign("Digest")
				.addHeaderToSign("Content-Length")
				.keyId("Test").build();
		Assert.assertEquals("(request-target): post /foo?param=value&pet=dog\n" + 
				"host: example.com\n" + 
				"date: Sun, 05 Jan 2014 21:31:40 GMT\n" + 
				"content-type: application/json\n" + 
				"digest: SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=\n" + 
				"content-length: 18", SigningStringBuilder.signingString(httpSignature.headersToSign(), request));
		
		request = httpSignature.sign(request);
		List<String> headerValues = request.headerValues("Signature");
		Assert.assertEquals("keyId=Test", headerValues.get(0));
		Assert.assertEquals("algorithm=rsa-sha256", headerValues.get(1));
		Assert.assertEquals("headers=\"(request-target) host date content-type digest content-length\"", headerValues.get(2));
		Assert.assertEquals("signature=vSdrb+dS3EceC9bcwHSo4MlyKS59iFIrhgYkz8+oVLEEzmYZZvRs" + 
				"8rgOp+63LEM3v+MFHB32NfpB2bEKBIvB1q52LaEUHFv120V01IL+TAD48XaERZF" + 
				"ukWgHoBTLMhYS2Gb51gWxpeIq8knRmPnYePbF5MOkR0Zkly4zKH7s1dE=", headerValues.get(3));
		
		HttpSignatureHeaderElements signatureHeaderElements = HttpSignatureHeaderElements.fromHeadersList(request.headerValues("signature"));
		Signature signature = getSignature("SHA256withRSA", rfcKeyPair.getPublic());
		signature.update(SigningStringBuilder.signingString(httpSignature.headersToSign(), request).getBytes());
		Assert.assertTrue(signature.verify(Base64.getDecoder().decode(signatureHeaderElements.signature())));
	}
	
	@Test
	public void basicTestFromRFC() throws GeneralSecurityException {
		KeyPair rfcKeyPair = getRFCKeyPair();
		RequestMock request = getRFCRequest();
		
		HttpSignature httpSignature = HttpSignature.builder(getSignature("SHA256withRSA", rfcKeyPair.getPrivate()))
				.addHeaderToSign(HttpSignature.REQUEST_TARGET)
				.addHeaderToSign("Host")
				.addHeaderToSign("Date")
				.keyId("Test").build();
		Assert.assertEquals("(request-target): post /foo?param=value&pet=dog\n" + 
				"host: example.com\n" + 
				"date: Sun, 05 Jan 2014 21:31:40 GMT", SigningStringBuilder.signingString(httpSignature.headersToSign(), request));
		
		request = httpSignature.sign(request);
		List<String> headerValues = request.headerValues("Signature");
		Assert.assertEquals("keyId=Test", headerValues.get(0));
		Assert.assertEquals("algorithm=rsa-sha256", headerValues.get(1));
		Assert.assertEquals("headers=\"(request-target) host date\"", headerValues.get(2));
		Assert.assertEquals("signature=qdx+H7PHHDZgy4" + 
				"y/Ahn9Tny9V3GP6YgBPyUXMmoxWtLbHpUnXS2mg2+SbrQDMCJypxBLSPQR2aAjn" + 
				"7ndmw2iicw3HMbe8VfEdKFYRqzic+efkb3nndiv/x1xSHDJWeSWkx3ButlYSuBs" + 
				"kLu6kd9Fswtemr3lgdDEmn04swr2Os0=", headerValues.get(3));
		
		HttpSignatureHeaderElements signatureHeaderElements = HttpSignatureHeaderElements.fromHeadersList(request.headerValues("signature"));
		Signature signature = getSignature("SHA256withRSA", rfcKeyPair.getPublic());
		signature.update(SigningStringBuilder.signingString(httpSignature.headersToSign(), request).getBytes());
		Assert.assertTrue(signature.verify(Base64.getDecoder().decode(signatureHeaderElements.signature())));
	}

	private RequestMock getRFCRequest() {
		RequestMock request = new RequestMock("post", URI.create("http://example.com/foo?param=value&pet=dog"));
		request.addHeader("Host", "example.com")
			.addHeader("Date", "Sun, 05 Jan 2014 21:31:40 GMT")
			.addHeader("Content-Type", "application/json")
			.addHeader("Digest", "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=")
			.addHeader("Content-Length", "18");
		return request;
	}

	private static KeyPair getRFCKeyPair() throws GeneralSecurityException {
		byte[] rsaPrivateKey = Base64.getDecoder().decode(
				"MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMIUQ0bDffIaKHL3akONlCGXQLfq" + 
				"s8mP4K99ILz6rbyHEDXrVAU1R3XfC4JNRyrRB3aqwF7/aEXJzYMIkmDSHUvvz7pnhQxHsQ5yl91Q" + 
				"T0d/eb+Gz4VRHjm4El4MrUdIUcPxscoPqS/wU8Z8lOi1z7bGMnChiL7WGqnV8h6RrGzJAgMBAAEC" + 
				"gYEAlHxmQJS/HmTO/6612XtPkyeit1PVO+hdckZcrtln5S68w1QJ03ZA9ziwGIBBa8vDVxIq3kOw" + 
				"pnxQROlg/Lyk9iecMTPZ0NiJp7D37ESm5vJ5bagfhnHvXCoG04qSrCtdr+nN2mK5xFGOTq8Tphjs" + 
				"QEGz+Du5qdWkaJs5UASyofUCQQDsOSNUfbxYNSB/Weq9+fYqPoJPuchwTeMYmxlnvOVmYGYcUM40" + 
				"wtStdH9mbelHmbS0KYGprlEr3m7jXaO3V08jAkEA0lPe/ymeS2HjxtCj98p6Xq4RjJuhG0Dn+4e4" + 
				"eRnoVAXs5SQaiByZImW451zm3qEjVWwufRBkSNBkwQ5av7ApIwJBAILiRckSwcC97vug/oe0b8iI" + 
				"SfuSnJRdE28WwMTRzOkkkG8v9pEVQnG5Er3WOGMLrywDs2wowaDk5dvkjkmPfrECQQCAhPtoU5gE" + 
				"XAaBABCRY0ou/JKApsBlFN4sFpykcy5B2XUN92e28DKqkBnSVjREqZYbpoUpqpB85coLJahSJWSd" + 
				"AkBeuWDJIVyL/a54qUgTVCoiItJnxXw6WkUtGdvWnMjtTXJBedMAQVgznrTImXNSk5vVXhxJwZ3f" + 
				"rm2JIy/Es69M");
		byte[] rsaPublicKey = Base64.getDecoder().decode(
				"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCFENGw33yGihy92pDjZQhl0C36rPJj+CvfSC8" + 
				"+q28hxA161QFNUd13wuCTUcq0Qd2qsBe/2hFyc2DCJJg0h1L78+6Z4UMR7EOcpfdUE9Hf3m/hs+F" + 
				"UR45uBJeDK1HSFHD8bHKD6kv8FPGfJTotc+2xjJwoYi+1hqp1fIekaxsyQIDAQAB"); 
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey));
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rsaPublicKey));
		KeyPair rfcKeyPair = new KeyPair(publicKey, privateKey);
		return rfcKeyPair;
	}
	
	private RequestMock createDummyRequest(Instant instant) {
		return new RequestMock("post", URI.create("http://localhost/service"))
				.addHeader("Date", instant.toString())
				.addHeader("XXXX", "VVVV")
				.addHeader("X2", "t1=v1 , t2=\"v2\"")
				.addHeader("X2", "t4=\"v1, V3 v2\"; p=8.9")
				.addHeader("X-Other-Header", "one, two  ,   three")
				.addHeader("Content-Type", "first/content-type , another/type ")
				.addHeader("X-Forwarded-Proto", "http")
				.addHeader("Content-Type", "second/content-type");
	}

	private Mac getMac(String algorithm, Key key) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance(algorithm);
		mac.init(key);
		return mac;
	}
	
	private Signature getSignature(String algorithm, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException {
		Signature signature = Signature.getInstance(algorithm);
		signature.initSign(privateKey);
		return signature;
	}
	
	private Signature getSignature(String algorithm, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException {
		Signature signature = Signature.getInstance(algorithm);
		signature.initVerify(publicKey);
		return signature;
	}
}
