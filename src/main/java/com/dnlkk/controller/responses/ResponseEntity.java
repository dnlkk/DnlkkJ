package com.dnlkk.controller.responses;

import java.util.HashMap;
import java.util.Map;

import com.dnlkk.controller.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class ResponseEntity<T> {
	private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> httpHeaders;
    private final T body;
	private final HttpStatus status;
    
    public String json() {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public ResponseEntity() {
		this(null, null, HttpStatus.OK);
	}
	public ResponseEntity(T body) {
		this(body, null, HttpStatus.OK);
	}
	public ResponseEntity(Map<String, String> headers) {
		this(null, headers);
	}
    
	public ResponseEntity(T body, Map<String, String> headers) {
		this.body = body;
		this.httpHeaders = headers != null ? headers : new HashMap<>();
        this.status = HttpStatus.OK;
	}

	public ResponseEntity(T body, HttpStatus httpStatus) {
		this(body, null, httpStatus);
	}
	public ResponseEntity(Map<String, String> headers, HttpStatus httpStatus) {
		this(null, headers, httpStatus);
    }
    
	public ResponseEntity(T body, Map<String, String> headers, HttpStatus httpStatus) {
		this.body = body;
		this.httpHeaders = headers != null ? headers : new HashMap<>();
        this.status = httpStatus;
	}

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
    public static <T> ResponseEntity<T> noContent(T body) {
        return new ResponseEntity<>(body, HttpStatus.NO_CONTENT);
    }
    public static <T> ResponseEntity<T> bad(T body) {
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    public static <T> ResponseEntity<T> notFound(T body) {
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}