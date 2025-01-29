package com.technews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication

public class TechNewsJavaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechNewsJavaApiApplication.class, args);
		System.out.println("Hello, World!");
	}
	@RestController
	public class Hello {
		@GetMapping("/hello")
		public static String ControllerHello() {
			return "Hello";
		}
	}
}
