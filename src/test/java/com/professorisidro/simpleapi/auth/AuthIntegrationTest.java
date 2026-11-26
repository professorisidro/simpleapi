package com.professorisidro.simpleapi.auth;

import com.professorisidro.simpleapi.auth.dto.TokenResponse;
import com.professorisidro.simpleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    void login_withSeededUser_returnsTokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"isidro","password":"isidro"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"isidro","password":"wrong"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_withValidRefreshToken_returnsNewAccessToken() throws Exception {
        TokenResponse tokens = login();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshBody(tokens.refreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refresh_usingAccessTokenAsRefresh_returns401() throws Exception {
        TokenResponse tokens = login();

        // An access token must not be accepted by the refresh endpoint.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshBody(tokens.accessToken()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshBody("not-a-jwt"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBlankBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":""}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sanity_tokensAreDistinct() throws Exception {
        TokenResponse tokens = login();
        assertThat(tokens.accessToken()).isNotEqualTo(tokens.refreshToken());
    }

    private record RefreshBody(String refreshToken) {
    }
}
