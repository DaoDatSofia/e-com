package com.ecommerce.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ecommerce")
public class SbEcomApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbEcomApplication.class, args);
	}

}
