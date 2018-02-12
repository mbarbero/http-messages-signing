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
package tech.barbero.http.message.signing.ahc4;

import tech.barbero.http.message.signing.MessageFactory;
import tech.barbero.http.message.signing.TestHttpMessageSigner;

public class TestAHCMessageSigner extends TestHttpMessageSigner {

	@Override
	protected MessageFactory createFactory() {
		return new AHCMessageFactory();
	}
	
}
