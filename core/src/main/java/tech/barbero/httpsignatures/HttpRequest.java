/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikaël Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.httpsignatures;

import java.net.URI;

public interface HttpRequest extends HttpMessage {
	HttpRequest addHeader(String name, String value);
	String method();
	URI uri();
}