package com.professorisidro.simpleapi.customer;

import com.professorisidro.simpleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerIntegrationTest extends IntegrationTestSupport {

    @Test
    void create_thenGet_roundTrips() throws Exception {
        String auth = bearer();

        String body = mockMvc.perform(post("/api/customers")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada Lovelace","email":"ada.create@example.com","phone":"+55 11 99999-0000"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/customers/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada.create@example.com"));
    }

    @Test
    void update_changesFields() throws Exception {
        String auth = bearer();

        long id = createCustomer(auth, "Grace Hopper", "grace.update@example.com");

        mockMvc.perform(put("/api/customers/" + id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Grace M. Hopper","email":"grace.update@example.com","phone":"123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grace M. Hopper"))
                .andExpect(jsonPath("$.phone").value("123"));
    }

    @Test
    void create_withDuplicateEmail_returns409() throws Exception {
        String auth = bearer();
        createCustomer(auth, "First", "dup@example.com");

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Second","email":"dup@example.com"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void create_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.name").isNotEmpty());
    }

    @Test
    void get_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/customers/999999").header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_removesCustomer() throws Exception {
        String auth = bearer();
        long id = createCustomer(auth, "Temp", "temp.delete@example.com");

        mockMvc.perform(delete("/api/customers/" + id).header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/" + id).header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    private long createCustomer(String auth, String name, String email) throws Exception {
        String body = mockMvc.perform(post("/api/customers")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
