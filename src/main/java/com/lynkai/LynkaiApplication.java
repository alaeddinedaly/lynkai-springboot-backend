package com.lynkai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LynkaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LynkaiApplication.class, args);
	}

}
