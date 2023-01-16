package com.blueconnectionz.nicenice;

import com.blueconnectionz.nicenice.security.service.ImageStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
public class NiceniceApplication implements CommandLineRunner {

	@Resource
	ImageStorageService imageStorageService;
	public static void main(String[] args) {

		SpringApplication.run(NiceniceApplication.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
		imageStorageService.deleteAll();
		imageStorageService.init();
	}
}
