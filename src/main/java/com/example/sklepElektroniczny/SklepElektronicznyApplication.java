package com.example.sklepElektroniczny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SklepElektronicznyApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SklepElektronicznyApplication.class, args);

		DatabaseConnectionTester tester = context.getBean(DatabaseConnectionTester.class);
		tester.testConnection();
	}
}
