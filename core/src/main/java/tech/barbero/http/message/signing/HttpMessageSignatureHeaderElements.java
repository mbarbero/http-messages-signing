/*******************************************************************************
 * Copyright (c) 2017 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;

import tech.barbero.http.message.signing.HttpMessageSigner.Algorithm;

@AutoValue
public abstract class HttpMessageSignatureHeaderElements {

	HttpMessageSignatureHeaderElements() { }

	public abstract String keyId();
	public abstract Algorithm algorithm();
	public abstract List<String> signedHeaders();
	public abstract String signature();

	public static HttpMessageSignatureHeaderElements fromHeaderValue(String header) {
		return HttpMessageSignatureHeaderElements.builder().parse(header).build();
	}

	public static HttpMessageSignatureHeaderElements fromHeaderValuesList(List<String> headers) {
		Builder builder = HttpMessageSignatureHeaderElements.builder();
		headers.forEach(header -> builder.parse(header));
		return builder.build();
	}

	static Builder builder() {
		return new AutoValue_HttpMessageSignatureHeaderElements.Builder().signedHeaders(new ArrayList<>());
	}

	@AutoValue.Builder
	abstract static class Builder {
		private static final BasicHeaderValueParser HEADER_VALUE_PARSER = new BasicHeaderValueParser();
		private static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

		abstract Builder keyId(String keyId);
		abstract Builder algorithm(Algorithm algorithm);
		abstract Builder signedHeaders(List<String> headers);
		abstract List<String> signedHeaders();
		abstract Builder signature(String signature);
		abstract HttpMessageSignatureHeaderElements autoBuild();

		HttpMessageSignatureHeaderElements build() {
			if (signedHeaders().isEmpty()) {
				signedHeaders().add(normalizeHeader(HttpMessageSigner.HEADER_DATE));
			}
			return autoBuild();
		}

		Builder parse(String headerValue) {
			HeaderElement[] elements = BasicHeaderValueParser.parseElements(headerValue, HEADER_VALUE_PARSER);
			for (HeaderElement element : elements) {
				parseHeaderElement(element);
			}
			return this;
		}

		private void parseHeaderElement(HeaderElement element) {
			String elementValue = element.getValue();
			switch (element.getName()) {
				case HttpMessageSigner.PARAM_KEY_ID:
					keyId(elementValue);
					break;
				case HttpMessageSigner.PARAM_ALGORITHM:
					Optional<Algorithm> alg = Algorithm.findFirst(a -> elementValue.equals(a.algorithmName()));
					if (alg.isPresent()) {
						algorithm(alg.get());
						break;
					}
					throw new IllegalStateException("Unsupported algorithm '" + elementValue + "'");
				case HttpMessageSigner.PARAM_HEADERS:
					signedHeaders(SPLITTER.splitToList(elementValue).stream()
							.map(Builder::normalizeHeader)
							.distinct()
							.collect(Collectors.toList()));
					break;
				case HttpMessageSigner.PARAM_SIGNATURE:
					signature(elementValue);
					break;
				default:
					// ignore unknown header element names
					break;
			}
		}

		static String normalizeHeader(String header) {
			return header.toLowerCase();
		}
	}
}
