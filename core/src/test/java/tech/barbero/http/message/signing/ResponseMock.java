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
package tech.barbero.http.message.signing;

import tech.barbero.http.message.signing.HttpResponse;

class ResponseMock extends MessageMock implements HttpResponse {

	private final int statusCode;

	ResponseMock(int statusCode) {
		this.statusCode = statusCode;
	}
	
	@Override
	public int statusCode() {
		return statusCode;
	}

}
