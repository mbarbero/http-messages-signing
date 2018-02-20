/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tech.barbero.http.message.signing.servlet;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tech.barbero.http.message.signing.SignatureHeaderVerifier;

/**
 * A simple servlet filter that send an HTTP 401 unauthorized status code if the signature is not recognized.
 */
public class SignatureHeaderVerifierServletFilter implements Filter {

	private final SignatureHeaderVerifier signatureVerifier;

	/**
	 * Creates a new {@code SignatureHeaderVerifierServletFilter} which will check HTTP request signatures with the given
	 * {@link SignatureHeaderVerifier signatureVerifier}.
	 *
	 * @param signatureVerifier
	 *          The signature verifier which will check HTTP request signature.
	 */
	public SignatureHeaderVerifierServletFilter(SignatureHeaderVerifier signatureVerifier) {
		this.signatureVerifier = signatureVerifier;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// no configuration for this Filter.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
		} else {
			throw new ServletException("ServletRequest and ServletResponse should be respectively instance of HttpServletRequest and HttpServletResponse");
		}
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			if (this.signatureVerifier.verify(ServletSignedRequest.from(request))) {
				chain.doFilter(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (GeneralSecurityException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		// we don't hold any resource, no need to release/destroy anything.
	}

}
