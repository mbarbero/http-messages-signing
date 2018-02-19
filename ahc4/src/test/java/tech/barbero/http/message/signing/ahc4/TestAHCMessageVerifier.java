/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.ahc4;

import tech.barbero.http.message.signing.MessageFactory;
import tech.barbero.http.message.signing.TestHttpMessageVerifier;

public class TestAHCMessageVerifier extends TestHttpMessageVerifier {

	@Override
	protected MessageFactory createFactory() {
		return new AHCMessageFactory();
	}
}
