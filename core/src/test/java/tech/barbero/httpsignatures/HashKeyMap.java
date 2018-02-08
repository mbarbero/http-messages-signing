/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikaël Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.httpsignatures;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class HashKeyMap implements KeyMap {
	private final Map<String, PublicKey> pubkMap;
	private final Map<String, PrivateKey> privkMap;
	private final Map<String, SecretKey> seckMap;
	
	public static final HashKeyMap INSTANCE = new HashKeyMap(); 

	HashKeyMap() {
		this.pubkMap = new HashMap<>();
		this.privkMap = new HashMap<>();
		this.seckMap = new HashMap<>();
	}
	
	@Override
	public SecretKey getSecretKey(String keyId) {
		return seckMap.get(keyId);
	}

	@Override
	public PublicKey getPublicKey(String keyId) {
		return pubkMap.get(keyId);
	}

	@Override
	public PrivateKey getPrivateKey(String keyId) {
		return privkMap.get(keyId);
	}
	
	static {
		createKeyPair("key-id", 48);
		createKeyPair("myKeyId", 512);
		
		createSecretKey("key-id", 42);
		createSecretKey("user1", 96);
	}
	
	private static SecretKey createSecretKey(String id, long seed) {
		byte[] bytes = new byte[256];
		new Random(seed).nextBytes(bytes);
		SecretKeySpec secretKey = new SecretKeySpec(bytes, "HmacSHA256");
		INSTANCE.seckMap.put(id, secretKey);
		return secretKey;
	}

	private static KeyPair createKeyPair(String id, long seed) {
		try {
			byte[] seedbytes = new byte[256];
			new Random(seed).nextBytes(seedbytes);
			SecureRandom csprng = new SecureRandom(seedbytes);
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024, csprng);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			INSTANCE.pubkMap.put(id, keyPair.getPublic());
			INSTANCE.privkMap.put(id, keyPair.getPrivate());
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}