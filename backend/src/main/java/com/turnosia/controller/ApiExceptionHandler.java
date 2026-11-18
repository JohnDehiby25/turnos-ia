package com.turnosia.controller;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.turnosia.dto.ErrorResponse;
import com.turnosia.service.BusinessRuleException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException e) {
		return ResponseEntity.status(statusFor(e.getCode())).body(new ErrorResponse(e.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			ServletRequestBindingException.class })
	public ResponseEntity<ErrorResponse> handleMalformedRequest() {
		return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", "La petición no es válida"));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound() {
		return notFound();
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandlerFound() {
		return notFound();
	}

	private static ResponseEntity<ErrorResponse> notFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse("NOT_FOUND", "La ruta no existe"));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed() {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(new ErrorResponse("METHOD_NOT_ALLOWED", "Ese método no existe para esta ruta"));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType() {
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", "Ese formato de contenido no está soportado"));
	}

	private static HttpStatus statusFor(String code) {
		return switch (code) {
			case "APPOINTMENT_NOT_FOUND", "SERVICE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
			case "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
			case "TOO_MANY_ATTEMPTS" -> HttpStatus.TOO_MANY_REQUESTS;
			case "APPOINTMENT_NOT_AVAILABLE", "APPOINTMENT_NOT_RESCHEDULABLE", "INVALID_STATUS_TRANSITION",
					"CLIENT_AMBIGUOUS", "CLIENT_PHONE_TAKEN" -> HttpStatus.CONFLICT;
			default -> HttpStatus.BAD_REQUEST;
		};
	}
}
