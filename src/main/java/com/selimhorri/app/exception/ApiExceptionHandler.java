package com.selimhorri.app.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.selimhorri.app.exception.payload.ExceptionMsg;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {
	
	@ExceptionHandler(value = {
		MethodArgumentNotValidException.class,
		HttpMessageNotReadableException.class,
	})
	public <T extends BindException> ResponseEntity<ExceptionMsg> handleValidationException(final T e) {
		
		log.info("**ApiExceptionHandler controller, handle validation exception*\n");
		final var badRequest = HttpStatus.BAD_REQUEST;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("*" + e.getBindingResult().getFieldError().getDefaultMessage() + "!**")
					.httpStatus(badRequest)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badRequest);
	}
	
	@ExceptionHandler(value = {
		IllegalStateException.class,
		OrderItemNotFoundException.class,
	})
	public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleApiRequestException(final T e) {
		
		log.info("**ApiExceptionHandler controller, handle API request*\n");
		final var badRequest = HttpStatus.BAD_REQUEST;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### " + e.getMessage() + "! ####")
					.httpStatus(badRequest)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badRequest);
	}
	
	@ExceptionHandler(value = {
		DataIntegrityViolationException.class,
	})
	public ResponseEntity<ExceptionMsg> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
		
		log.info("**ApiExceptionHandler controller, handle data integrity violation*\n");
		final var conflict = HttpStatus.CONFLICT;
		
		// Verificar si es un error de clave duplicada
		String errorMessage = e.getMessage();
		if (errorMessage != null && (errorMessage.contains("Duplicate") ||
		                             errorMessage.contains("PRIMARY KEY") ||
		                             errorMessage.contains("unique constraint"))) {
			errorMessage = "OrderItem already exists with the same orderId and productId";
		} else {
			errorMessage = "Data integrity violation: " + (errorMessage != null ? errorMessage : "Unknown error");
		}
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### " + errorMessage + "! ####")
					.httpStatus(conflict)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), conflict);
	}
	
	@ExceptionHandler(value = {
		ResourceAccessException.class,
	})
	public ResponseEntity<ExceptionMsg> handleResourceAccessException(final ResourceAccessException e) {
		
		log.error("**ApiExceptionHandler controller, handle resource access exception (timeout/connection error)*\n", e);
		final var serviceUnavailable = HttpStatus.SERVICE_UNAVAILABLE;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### Service temporarily unavailable. Please try again later. ####")
					.httpStatus(serviceUnavailable)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), serviceUnavailable);
	}
	
	@ExceptionHandler(value = {
		HttpServerErrorException.class,
	})
	public ResponseEntity<ExceptionMsg> handleHttpServerErrorException(final HttpServerErrorException e) {
		
		log.error("**ApiExceptionHandler controller, handle HTTP server error from external service*\n", e);
		final var badGateway = HttpStatus.BAD_GATEWAY;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### External service error. Please try again later. ####")
					.httpStatus(badGateway)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badGateway);
	}
	
	@ExceptionHandler(value = {
		HttpClientErrorException.class,
	})
	public ResponseEntity<ExceptionMsg> handleHttpClientErrorException(final HttpClientErrorException e) {
		
		log.error("**ApiExceptionHandler controller, handle HTTP client error from external service*\n", e);
		final var badGateway = HttpStatus.BAD_GATEWAY;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### External service returned an error. Please try again later. ####")
					.httpStatus(badGateway)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), badGateway);
	}
	
	@ExceptionHandler(value = {
		Exception.class,
	})
	public ResponseEntity<ExceptionMsg> handleGenericException(final Exception e) {
		
		log.error("**ApiExceptionHandler controller, handle generic exception*\n", e);
		final var internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
		
		return new ResponseEntity<>(
				ExceptionMsg.builder()
					.msg("#### An unexpected error occurred. Please try again later. ####")
					.httpStatus(internalServerError)
					.timestamp(ZonedDateTime
							.now(ZoneId.systemDefault()))
					.build(), internalServerError);
	}
	
	
	
	
}
