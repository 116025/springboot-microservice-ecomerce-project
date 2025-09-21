package com.springboot.product_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.product_service.dto.ProductRequest;
import com.springboot.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}


	@BeforeEach
	void cleanDb() {
		productRepository.deleteAll();
	}

	@Test
	void shouldCreateProduct() throws Exception {

		ProductRequest productRequest = getProductRequest();

		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString))
				.andExpect(status().isCreated());

        Assertions.assertEquals(1, productRepository.findAll().size());
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("Iphone 13")
				.description("Iphone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}

	@Test
	void shouldGetAllProducts() throws Exception {
		// Seed #1
		ProductRequest iphone = ProductRequest.builder()
				.name("iPhone 13")
				.description("Apple iPhone 13")
				.price(BigDecimal.valueOf(1200))
				.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(iphone)))
				.andExpect(status().isCreated());

		// Seed #2
		ProductRequest pixel = ProductRequest.builder()
				.name("Pixel 8")
				.description("Google Pixel 8")
				.price(BigDecimal.valueOf(899))
				.build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(pixel)))
				.andExpect(status().isCreated());


		mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				// list has 2 items
				.andExpect(jsonPath("$", hasSize(2)))
				// verify some fields on first element (order may depend on your repository; adjust if needed)
				.andExpect(jsonPath("$[0].name", not(isEmptyOrNullString())))
				.andExpect(jsonPath("$[0].price", notNullValue()))
				// and make sure at least one of the seeded items is present
				.andExpect(jsonPath("$[*].name", hasItems("iPhone 13", "Pixel 8")));
	}




}
