/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
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