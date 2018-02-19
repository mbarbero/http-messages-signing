/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

public class HttpMessageSignatureVerificationException extends Exception {

	private static final long serialVersionUID = -661385921885321777L;

	public HttpMessageSignatureVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpMessageSignatureVerificationException(String string) {
		super(string);
	}

}
