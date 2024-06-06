package com.example.reminderservicemaven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class ReminderServiceMavenApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReminderServiceMavenApplication.class, args);
	}

}
