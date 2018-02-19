/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.okhttp3;

import tech.barbero.http.message.signing.MessageFactory;
import tech.barbero.http.message.signing.TestSigningStringBuilder;

public class TestAHCSigningStringBuilder extends TestSigningStringBuilder {

	@Override
	protected MessageFactory createFactory() {
		return new OkHttp3MessageFactory();
	}

	@Override
	public void testResponseStatus() {
		// deactivate test as OkHttp is a client-only
	}
}
