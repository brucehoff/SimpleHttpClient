package org.sagebionetworks.simpleHttpClient;

import java.util.Arrays;

import org.apache.http.Header;

/**
 * This object represents a simple HttpResponse.
 * 
 * A SimpleHttpResponse only keeps information about the status code, status reason, and the content of the response.
 * 
 * This should only be used for responses whose content fits in memory.
 * 
 * @author kimyentruong
 *
 */
public class SimpleHttpResponse {

	private int statusCode;
	private String statusReason;
	private String content;
	private Header[] headers;

	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusReason() {
		return statusReason;
	}
	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Header[] getHeaders() {
		return headers;
	}
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + Arrays.hashCode(headers);
		result = prime * result + statusCode;
		result = prime * result + ((statusReason == null) ? 0 : statusReason.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleHttpResponse other = (SimpleHttpResponse) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (!Arrays.equals(headers, other.headers))
			return false;
		if (statusCode != other.statusCode)
			return false;
		if (statusReason == null) {
			if (other.statusReason != null)
				return false;
		} else if (!statusReason.equals(other.statusReason))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SimpleHttpResponse [statusCode=" + statusCode + ", statusReason=" + statusReason + ", content="
				+ content + ", headers=" + Arrays.toString(headers) + "]";
	}
}
