package com.professorisidro.simpleapi.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.professorisidro.simpleapi.auth.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for MVC integration tests: boots the full context with MockMvc and
 * provides helpers for authentication and JSON (de)serialization.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Logs in with the seeded user and returns the token pair. */
    protected TokenResponse login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"isidro","password":"isidro"}"""))
                .andExpect(status().isOk())
                .andReturn();
        return read(result.getResponse().getContentAsString(), TokenResponse.class);
    }

    /** Convenience: "Bearer <accessToken>" for the seeded user. */
    protected String bearer() throws Exception {
        return "Bearer " + login().accessToken();
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected <T> T read(String json, Class<T> type) throws Exception {
        return objectMapper.readValue(json, type);
    }
}
