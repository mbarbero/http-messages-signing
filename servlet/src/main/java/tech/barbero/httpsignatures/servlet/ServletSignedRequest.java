package tech.barbero.httpsignatures.servlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import tech.barbero.httpsignatures.HttpRequest;

class ServletSignedRequest extends HttpServletRequestWrapper implements HttpRequest {

	private ServletSignedRequest(HttpServletRequest request) {
		super(request);
	}
	
	public static HttpRequest from(HttpServletRequest httpServletRequest) {
		return new ServletSignedRequest(httpServletRequest);
	}
	
	private HttpServletRequest getServletRequest() {
        return (HttpServletRequest) super.getRequest();
    }

	@Override
	public List<String> headerValues(String name) {
		return Collections.list(getServletRequest().getHeaders(name));
	}

	@Override
	public HttpRequest addHeader(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String method() {
		return getServletRequest().getMethod();
	}

	@Override
	public URI uri() {
		try {
			return new URI(getServletRequest().getRequestURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
