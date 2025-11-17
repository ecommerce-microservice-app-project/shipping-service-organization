package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderItemServiceImpl Unit Tests")
class OrderItemServiceImplTest {
	
	@Mock
	private OrderItemRepository orderItemRepository;
	
	@Mock
	private RestTemplate restTemplate;
	
	@InjectMocks
	private OrderItemServiceImpl orderItemService;
	
	private OrderItem testOrderItem;
	private OrderItemDto testOrderItemDto;
	private OrderItemId testOrderItemId;
	private ProductDto testProductDto;
	private OrderDto testOrderDto;
	
	@BeforeEach
	void setUp() {
		testOrderItemId = new OrderItemId(1, 100);
		
		testProductDto = ProductDto.builder()
				.productId(100)
				.productTitle("Test Product")
				.priceUnit(99.99)
				.build();
		
		testOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDesc("Test Order")
				.orderFee(99.99)
				.build();
		
		testOrderItem = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.build();
		
		testOrderItemDto = OrderItemDto.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(5)
				.productDto(testProductDto)
				.orderDto(testOrderDto)
				.build();
	}
	
	@Test
	@DisplayName("Should find all order items successfully")
	void testFindAll_Success() {
		// Given
		List<OrderItem> orderItems = Arrays.asList(testOrderItem);
		when(orderItemRepository.findAll()).thenReturn(orderItems);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		
		// When
		List<OrderItemDto> result = orderItemService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1, result.get(0).getOrderId());
		assertEquals(100, result.get(0).getProductId());
		assertNotNull(result.get(0).getProductDto());
		assertNotNull(result.get(0).getOrderDto());
		verify(orderItemRepository, times(1)).findAll();
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class));
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class));
	}
	
	@Test
	@DisplayName("Should return empty list when no order items exist")
	void testFindAll_EmptyList() {
		// Given
		when(orderItemRepository.findAll()).thenReturn(Collections.emptyList());
		
		// When
		List<OrderItemDto> result = orderItemService.findAll();
		
		// Then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(orderItemRepository, times(1)).findAll();
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should find order item by id successfully")
	void testFindById_Success() {
		// Given
		when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.of(testOrderItem));
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		
		// When
		OrderItemDto result = orderItemService.findById(testOrderItemId);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		assertEquals(100, result.getProductId());
		assertEquals(5, result.getOrderedQuantity());
		assertNotNull(result.getProductDto());
		assertNotNull(result.getOrderDto());
		verify(orderItemRepository, times(1)).findById(testOrderItemId);
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class));
		verify(restTemplate, times(1)).getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class));
	}
	
	@Test
	@DisplayName("Should throw OrderItemNotFoundException when order item not found")
	void testFindById_NotFound() {
		// Given
		OrderItemId nonExistentId = new OrderItemId(999, 999);
		when(orderItemRepository.findById(nonExistentId)).thenReturn(Optional.empty());
		
		// When & Then
		OrderItemNotFoundException exception = assertThrows(
				OrderItemNotFoundException.class,
				() -> orderItemService.findById(nonExistentId)
		);
		
		assertTrue(exception.getMessage().contains("OrderItem with id: " + nonExistentId + " not found"));
		verify(orderItemRepository, times(1)).findById(nonExistentId);
		verify(restTemplate, never()).getForObject(any(String.class), any(Class.class));
	}
	
	@Test
	@DisplayName("Should save order item successfully")
	void testSave_Success() {
		// Given
		OrderItemDto newOrderItemDto = OrderItemDto.builder()
				.orderId(2)
				.productId(200)
				.orderedQuantity(10)
				.productDto(ProductDto.builder().productId(200).build())
				.orderDto(OrderDto.builder().orderId(2).build())
				.build();
		
		OrderItem savedOrderItem = OrderItem.builder()
				.orderId(2)
				.productId(200)
				.orderedQuantity(10)
				.build();
		
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);
		
		// When
		OrderItemDto result = orderItemService.save(newOrderItemDto);
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.getOrderId());
		assertEquals(200, result.getProductId());
		assertEquals(10, result.getOrderedQuantity());
		verify(orderItemRepository, times(1)).save(any(OrderItem.class));
	}
	
	@Test
	@DisplayName("Should update order item successfully")
	void testUpdate_Success() {
		// Given
		OrderItemDto updatedOrderItemDto = OrderItemDto.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(8)
				.productDto(ProductDto.builder().productId(100).build())
				.orderDto(OrderDto.builder().orderId(1).build())
				.build();
		
		OrderItem updatedOrderItem = OrderItem.builder()
				.orderId(1)
				.productId(100)
				.orderedQuantity(8)
				.build();
		
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(updatedOrderItem);
		
		// When
		OrderItemDto result = orderItemService.update(updatedOrderItemDto);
		
		// Then
		assertNotNull(result);
		assertEquals(1, result.getOrderId());
		assertEquals(100, result.getProductId());
		assertEquals(8, result.getOrderedQuantity());
		verify(orderItemRepository, times(1)).save(any(OrderItem.class));
	}
	
	@Test
	@DisplayName("Should delete order item by id successfully")
	void testDeleteById_Success() {
		// Given
		// No need to mock anything for delete
		
		// When
		orderItemService.deleteById(testOrderItemId);
		
		// Then
		verify(orderItemRepository, times(1)).deleteById(testOrderItemId);
	}
	
	@Test
	@DisplayName("Should handle multiple order items and return distinct list")
	void testFindAll_MultipleOrderItems() {
		// Given
		OrderItem orderItem2 = OrderItem.builder()
				.orderId(2)
				.productId(200)
				.orderedQuantity(3)
				.build();
		
		List<OrderItem> orderItems = Arrays.asList(testOrderItem, orderItem2);
		when(orderItemRepository.findAll()).thenReturn(orderItems);
		
		ProductDto productDto2 = ProductDto.builder()
				.productId(200)
				.productTitle("Another Product")
				.build();
		
		OrderDto orderDto2 = OrderDto.builder()
				.orderId(2)
				.orderDesc("Another Order")
				.build();
		
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/100"),
				eq(ProductDto.class)))
				.thenReturn(testProductDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/200"),
				eq(ProductDto.class)))
				.thenReturn(productDto2);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"),
				eq(OrderDto.class)))
				.thenReturn(testOrderDto);
		when(restTemplate.getForObject(
				eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/2"),
				eq(OrderDto.class)))
				.thenReturn(orderDto2);
		
		// When
		List<OrderItemDto> result = orderItemService.findAll();
		
		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getOrderId());
		assertEquals(2, result.get(1).getOrderId());
		verify(orderItemRepository, times(1)).findAll();
	}

}

