package tech.barbero.httpsignatures;

import java.net.URI;

public interface MessageFactory {

	HttpRequest createRequest(String method, URI uri);
	
	HttpResponse createResponse(int statusCode);
	
	class MockImpl implements MessageFactory {

		@Override
		public HttpRequest createRequest(String method, URI uri) {
			return new RequestMock(method, uri);
		}

		@Override
		public HttpResponse createResponse(int statusCode) {
			return new ResponseMock(statusCode);
		}
		
	}
}
