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
package tech.barbero.httpsignatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class HttpSignatureHeaderElements {

	HttpSignatureHeaderElements() {}
	
	public abstract String keyId();
	public abstract String algorithm();
	public abstract List<String> headers();
	public abstract String signature();
		
	public static HttpSignatureHeaderElements fromHeadersList(List<String> headers) {
		Builder builder = HttpSignatureHeaderElements.builder();
		headers.forEach(header -> builder.parse(header));
		return builder.build();
	}

	public static Builder builder() {
		return new AutoValue_HttpSignatureHeaderElements.Builder().headers(new ArrayList<>());
	}

	@AutoValue.Builder
	static abstract class Builder {
		abstract Builder keyId(String keyId);
		abstract Builder algorithm(String algorithm);
		abstract Builder headers(List<String> headers);
		abstract List<String> headers();
		abstract Builder signature(String signature);
		abstract HttpSignatureHeaderElements build();
		
		Builder parse(String header) {
			HeaderElement[] elements = BasicHeaderValueParser.parseElements(header, BasicHeaderValueParser.INSTANCE);
			for (HeaderElement element : elements) {
				String value = element.getValue();
				switch(element.getName()) {
					case HttpSignature.PARAM_KEY_ID:
						keyId(value);
						break;
					case HttpSignature.PARAM_ALGORITHM:
						algorithm(value);
						break;
					case HttpSignature.PARAM_HEADERS:
						headers().addAll(Arrays.asList(value.split(" +")));
						break;
					case HttpSignature.PARAM_SIGNATURE:
						signature(value);
						break;
					default:
						// ignore unknown header element names
						break;
				}
			}
			return this;
		}
	}
}
