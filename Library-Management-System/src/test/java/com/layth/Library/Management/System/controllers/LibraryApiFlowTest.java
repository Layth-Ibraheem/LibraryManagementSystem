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
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Autowired
    private ApplicationContext context;

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

    @Test
    void getAfterDeleteReturnsNotFound() throws Exception {
        int bookId = createBook("Refactoring");
        getBook(bookId).andExpect(status().isOk());

        mockMvc.perform(delete("/api/books/{id}", bookId).header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        getBook(bookId).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/books/{id}", bookId).header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void aMissIsNotCachedSoABookCreatedLaterIsFound() throws Exception {
        int nextId = createBook("Working Effectively with Legacy Code") + 1;
        getBook(nextId).andExpect(status().isNotFound());

        assertThat(createBook("Patterns of Enterprise Application Architecture")).isEqualTo(nextId);

        getBook(nextId).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patterns of Enterprise Application Architecture"));
    }

    @Test
    void updateReplacesTheCachedBook() throws Exception {
        int bookId = createBook("Clean Architecture");
        getBook(bookId).andExpect(jsonPath("$.title").value("Clean Architecture"));

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Clean Code\", \"author\": \"Robert C. Martin\", \"publicationYear\": 2008}"))
                .andExpect(status().isOk());

        getBook(bookId).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.publicationYear").value(2008));
    }

    @Test
    void borrowAndReturnShowUpInTheNextGet() throws Exception {
        int bookId = createBook("The Pragmatic Programmer");
        int patronId = createPatron("Reader One");
        getBook(bookId).andExpect(jsonPath("$.borrowed").value(false));

        mockMvc.perform(post("/api/borrow/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
        getBook(bookId).andExpect(jsonPath("$.borrowed").value(true));

        mockMvc.perform(put("/api/return/{bookId}/patron/{patronId}", bookId, patronId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
        getBook(bookId).andExpect(jsonPath("$.borrowed").value(false));
    }

    @Test
    void aBookOnLoanCannotBeLentToASecondPatronUntilItIsReturned() throws Exception {
        int bookId = createBook("Designing Data-Intensive Applications");
        int firstPatron = createPatron("First Reader");
        int secondPatron = createPatron("Second Reader");

        borrow(bookId, firstPatron).andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId))
                .andExpect(jsonPath("$.patronId").value(firstPatron))
                .andExpect(jsonPath("$.returnedDate").doesNotExist());
        borrow(bookId, secondPatron).andExpect(status().isConflict());
        giveBack(bookId, secondPatron).andExpect(status().isConflict());

        giveBack(bookId, firstPatron).andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedDate").exists());
        giveBack(bookId, firstPatron).andExpect(status().isConflict());

        borrow(bookId, secondPatron).andExpect(status().isOk());
        giveBack(bookId, secondPatron).andExpect(status().isOk());
    }

    @Test
    void borrowingAnUnknownBookOrPatronIsNotFound() throws Exception {
        int bookId = createBook("Release It! Second Edition");
        int patronId = createPatron("Known Reader");

        borrow(999_999, patronId).andExpect(status().isNotFound());
        borrow(bookId, 999_999).andExpect(status().isNotFound());
        giveBack(bookId, 999_999).andExpect(status().isNotFound());
    }

    @Test
    void cacheUpdatesRunOutsideTheTransaction() {
        int cacheOrder = context.getBean(BeanFactoryCacheOperationSourceAdvisor.class).getOrder();
        int transactionOrder = context.getBean(BeanFactoryTransactionAttributeSourceAdvisor.class).getOrder();

        // A lower order wraps a higher one, so the cache sees the method return only after the commit.
        assertThat(cacheOrder).isLessThan(transactionOrder);
    }

    private int createBook(String title) throws Exception {
        return body(mockMvc.perform(post("/api/books")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"%s\", \"author\": \"Some Author\", \"publicationYear\": 2001}".formatted(title)))
                .andExpect(status().isCreated())).get("id").asInt();
    }

    private int createPatron(String name) throws Exception {
        return body(mockMvc.perform(post("/api/patrons")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"%s\", \"email\": \"reader%d@example.com\", \"phoneNumber\": \"0991234567\"}"
                                .formatted(name, SEQUENCE.incrementAndGet())))
                .andExpect(status().isCreated())).get("id").asInt();
    }

    private ResultActions borrow(int bookId, int patronId) throws Exception {
        return mockMvc.perform(post("/api/borrow/{bookId}/patron/{patronId}", bookId, patronId)
                .header("Authorization", adminToken));
    }

    private ResultActions giveBack(int bookId, int patronId) throws Exception {
        return mockMvc.perform(put("/api/return/{bookId}/patron/{patronId}", bookId, patronId)
                .header("Authorization", adminToken));
    }

    private ResultActions getBook(int bookId) throws Exception {
        return mockMvc.perform(get("/api/books/{id}", bookId).header("Authorization", adminToken));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static String bearer(JsonNode authResponse) {
        return "Bearer " + authResponse.get("token").asText();
    }
}
