package org.sagebionetworks.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.ContentType;

public class ContentTypeUtil {
	public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create("text/plain", StandardCharsets.UTF_8);
	public static final ContentType TEXT_HTML_UTF8 = ContentType.create("text/html", StandardCharsets.UTF_8);

	/**
	 * 
	 * @param contentTypeString
	 * @return the Charset indicated by the given string or null if no Charset is indicated
	 */
	public static Charset getCharsetFromContentTypeString(String contentTypeString) {
		ContentType contentType = contentTypeString==null ? null : ContentType.parse(contentTypeString);
		return contentType==null ? null : contentType.getCharset();
	}

}
