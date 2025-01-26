package com.example.acfac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AcfacApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcfacApplication.class, args);
	}

}
