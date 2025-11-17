package com.selimhorri.app.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.OrderItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas de Integración para OrderItemService
 * Estas pruebas usan la base de datos real (H2 en memoria)
 * y prueban la integración completa entre capas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderItem Service Integration Tests")
class OrderItemServiceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		// Clean database before each test
		orderItemRepository.deleteAll();

		// Mock RestTemplate responses for external service calls
		ProductDto mockProductDto = ProductDto.builder()
				.productId(100)
				.productTitle("Test Product")
				.priceUnit(99.99)
				.build();

		OrderDto mockOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDesc("Test Order")
				.orderFee(99.99)
				.build();

		when(restTemplate.getForObject(any(String.class), eq(ProductDto.class)))
				.thenReturn(mockProductDto);
		when(restTemplate.getForObject(any(String.class), eq(OrderDto.class)))
				.thenReturn(mockOrderDto);
	}

	@Test
	@DisplayName("Should create order item successfully via REST API")
	void testCreateOrderItem_Success() throws Exception {
		// Given
		OrderItemDto orderItemDto = OrderItemDto.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.productDto(ProductDto.builder().productId(100).build())
				.orderDto(OrderDto.builder().orderId(1).build())
				.build();

		// When & Then
		mockMvc.perform(post("/api/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderItemDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.productId").value(100))
				.andExpect(jsonPath("$.orderedQuantity").value(5));

		// Verify it was saved in database
		assertTrue(orderItemRepository.count() > 0);
	}

	@Test
	@DisplayName("Should retrieve order item by id via REST API")
	void testGetOrderItemById_Success() throws Exception {
		// Given
		OrderItem savedOrderItem = createOrderItemInDatabase();

		// When & Then
		mockMvc.perform(get("/api/shippings/{orderId}/{productId}",
				savedOrderItem.getOrderId(),
				savedOrderItem.getProductId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(savedOrderItem.getOrderId()))
				.andExpect(jsonPath("$.productId").value(savedOrderItem.getProductId()))
				.andExpect(jsonPath("$.product").exists())
				.andExpect(jsonPath("$.order").exists());
	}

	@Test
	@DisplayName("Should retrieve all order items via REST API")
	void testGetAllOrderItems_Success() throws Exception {
		// Given - Create multiple order items
		OrderItem orderItem1 = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();
		orderItemRepository.save(orderItem1);

		OrderItem orderItem2 = OrderItem.builder()
				.orderId(2)
				.productId(200)
				.orderedQuantity(10)
				.build();
		orderItemRepository.save(orderItem2);

		// When & Then
		mockMvc.perform(get("/api/shippings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection").isArray())
				.andExpect(jsonPath("$.collection.length()").value(2));
	}

	@Test
	@DisplayName("Should update order item successfully via REST API")
	void testUpdateOrderItem_Success() throws Exception {
		// Given
		OrderItem savedOrderItem = createOrderItemInDatabase();

		OrderItemDto updatedOrderItemDto = OrderItemDto.builder()
				.orderId(savedOrderItem.getOrderId())
				.productId(savedOrderItem.getProductId())
				.orderedQuantity(15)
				.productDto(ProductDto.builder().productId(savedOrderItem.getProductId()).build())
				.orderDto(OrderDto.builder().orderId(savedOrderItem.getOrderId()).build())
				.build();

		// When & Then
		mockMvc.perform(put("/api/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updatedOrderItemDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(savedOrderItem.getOrderId()))
				.andExpect(jsonPath("$.productId").value(savedOrderItem.getProductId()))
				.andExpect(jsonPath("$.orderedQuantity").value(15));
	}

	@Test
	@DisplayName("Should delete order item successfully via REST API")
	void testDeleteOrderItem_Success() throws Exception {
		// Given
		OrderItem savedOrderItem = createOrderItemInDatabase();

		// When & Then
		mockMvc.perform(delete("/api/shippings/{orderId}/{productId}",
				savedOrderItem.getOrderId(),
				savedOrderItem.getProductId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		// Verify it was deleted from database
		OrderItemId orderItemId = new OrderItemId(
				savedOrderItem.getProductId(),
				savedOrderItem.getOrderId());
		assertTrue(orderItemRepository.findById(orderItemId).isEmpty());
	}

	@Test
	@DisplayName("Should return 400 error when order item not found")
	void testGetOrderItemById_NotFound() throws Exception {
		// When & Then
		mockMvc.perform(get("/api/shippings/{orderId}/{productId}", 999, 999))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should persist order item with correct composite key")
	void testOrderItemCompositeKey() throws Exception {
		// Given
		OrderItemDto orderItemDto = OrderItemDto.builder()
				.orderId(5)
				.productId(500)
				.orderedQuantity(20)
				.productDto(ProductDto.builder().productId(500).build())
				.orderDto(OrderDto.builder().orderId(5).build())
				.build();

		// When
		String response = mockMvc.perform(post("/api/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderItemDto)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		OrderItemDto result = objectMapper.readValue(response, OrderItemDto.class);

		// Then - Verify composite key in database
		OrderItemId orderItemId = new OrderItemId(
				result.getProductId(),
				result.getOrderId());
		OrderItem dbOrderItem = orderItemRepository.findById(orderItemId).orElseThrow();
		assertNotNull(dbOrderItem);
		assertEquals(5, dbOrderItem.getOrderId());
		assertEquals(500, dbOrderItem.getProductId());
		assertEquals(20, dbOrderItem.getOrderedQuantity());
	}

	@Test
	@DisplayName("Should retrieve order items with product and order information")
	void testGetOrderItemsWithProductAndOrder() throws Exception {
		// Given
		createOrderItemInDatabase();

		// When & Then
		mockMvc.perform(get("/api/shippings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection[0].product").exists())
				.andExpect(jsonPath("$.collection[0].product.productId").exists())
				.andExpect(jsonPath("$.collection[0].order").exists())
				.andExpect(jsonPath("$.collection[0].order.orderId").exists());
	}

	@Test
	@DisplayName("Should handle multiple order items for same order")
	void testMultipleOrderItemsForSameOrder() throws Exception {
		// Given
		OrderItem orderItem1 = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();

		OrderItem orderItem2 = OrderItem.builder()
				.orderId(1)
				.productId(200)
				.orderedQuantity(10)
				.build();

		orderItemRepository.save(orderItem1);
		orderItemRepository.save(orderItem2);

		// When & Then
		mockMvc.perform(get("/api/shippings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.collection.length()").value(2))
				.andExpect(jsonPath("$.collection[0].orderId").value(1))
				.andExpect(jsonPath("$.collection[1].orderId").value(1));
	}

	/**
	 * Helper method to create an order item in the database
	 */
	private OrderItem createOrderItemInDatabase() {
		OrderItem orderItem = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();

		return orderItemRepository.save(orderItem);
	}

}

