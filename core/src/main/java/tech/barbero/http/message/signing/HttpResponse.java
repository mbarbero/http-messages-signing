/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

/**
 * After receiving and interpreting a request message, a server responds with an HTTP response message.
 */
public interface HttpResponse extends HttpMessage {

	/**
	 * Returns the numeric status code of the response.
	 *
	 * @return the numeric status code of the response.
	 */
	int statusCode();
}
