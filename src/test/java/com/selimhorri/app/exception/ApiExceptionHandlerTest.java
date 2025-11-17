package com.selimhorri.app.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.selimhorri.app.exception.payload.ExceptionMsg;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiExceptionHandler Unit Tests")
class ApiExceptionHandlerTest {
	
	@InjectMocks
	private ApiExceptionHandler apiExceptionHandler;
	
	@Test
	@DisplayName("Should handle OrderItemNotFoundException correctly")
	void testHandleOrderItemNotFoundException() {
		// Given
		OrderItemNotFoundException exception = new OrderItemNotFoundException("OrderItem with id: [1, 100] not found");
		
		// When
		ResponseEntity<ExceptionMsg> response = apiExceptionHandler.handleApiRequestException(exception);
		
		// Then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getMsg().contains("OrderItem with id: [1, 100] not found"));
		assertTrue(response.getBody().getMsg().startsWith("####"));
		assertTrue(response.getBody().getMsg().endsWith("####"));
		assertNotNull(response.getBody().getTimestamp());
	}
	
	@Test
	@DisplayName("Should handle OrderItemNotFoundException with custom message")
	void testHandleOrderItemNotFoundException_CustomMessage() {
		// Given
		OrderItemNotFoundException exception = new OrderItemNotFoundException("Invalid order item combination");
		
		// When
		ResponseEntity<ExceptionMsg> response = apiExceptionHandler.handleApiRequestException(exception);
		
		// Then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getMsg().contains("Invalid order item combination"));
	}
	
	@Test
	@DisplayName("Should set correct HTTP status as BAD_REQUEST")
	void testHandleApiRequestException_HttpStatus() {
		// Given
		OrderItemNotFoundException exception = new OrderItemNotFoundException("Test error");
		
		// When
		ResponseEntity<ExceptionMsg> response = apiExceptionHandler.handleApiRequestException(exception);
		
		// Then
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getHttpStatus());
	}
	
	@Test
	@DisplayName("Should include timestamp in exception message")
	void testHandleApiRequestException_Timestamp() {
		// Given
		OrderItemNotFoundException exception = new OrderItemNotFoundException("Timestamp test");
		
		// When
		ResponseEntity<ExceptionMsg> response = apiExceptionHandler.handleApiRequestException(exception);
		
		// Then
		assertNotNull(response.getBody().getTimestamp());
	}
	
	@Test
	@DisplayName("Should format exception message with markers")
	void testHandleApiRequestException_MessageFormat() {
		// Given
		OrderItemNotFoundException exception = new OrderItemNotFoundException("Error message");
		
		// When
		ResponseEntity<ExceptionMsg> response = apiExceptionHandler.handleApiRequestException(exception);
		
		// Then
		String message = response.getBody().getMsg();
		assertEquals("#### Error message! ####", message);
	}
	
}

