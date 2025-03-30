package com.example.sklepElektroniczny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SklepElektronicznyApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SklepElektronicznyApplication.class, args);

		DatabaseConnectionTester tester = context.getBean(DatabaseConnectionTester.class);
		tester.testConnection();
	}

}

