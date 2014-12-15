package com.codencare.snipper.util;

import java.util.regex.Pattern;

public interface ProtocolSignature {
	final String YAHOO_MESSENGER = "YMSG";
	final String VALID_HTTP_RESPONSE_PATTERN = "(?i)http/\\d\\.\\d 200 OK(.|\\s)*";
	final String HTTP_CONTENT_TYPE_PATTERN = "(?i)Content\\-Type";
	final String HTTP_CONTENT_ENCODING_PATTERN = "(?i)Content\\-Encoding";
	final String HTTP_NOT_COMPRESSED_PATTERN = "\\b(?:(?!((?i)gzip|(?i)sdch))\\w)\\b";
	final String HTTP_COMPRESSED_PATTERN = "\\b(((?i)gzip|(?i)sdch)\\b)";
        final String HTTP_TRANSFER_ENCODING = "(?i)Transfer\\-Encoding";
}
