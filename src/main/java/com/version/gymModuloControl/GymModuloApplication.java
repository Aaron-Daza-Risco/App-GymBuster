package com.version.gymModuloControl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GymModuloApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymModuloApplication.class, args);
		System.out.println("HELLO WORLD");
	}
}