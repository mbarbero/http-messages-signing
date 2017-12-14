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
package tech.barbero.httpsignatures;

import java.util.Iterator;
import java.util.function.Function;

final class Utils {

	private Utils() {
		// prevent instantiation
	}
	
	static String join(Iterator<String> it, String separator, Function<String, String> f) {
		StringBuilder sb = new StringBuilder();
		if (it.hasNext()) {
			sb.append(f.apply(it.next()));
			while (it.hasNext()) {
				sb.append(separator);
				sb.append(f.apply(it.next()));
			}
		}
		return sb.toString();
	}
}
