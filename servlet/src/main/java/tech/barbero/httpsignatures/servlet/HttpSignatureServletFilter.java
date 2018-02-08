/*******************************************************************************
 * Copyright (c) 2018 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
 *******************************************************************************/
package tech.barbero.httpsignatures.servlet;

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

import tech.barbero.httpsignatures.HttpMessageSignatureVerificationException;
import tech.barbero.httpsignatures.HttpMessageSignatureVerifier;

public class HttpSignatureServletFilter implements Filter {

	private final HttpMessageSignatureVerifier httpMessageSignatureVerifier;
	
	public HttpSignatureServletFilter(HttpMessageSignatureVerifier httpMessageSignatureVerifier) {
		this.httpMessageSignatureVerifier = httpMessageSignatureVerifier;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
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
			if (httpMessageSignatureVerifier.verify(ServletSignedRequest.from(request))) {
				chain.doFilter(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (HttpMessageSignatureVerificationException e) {
			request.getServletContext().log(e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (GeneralSecurityException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
	}

}
