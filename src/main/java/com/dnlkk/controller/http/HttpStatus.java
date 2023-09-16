package com.dnlkk.controller.http;

public enum HttpStatus implements HttpStatusCode {
    CONTINUE(100, HttpStatusType.INFORMATIONAL, "Continue"),
    OK(200, HttpStatusType.SUCCESSFUL, "OK"),
    CREATED(201, HttpStatusType.SUCCESSFUL, "Created"),
    ACCEPTED(202, HttpStatusType.SUCCESSFUL, "Accepted"),
    NO_CONTENT(204, HttpStatusType.SUCCESSFUL, "No Content"),
    MULTIPLE_CHOICES(300, HttpStatusType.REDIRECTION, "Multiple Choices"),
    MOVED_PERMANENTLY(301, HttpStatusType.REDIRECTION, "Moved Permanently"),
    FOUND(302, HttpStatusType.REDIRECTION, "Found"),
    SEE_OTHER(303, HttpStatusType.REDIRECTION, "See Other"),
    NOT_MODIFIED(304, HttpStatusType.REDIRECTION, "Not Modified"),
    TEMPORARY_REDIRECT(307, HttpStatusType.REDIRECTION, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, HttpStatusType.REDIRECTION, "Permanent Redirect"),
    BAD_REQUEST(400, HttpStatusType.CLIENT_ERROR, "Bad Request"),
    UNAUTHORIZED(401, HttpStatusType.CLIENT_ERROR, "Unauthorized"),
    PAYMENT_REQUIRED(402, HttpStatusType.CLIENT_ERROR, "Payment Required"),
    FORBIDDEN(403, HttpStatusType.CLIENT_ERROR, "Forbidden"),
    NOT_FOUND(404, HttpStatusType.CLIENT_ERROR, "Not Found"),
    METHOD_NOT_ALLOWED(405, HttpStatusType.CLIENT_ERROR, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, HttpStatusType.CLIENT_ERROR, "Not Acceptable"),
    REQUEST_TIMEOUT(408, HttpStatusType.CLIENT_ERROR, "Request Timeout"),
    CONFLICT(409, HttpStatusType.CLIENT_ERROR, "Conflict"),
    I_AM_A_TEAPOT(418, HttpStatusType.CLIENT_ERROR, "I'm a teapot"),
    TOO_MANY_REQUESTS(429, HttpStatusType.CLIENT_ERROR, "Too Many Requests"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, HttpStatusType.CLIENT_ERROR, "Unavailable For Legal Reasons"),
    DNLKK_TRIED_SO_HARD(474, HttpStatusType.CLIENT_ERROR, "Dnlkk thinks that that's something strange"),
    INTERNAL_SERVER_ERROR(500, HttpStatusType.SERVER_ERROR, "Internal Server Error"),
    NOT_IMPLEMENTED(501, HttpStatusType.SERVER_ERROR, "Not Implemented"),
    BAD_GATEWAY(502, HttpStatusType.SERVER_ERROR, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, HttpStatusType.SERVER_ERROR, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, HttpStatusType.SERVER_ERROR, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, HttpStatusType.SERVER_ERROR, "HTTP Version not supported"),
    NETWORK_AUTHENTICATION_REQUIRED(511, HttpStatusType.SERVER_ERROR, "Network Authentication Required");

	private static final HttpStatus[] CODES;

	static {
		CODES = values();
	}


	private final int code;
	private final HttpStatusType httpStatusType;
	private final String reasonPhrase;

	HttpStatus(int code, HttpStatusType httpStatusType, String reasonPhrase) {
		this.code = code;
		this.httpStatusType = httpStatusType;
		this.reasonPhrase = reasonPhrase;
	}


    @Override
    public int code() {
		return this.code;
    }
    
	public HttpStatusType getStatusType() {
		return this.httpStatusType;
	}

	public String getReasonPhrase() {
		return this.reasonPhrase;
	}


	@Override
	public boolean isInformational() {
		return (getStatusType() == HttpStatusType.INFORMATIONAL);
	}

	@Override
	public boolean isSuccessful() {
		return (getStatusType() == HttpStatusType.SUCCESSFUL);
	}

	@Override
	public boolean isRedirection() {
		return (getStatusType() == HttpStatusType.REDIRECTION);
	}

	@Override
	public boolean isClientError() {
		return (getStatusType() == HttpStatusType.CLIENT_ERROR);
	}

	@Override
	public boolean isServerError() {
		return (getStatusType() == HttpStatusType.SERVER_ERROR);
	}

	@Override
	public boolean isError() {
		return (isClientError() || isServerError());
	}

	public String toString() {
		return this.code + " " + name();
	}

    
	public static HttpStatus valueOf(int statusCode) {
		HttpStatus status = resolve(statusCode);
		if (status == null) {
			throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
		}
		return status;
	}
    
	public static HttpStatus resolve(int statusCode) {
		// Use cached VALUES instead of values() to prevent array allocation.
		for (HttpStatus status : CODES) {
			if (status.code == statusCode) {
				return status;
			}
		}
		return null;
	}
}