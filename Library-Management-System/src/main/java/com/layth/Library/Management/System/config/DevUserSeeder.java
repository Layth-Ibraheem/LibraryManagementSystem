package com.layth.Library.Management.System.config;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Local development only (h2 profile): creates two users so the API can be used right away,
 * because self-registered users have no permissions.
 * <ul>
 *   <li>admin: every permission (roles = -1), can grant permissions to others</li>
 *   <li>clerk: ManagePatrons only (patrons, borrowing and returns)</li>
 * </ul>
 * Their passwords are the public dev-only values in application-h2.properties.
 */
@Configuration
@Profile("h2")
public class DevUserSeeder {
    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

    @Bean
    CommandLineRunner seedDevUsers(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   @Value("${app.seed.admin-password}") String adminPassword,
                                   @Value("${app.seed.clerk-password}") String clerkPassword) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder, "admin", adminPassword, UserRoles.AllRoles.getRole());
            createIfMissing(userRepository, passwordEncoder, "clerk", clerkPassword, UserRoles.ManagePatrons.getRole());
        };
    }

    private static void createIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                        String userName, String rawPassword, int roles) {
        if (userRepository.existsByUserName(userName)) {
            return;
        }
        userRepository.save(new User(null, userName, passwordEncoder.encode(rawPassword), roles));
        log.info("Seeded dev user '{}' with roles {}", userName, roles);
    }
}
