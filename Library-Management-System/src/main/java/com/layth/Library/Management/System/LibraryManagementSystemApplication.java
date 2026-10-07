package com.layth.Library.Management.System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.Ordered;

// Users log in through AuthenticationController and the users table, so Boot's default
// in-memory user (with its generated password printed at startup) is switched off.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
// The caching proxy wraps the transaction proxy (whose order is LOWEST_PRECEDENCE), so
// @CachePut and @CacheEvict run after the transaction has committed, never before it.
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
public class LibraryManagementSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(LibraryManagementSystemApplication.class, args);
	}

}
