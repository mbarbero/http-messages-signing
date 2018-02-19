/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;

public class RFCData {

	public static final String KEY_ID = "Test";

	public static final String SIGNING_STRING__DEFAULT_TEST = "date: Sun, 05 Jan 2014 21:31:40 GMT";

	public static final String SIGNING_STRING__BASIC_TEST = "(request-target): post /foo?param=value&pet=dog\n"
			+ "host: example.com\n"
			+ "date: Sun, 05 Jan 2014 21:31:40 GMT";

	public static final String SIGNING_STRING__ALL_HEADERS_TEST = "(request-target): post /foo?param=value&pet=dog\n"
			+ "host: example.com\n"
			+ "date: Sun, 05 Jan 2014 21:31:40 GMT\n"
			+ "content-type: application/json\n"
			+ "digest: SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=\n"
			+ "content-length: 18";

	public static final String SIGNATURE_HEADER_VALUE__DEFAULT_TEST = "keyId=\"" + KEY_ID + "\",algorithm=\"rsa-sha256\","
			+ "signature=\"SjWJWbWN7i0wzBvtPl8rbASWz5xQW6mcJmn+ibttBqtifLN7Sazz"
			+ "6m79cNfwwb8DMJ5cou1s7uEGKKCs+FLEEaDV5lp7q25WqS+lavg7T8hc0GppauB"
			+ "6hbgEKTwblDHYGEtbGmtdHgVCk9SuS13F0hZ8FD0k/5OxEPXe5WozsbM=\"";

	public static final String SIGNATURE_HEADER__DEFAULT_TEST = "Signature: " + SIGNATURE_HEADER_VALUE__DEFAULT_TEST;

	public static final String SIGNATURE_HEADER_VALUE__BASIC_TEST = "keyId=\"" + KEY_ID + "\",algorithm=\"rsa-sha256\","
			+ "headers=\"(request-target) host date\", signature=\"qdx+H7PHHDZgy4"
			+ "y/Ahn9Tny9V3GP6YgBPyUXMmoxWtLbHpUnXS2mg2+SbrQDMCJypxBLSPQR2aAjn"
			+ "7ndmw2iicw3HMbe8VfEdKFYRqzic+efkb3nndiv/x1xSHDJWeSWkx3ButlYSuBs"
			+ "kLu6kd9Fswtemr3lgdDEmn04swr2Os0=\"";

	public static final String SIGNATURE_HEADER__BASIC_TEST = "Signature: " + SIGNATURE_HEADER_VALUE__BASIC_TEST;

	public static final String SIGNATURE_HEADER_VALUE__ALL_HEADERS_TEST = "keyId=\"" + KEY_ID + "\",algorithm=\"rsa-sha256\","
			+ "headers=\"(request-target) host date content-type digest content-length\","
			+ "signature=\"vSdrb+dS3EceC9bcwHSo4MlyKS59iFIrhgYkz8+oVLEEzmYZZvRs"
			+ "8rgOp+63LEM3v+MFHB32NfpB2bEKBIvB1q52LaEUHFv120V01IL+TAD48XaERZF"
			+ "ukWgHoBTLMhYS2Gb51gWxpeIq8knRmPnYePbF5MOkR0Zkly4zKH7s1dE=\"";

	public static final String SIGNATURE_HEADER__ALL_HEADERS_TEST = "Signature: " + SIGNATURE_HEADER_VALUE__ALL_HEADERS_TEST;

	private final MessageFactory messageFactory;

	public RFCData(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	public HttpRequest request() {
		HttpRequest r = messageFactory.createRequest("POST", URI.create("http://example.com/foo?param=value&pet=dog"));
		r.addHeader("Host", "example.com");
		r.addHeader("Date", "Sun, 05 Jan 2014 21:31:40 GMT");
		r.addHeader("Content-Type", "application/json");
		r.addHeader("Digest", "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=");
		r.addHeader("Content-Length", "18");
		return r;
	}

	public KeyMap keyMap() {
		return new KeyMap() {
			@Override
			public SecretKey getSecretKey(String keyId) {
				throw new IllegalStateException();
			}

			@Override
			public PublicKey getPublicKey(String keyId) {
				if (KEY_ID.equals(keyId)) {
					byte[] rsaPublicKey = Base64.getDecoder().decode(
							"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCFENGw33yGihy92pDjZQhl0C36rPJj+CvfSC8"
									+ "+q28hxA161QFNUd13wuCTUcq0Qd2qsBe/2hFyc2DCJJg0h1L78+6Z4UMR7EOcpfdUE9Hf3m/hs+F"
									+ "UR45uBJeDK1HSFHD8bHKD6kv8FPGfJTotc+2xjJwoYi+1hqp1fIekaxsyQIDAQAB");
					try {
						return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rsaPublicKey));
					} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
						throw new IllegalStateException(e);
					}
				}
				throw new IllegalStateException();
			}

			@Override
			public PrivateKey getPrivateKey(String keyId) {
				if (KEY_ID.equals(keyId)) {
					byte[] rsaPrivateKey = Base64.getDecoder().decode(
							// openSSL pkcs8 -in rfc.pem -topk8 -nocrypt -out javaTest.pk8
							"MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMIUQ0bDffIaKHL3akONlCGXQLfq"
									+ "s8mP4K99ILz6rbyHEDXrVAU1R3XfC4JNRyrRB3aqwF7/aEXJzYMIkmDSHUvvz7pnhQxHsQ5yl91Q"
									+ "T0d/eb+Gz4VRHjm4El4MrUdIUcPxscoPqS/wU8Z8lOi1z7bGMnChiL7WGqnV8h6RrGzJAgMBAAEC"
									+ "gYEAlHxmQJS/HmTO/6612XtPkyeit1PVO+hdckZcrtln5S68w1QJ03ZA9ziwGIBBa8vDVxIq3kOw"
									+ "pnxQROlg/Lyk9iecMTPZ0NiJp7D37ESm5vJ5bagfhnHvXCoG04qSrCtdr+nN2mK5xFGOTq8Tphjs"
									+ "QEGz+Du5qdWkaJs5UASyofUCQQDsOSNUfbxYNSB/Weq9+fYqPoJPuchwTeMYmxlnvOVmYGYcUM40"
									+ "wtStdH9mbelHmbS0KYGprlEr3m7jXaO3V08jAkEA0lPe/ymeS2HjxtCj98p6Xq4RjJuhG0Dn+4e4"
									+ "eRnoVAXs5SQaiByZImW451zm3qEjVWwufRBkSNBkwQ5av7ApIwJBAILiRckSwcC97vug/oe0b8iI"
									+ "SfuSnJRdE28WwMTRzOkkkG8v9pEVQnG5Er3WOGMLrywDs2wowaDk5dvkjkmPfrECQQCAhPtoU5gE"
									+ "XAaBABCRY0ou/JKApsBlFN4sFpykcy5B2XUN92e28DKqkBnSVjREqZYbpoUpqpB85coLJahSJWSd"
									+ "AkBeuWDJIVyL/a54qUgTVCoiItJnxXw6WkUtGdvWnMjtTXJBedMAQVgznrTImXNSk5vVXhxJwZ3f"
									+ "rm2JIy/Es69M");
					try {
						return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey));
					} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
						throw new IllegalStateException(e);
					}
				}
				throw new IllegalStateException();
			}
		};
	}
}
