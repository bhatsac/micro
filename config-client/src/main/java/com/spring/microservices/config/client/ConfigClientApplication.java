package com.spring.microservices.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigClientApplication.class, args);
	}

	
	
	
	
}

@RestController
@RefreshScope
class ConfigClientController {

	private final String value;

	
	//This is the value of the property which needs to be fetched via config server from git repo
	ConfigClientController(@Value("${foo:test}") String value) {
		this.value = value;
	}

	@GetMapping("/foo")
	String foo() {
		return this.value;
	}
}

@RestController
class BonusController {

	private final String value;

	BonusController(@Value("${bonus:test}") String value) {
		this.value = value;
	}

	@GetMapping("/bonus")
	String foo() {
		return this.value;
	}
}