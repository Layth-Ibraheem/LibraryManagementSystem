package com.layth.Library.Management.System.aspects;

import com.layth.Library.Management.System.services.LibrarianService;
import com.layth.Library.Management.System.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class LoggingAspectTest {
    @Autowired
    private LibrarianService librarianService;

    @Autowired
    private UserService userService;

    @Test
    void serviceCallsAreLoggedWithTheirDuration(CapturedOutput output) {
        librarianService.getAllLibrarians();

        assertThat(output).containsPattern("LibrarianService\\.getAllLibrarians\\(\\) completed in \\d+ ms");
    }

    @Test
    void argumentsSuchAsPasswordsAreNeverLogged(CapturedOutput output) {
        userService.authenticate("nobody-with-this-name", "secret-password-that-must-not-leak");

        assertThat(output).contains("UserService.authenticate(..) completed in");
        assertThat(output).doesNotContain("secret-password-that-must-not-leak");
    }
}
