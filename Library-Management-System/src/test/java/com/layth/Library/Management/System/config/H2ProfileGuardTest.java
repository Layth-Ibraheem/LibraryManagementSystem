package com.layth.Library.Management.System.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class H2ProfileGuardTest {

    @Test
    void acceptsAnInMemoryDatabase() {
        assertThatCode(() -> new H2ProfileGuard(h2("jdbc:h2:mem:guard-test")))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesADatabaseThatIsNotInMemory(@TempDir Path dir) {
        // A file database stands in for a real server: it is not embedded in the sense of Spring Boot.
        DataSource fileDatabase = h2("jdbc:h2:file:" + dir.resolve("db").toAbsolutePath());

        assertThatIllegalStateException()
                .isThrownBy(() -> new H2ProfileGuard(fileDatabase))
                .withMessageContaining("SPRING_PROFILES_ACTIVE")
                .withMessageNotContaining("jdbc:");
    }

    private static DataSource h2(String url) {
        return new DriverManagerDataSource(url, "sa", "");
    }
}
