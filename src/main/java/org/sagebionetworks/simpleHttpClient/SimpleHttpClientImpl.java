package org.sagebionetworks.simpleHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public final class SimpleHttpClientImpl implements SimpleHttpClient{

	private CloseableHttpClient httpClient;
	private StreamProvider provider;

	public SimpleHttpClientImpl() {
		this(null);
	}

	/**
	 * Create a SimpleHttpClient with a new connection pool
	 * 
	 * @param config
	 */
	public SimpleHttpClientImpl(SimpleHttpClientConfig config) {
		if (config == null) {
			httpClient = HttpClients.createDefault();
		} else {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(config.getConnectionRequestTimeoutMs())
					.setConnectTimeout(config.getConnectTimeoutMs())
					.setSocketTimeout(config.getSocketTimeoutMs())
					.build();
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.build();;
		}
		provider = new StreamProviderImpl();
	}

	@Override
	public SimpleHttpResponse get(SimpleHttpRequest request)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		return execute(httpGet);
	}

	@Override
	public SimpleHttpResponse post(SimpleHttpRequest request, String requestBody)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPost httpPost = new HttpPost(request.getUri());
		if (requestBody != null) {
			httpPost.setEntity(new StringEntity(requestBody));
		}
		copyHeaders(request, httpPost);
		return execute(httpPost);
	}

	@Override
	public SimpleHttpResponse put(SimpleHttpRequest request, String requestBody)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPut httpPut = new HttpPut(request.getUri());
		if (requestBody != null) {
			httpPut.setEntity(new StringEntity(requestBody));
		}
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public SimpleHttpResponse delete(SimpleHttpRequest request)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpDelete httpDelete = new HttpDelete(request.getUri());
		copyHeaders(request, httpDelete);
		return execute(httpDelete);
	}

	@Override
	public SimpleHttpResponse putFile(SimpleHttpRequest request, File toUpload)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		if (toUpload == null) {
			throw new IllegalArgumentException("toUpload cannot be null");
		}
		HttpPut httpPut = new HttpPut(request.getUri());
		httpPut.setEntity(new FileEntity(toUpload));
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public SimpleHttpResponse getFile(SimpleHttpRequest request, File result)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		if (result == null) {
			throw new IllegalArgumentException("result cannot be null");
		}
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		CloseableHttpResponse response = null;
		FileOutputStream fileOutputStream = provider.getFileOutputStream(result);
		try {
			response = httpClient.execute(httpGet);
			if (response.getEntity() != null) {
				response.getEntity().writeTo(fileOutputStream);
			}
			SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse();
			simpleHttpResponse.setStatusCode(response.getStatusLine().getStatusCode());
			simpleHttpResponse.setStatusReason(response.getStatusLine().getReasonPhrase());
			simpleHttpResponse.setHeaders(response.getAllHeaders());
			return simpleHttpResponse;
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			if (response != null) {
				response.close();
			}
		}
	}

	/**
	 * Validates a SimpleHttpRequest and throw exception if any required field is null
	 * 
	 * @param request
	 */
	public static void validateSimpleHttpRequest(SimpleHttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		}
		if (request.getUri() == null) {
			throw new IllegalArgumentException("SimpleHttpRequest.uri cannot be null");
		}
	}

	/**
	 * Copies the headers from a SimpleHttpRequest to a HttpUriRequest
	 * 
	 * @param request
	 * @param httpUriRequest
	 */
	public static void copyHeaders(SimpleHttpRequest request, HttpUriRequest httpUriRequest) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		}
		if (httpUriRequest == null) {
			throw new IllegalArgumentException("httpUriRequest cannot be null");
		}
		Map<String, String> headers = request.getHeaders();
		if (headers != null) {
			for (String name : headers.keySet()) {
				httpUriRequest.addHeader(name, headers.get(name));
			}
		}
	}

	/**
	 * Performs the request, then consume the response to build a simpleHttpResponse
	 * 
	 * @param httpUriRequest
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	protected SimpleHttpResponse execute(HttpUriRequest httpUriRequest)
			throws IOException, ClientProtocolException {
		if (httpUriRequest == null) {
			throw new IllegalArgumentException("httpUriRequest cannot be null");
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpUriRequest);
			SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse();
			simpleHttpResponse.setStatusCode(response.getStatusLine().getStatusCode());
			simpleHttpResponse.setStatusReason(response.getStatusLine().getReasonPhrase());
			simpleHttpResponse.setHeaders(response.getAllHeaders());
			if (response.getEntity() != null) {
				simpleHttpResponse.setContent(EntityUtils.toString(response.getEntity()));
			}
			return simpleHttpResponse;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	protected void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	protected void setStreamProvider(StreamProvider provider) {
		this.provider = provider;
	}
}
