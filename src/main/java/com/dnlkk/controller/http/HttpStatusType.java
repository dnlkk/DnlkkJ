package com.dnlkk.controller.http;

public enum HttpStatusType {
	INFORMATIONAL(1),
	SUCCESSFUL(2),
	REDIRECTION(3),
	CLIENT_ERROR(4),
	SERVER_ERROR(5);

	private final int classCode;

	HttpStatusType(int value) {
		this.classCode = value;
	}
    
	public static HttpStatusType valueOf(int statusCode) {
		HttpStatusType series = resolve(statusCode);
		if (series == null) {
			throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
		}
		return series;
	}

	public static HttpStatusType resolve(int statusCode) {
		int seriesCode = statusCode / 100;
		for (HttpStatusType series : values()) {
			if (series.classCode == seriesCode) {
				return series;
			}
		}
		return null;
	}
}