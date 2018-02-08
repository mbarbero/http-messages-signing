/*******************************************************************************
 * Copyright (c) 2017 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.httpsignatures.ahc4;

import tech.barbero.httpsignatures.MessageFactory;
import tech.barbero.httpsignatures.TestHttpMessageVerifier;

public class TestAHCMessageVerifier extends TestHttpMessageVerifier {

	@Override
	protected MessageFactory createFactory() {
		return new AHCMessageFactory();
	}
}
