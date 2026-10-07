package com.layth.Library.Management.System.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.layth.Library.Management.System.entities.Book;
import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.entities.UserRoles;
import com.layth.Library.Management.System.repositories.BookRepository;
import com.layth.Library.Management.System.repositories.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks of the book, patron and loan endpoints on the h2 profile.
 * It uses its own in-memory database and turns on Hibernate statistics to count SQL statements.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:library-flow;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@AutoConfigureMockMvc
class LibraryApiFlowTest {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    private String adminToken;

    @BeforeEach
    void logInAsAdmin() throws Exception {
        adminToken = bearer(body(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\": \"admin\", \"password\": \"%s\"}".formatted(adminPassword)))
                .andExpect(status().isOk())));
    }

    @Test
    void listingBooksRunsOneQueryNoMatterHowManyUsersAddedThem() throws Exception {
        for (int i = 0; i < 3; i++) {
            User creator = userRepository.save(new User(null, "creator-" + SEQUENCE.incrementAndGet(),
                    "not-a-real-hash", UserRoles.ManageBooks.getRole()));
            bookRepository.save(new Book(null, "Book by creator " + i, "Some Author", 2001,
                    "isbn-" + SEQUENCE.incrementAndGet(), LocalDate.now(), creator, false));
        }
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/api/books").header("Authorization", adminToken))
                .andExpect(status().isOk());

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    private JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static String bearer(JsonNode authResponse) {
        return "Bearer " + authResponse.get("token").asText();
    }
}
