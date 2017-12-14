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

import java.util.List;
import java.util.Objects;

import tech.barbero.httpsignatures.HttpSignature.Builder;

abstract class AbstractHttpSignatureBuilder<B extends AbstractHttpSignatureBuilder<B, S>, S extends HttpSignature> implements Builder {

	public abstract B keyId(String keyId);
	
	abstract B headersToSign(List<String> headers);
	abstract List<String> headersToSign();

	@Override
	public AbstractHttpSignatureBuilder<B, S> addHeaderToSign(String header) {
		headersToSign().add(Objects.requireNonNull(header));
		return this;
	}

	abstract S autoBuild();
	
	public S build() {
		S ret = autoBuild();
		if (!headersToSign().isEmpty() && headersToSign().stream().noneMatch(h -> "date".equals(h.toLowerCase()))) {
			throw new IllegalStateException("Headers to sign must contain 'date' header");
		}
		ret.algorithm(); // check supported algorithm name
		return ret;
	}

}
