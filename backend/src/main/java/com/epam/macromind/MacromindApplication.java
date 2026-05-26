package com.epam.macromind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MacromindApplication {

	public static void main(String[] args) {
		SpringApplication.run(MacromindApplication.class, args);
	}

}
