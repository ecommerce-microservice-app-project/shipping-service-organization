package com.selimhorri.app.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.dto.OrderItemDto;

@DisplayName("OrderItemMappingHelper Unit Tests")
class OrderItemMappingHelperTest {
	
	private OrderItem testOrderItem;
	private OrderItemDto testOrderItemDto;
	
	@BeforeEach
	void setUp() {
		testOrderItem = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();
		
		testOrderItemDto = OrderItemDto.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();
	}
	
	@Test
	@DisplayName("Should map OrderItem to OrderItemDto successfully")
	void testMapOrderItemToDto_Success() {
		// When
		OrderItemDto result = OrderItemMappingHelper.map(testOrderItem);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		assertEquals(100, result.getProductId());
		assertEquals(5, result.getOrderedQuantity());
		assertNotNull(result.getProductDto());
		assertEquals(100, result.getProductDto().getProductId());
		assertNotNull(result.getOrderDto());
		assertEquals(1, result.getOrderDto().getOrderId());
	}
	
	@Test
	@DisplayName("Should map OrderItemDto to OrderItem successfully")
	void testMapDtoToOrderItem_Success() {
		// When
		OrderItem result = OrderItemMappingHelper.map(testOrderItemDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		assertEquals(100, result.getProductId());
		assertEquals(5, result.getOrderedQuantity());
	}
	
	@Test
	@DisplayName("Should maintain bidirectional mapping consistency")
	void testBidirectionalMapping_Consistency() {
		// When - OrderItem to DTO and back
		OrderItemDto mappedDto = OrderItemMappingHelper.map(testOrderItem);
		OrderItem mappedBackOrderItem = OrderItemMappingHelper.map(mappedDto);
		
		// Then
		assertEquals(testOrderItem.getOrderId(), mappedBackOrderItem.getOrderId());
		assertEquals(testOrderItem.getProductId(), mappedBackOrderItem.getProductId());
		assertEquals(testOrderItem.getOrderedQuantity(), mappedBackOrderItem.getOrderedQuantity());
	}
	
	@Test
	@DisplayName("Should map OrderItem with different quantity correctly")
	void testMapOrderItemWithDifferentQuantity() {
		// Given
		OrderItem orderItemWithDifferentQuantity = OrderItem.builder()
				.orderId(2)
				.productId(200)
				.orderedQuantity(10)
				.build();
		
		// When
		OrderItemDto result = OrderItemMappingHelper.map(orderItemWithDifferentQuantity);
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.getOrderId());
		assertEquals(200, result.getProductDto().getProductId());
		assertEquals(10, result.getOrderedQuantity());
	}
	
	@Test
	@DisplayName("Should map OrderItemDto with different quantity correctly")
	void testMapDtoWithDifferentQuantity() {
		// Given
		OrderItemDto orderItemDtoWithDifferentQuantity = OrderItemDto.builder()
				.orderId(3)
				.productId(300)
				.orderedQuantity(15)
				.build();
		
		// When
		OrderItem result = OrderItemMappingHelper.map(orderItemDtoWithDifferentQuantity);
		
		// Then
		assertNotNull(result);
		assertEquals(3, result.getOrderId());
		assertEquals(300, result.getProductId());
		assertEquals(15, result.getOrderedQuantity());
	}
	
}

