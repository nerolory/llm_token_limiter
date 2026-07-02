package com.tokenlimiter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokenlimiter.dto.request.ConsumeCreditsRequest;
import com.tokenlimiter.dto.request.EstimateTokensRequest;
import com.tokenlimiter.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void consumeCredits_shouldReturn200WithRemainingTokens() throws Exception {
        ConsumeCreditsRequest request = new ConsumeCreditsRequest("user-uuid-8888", 45L);

        mockMvc.perform(post("/api/v1/credits/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-uuid-8888")))
                .andExpect(jsonPath("$.remainingTokens", is(155)))
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }

    @Test
    void consumeCredits_shouldReturn402_whenInsufficientBalance() throws Exception {
        ConsumeCreditsRequest request = new ConsumeCreditsRequest("user-uuid-9999", 1L);

        mockMvc.perform(post("/api/v1/credits/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.title", is("Insufficient Credits")))
                .andExpect(jsonPath("$.status", is(402)));
    }

    @Test
    void consumeCredits_shouldReturn422_whenZeroTokensRequested() throws Exception {
        String invalidPayload = "{\"userId\": \"user-uuid-8888\", \"tokensRequested\": 0}";

        mockMvc.perform(post("/api/v1/credits/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Validation Error")))
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void estimateTokens_shouldReturn422_whenModelIsUnknown() throws Exception {
        EstimateTokensRequest request = new EstimateTokensRequest("Hello world", "gpt-unknown");

        mockMvc.perform(post("/api/v1/credits/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Invalid Model")))
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void getBalance_shouldReturn200WithAccountDetails() throws Exception {
        mockMvc.perform(get("/api/v1/credits/balance/user-uuid-8888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-uuid-8888")))
                .andExpect(jsonPath("$.currentBalance", is(200)))
                .andExpect(jsonPath("$.accountStatus", is("ACTIVE")));
    }

    @Test
    void consumeCredits_shouldReturn422_whenPayloadIsInvalid() throws Exception {
        String invalidPayload = "{\"userId\":\"\", \"tokensRequested\": -5}";

        mockMvc.perform(post("/api/v1/credits/consume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title", is("Validation Error")))
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void estimateTokens_shouldReturn200WithEstimatedCount() throws Exception {
        EstimateTokensRequest request = new EstimateTokensRequest(
                "Hello, can you please analyze this system architecture and provide feedback?",
                "gpt-4o");

        mockMvc.perform(post("/api/v1/credits/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedTokens").isNumber())
                .andExpect(jsonPath("$.modelUsed", is("gpt-4o")));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void consumeCredits_shouldHandleConcurrentRequestsSafely() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                ConsumeCreditsRequest request = new ConsumeCreditsRequest("user-uuid-1111", 5L);
                try {
                    return mockMvc.perform(post("/api/v1/credits/consume")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        List<Future<Integer>> results = executor.invokeAll(tasks);
        executor.shutdown();

        for (Future<Integer> result : results) {
            int status = result.get();
            assertThat(status).isIn(200, 402);
        }

        Long finalBalance = userAccountRepository
                .findByUserId("user-uuid-1111")
                .orElseThrow()
                .getTokenBalance();
        assertThat(finalBalance).isGreaterThanOrEqualTo(0L);
        assertThat(finalBalance).isZero();
    }
}
