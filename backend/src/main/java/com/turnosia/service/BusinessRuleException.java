package com.turnosia.service;

public class BusinessRuleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String code;

	public BusinessRuleException(String code, String message) {
		this(code, message, null);
	}

	public BusinessRuleException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
