package com.anran.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PartnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PartnerApplication.class, args);
	}

}
