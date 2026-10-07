package com.layth.Library.Management.System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

// Users log in through AuthenticationController and the users table, so Boot's default
// in-memory user (with its generated password printed at startup) is switched off.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
@EnableCaching
public class LibraryManagementSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(LibraryManagementSystemApplication.class, args);
	}

}
