package com.infosys.ordermanagement;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class OrdermanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdermanagementApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
//	@Bean
//	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
//		return ((args) -> {
//			Object quote = restTemplate.getForObject(
//					"http://vfpmys-56:8080/api/products/107", Object.class);
////			restTemplate.get
//			System.out.println(quote);
//		});
//	}
}
