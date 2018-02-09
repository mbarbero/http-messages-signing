package tech.barbero.http.message.signing.servlet;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import tech.barbero.http.message.signing.HttpRequest;

class ServletSignedRequest extends HttpServletRequestWrapper implements HttpRequest {

	private ServletSignedRequest(HttpServletRequest request) {
		super(request);
	}
	
	public static HttpRequest from(HttpServletRequest httpServletRequest) {
		return new ServletSignedRequest(httpServletRequest);
	}
	
	public HttpServletRequest getRequest() {
        return (HttpServletRequest) super.getRequest();
    }

	@Override
	public List<String> headerValues(String name) {
		return Collections.list(getRequest().getHeaders(name));
	}

	@Override
	public void addHeader(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String method() {
		return getRequest().getMethod();
	}

	@Override
	public URI uri() {
		return URI.create(getRequest().getRequestURI());
	}
}
