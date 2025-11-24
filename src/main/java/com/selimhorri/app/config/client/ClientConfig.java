package com.selimhorri.app.config.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

	@LoadBalanced
	@Bean
	public RestTemplate restTemplateBean() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		// Timeout de conexión: 5 segundos
		factory.setConnectTimeout(5000);
		// Timeout de lectura: 10 segundos
		factory.setReadTimeout(10000);

		return new RestTemplate(factory);
	}

}
