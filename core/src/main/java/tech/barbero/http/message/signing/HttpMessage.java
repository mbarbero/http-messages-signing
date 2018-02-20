/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.util.List;

/**
 * HTTP messages consist of requests from client to server and responses from server to client. This interface expose the minimum surface for the purpose of
 * http-message-signing.
 */
public interface HttpMessage {

	/**
	 * Returns all the values of the headers with a specified name of this message. Headers and associated values must be ordered in the sequence they will be sent over a
	 * connection.
	 *
	 * @param name
	 *          the name of the header of which values are returned.
	 * @return values of all headers with a specified name.
	 */
	List<String> headerValues(String name);

	/**
	 * Adds a header with the given value to this message.
	 *
	 * @param name
	 *          the name of the header.
	 * @param value
	 *          the value of the header.
	 */
	void addHeader(String name, String value);
}
