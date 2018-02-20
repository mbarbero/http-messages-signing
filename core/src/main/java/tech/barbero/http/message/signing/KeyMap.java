/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

/**
 * A {@link KeyStore} like interface.
 */
public interface KeyMap {

	/**
	 * Returns the public key associated with the given {@code keyId} or null if none.
	 *
	 * @param keyId
	 *          the id of key to be returned.
	 * @return the public key associated with the given {@code keyId} or null if none.
	 */
	PublicKey getPublicKey(String keyId);

	/**
	 * Returns the private key associated with the given {@code keyId} or null if none.
	 *
	 * @param keyId
	 *          the id of key to be returned.
	 * @return the private key associated with the given {@code keyId} or null if none.
	 */
	PrivateKey getPrivateKey(String keyId);

	/**
	 * Returns the secret key associated with the given {@code keyId} or null if none.
	 *
	 * @param keyId
	 *          the id of key to be returned.
	 * @return the secret key associated with the given {@code keyId} or null if none.
	 */
	SecretKey getSecretKey(String keyId);
}
