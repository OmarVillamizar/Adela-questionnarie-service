package com.example.adela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class AdelaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdelaApplication.class, args);
	}

}
