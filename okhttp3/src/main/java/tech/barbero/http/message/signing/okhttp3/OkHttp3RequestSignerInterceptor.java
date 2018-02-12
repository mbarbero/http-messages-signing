package tech.barbero.http.message.signing.okhttp3;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.Response;
import tech.barbero.http.message.signing.HttpMessageSigner;

public class OkHttp3RequestSignerInterceptor implements Interceptor {

	private final HttpMessageSigner messageSigner;

	public OkHttp3RequestSignerInterceptor(HttpMessageSigner messageSigner) {
		this.messageSigner = Objects.requireNonNull(messageSigner);
	}
	
	@Override
	public Response intercept(Chain chain) throws IOException {
		OkHttp3RequestWrapper request = OkHttp3RequestWrapper.from(chain.request());
		try {
			messageSigner.sign(request);
			return chain.proceed(request.delegate());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Can't sign HTTP message '" + chain.request() + "'", e);
		}
	}

}
