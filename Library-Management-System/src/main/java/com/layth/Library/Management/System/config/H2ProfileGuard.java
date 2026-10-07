package com.layth.Library.Management.System.config;

import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * The h2 profile is the default profile and carries public dev-only values: a JWT signing key
 * and the passwords of the users that DevUserSeeder creates. It must never run against a real
 * database, for example when SPRING_DATASOURCE_URL is set but SPRING_PROFILES_ACTIVE is forgotten.
 * <p>
 * This bean stops the startup (before the web server starts and before any user is seeded)
 * unless the DataSource is an embedded in-memory database. The h2 profile also leaves
 * {@code ddl-auto} at Spring Boot's default, which is create-drop only for an embedded database
 * and none otherwise, so Hibernate never drops the tables of an external database.
 */
@Component
@Profile("h2")
public class H2ProfileGuard {

    public H2ProfileGuard(DataSource dataSource) {
        if (!EmbeddedDatabaseConnection.isEmbedded(dataSource)) {
            // The URL is not included: a JDBC URL can contain a password.
            throw new IllegalStateException("The h2 profile only runs on an embedded in-memory database, "
                    + "but spring.datasource.url points to another database. Set SPRING_PROFILES_ACTIVE "
                    + "(for example to sqlserver) whenever you set SPRING_DATASOURCE_URL.");
        }
    }
}
