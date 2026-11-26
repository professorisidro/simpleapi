package com.professorisidro.simpleapi.product;

import com.professorisidro.simpleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends IntegrationTestSupport {

    @Test
    void create_thenGet_roundTrips() throws Exception {
        String auth = bearer();

        String body = mockMvc.perform(post("/api/products")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Keyboard","description":"Mechanical","sku":"KB-RT","price":199.90,"stockQuantity":10}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("KB-RT"))
                .andExpect(jsonPath("$.price").value(199.90))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/products/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void create_withDuplicateSku_returns409() throws Exception {
        String auth = bearer();
        mockMvc.perform(post("/api/products")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"A","sku":"DUP-SKU","price":1.00,"stockQuantity":1}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"B","sku":"DUP-SKU","price":2.00,"stockQuantity":1}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void create_withZeroPrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Free","sku":"FREE-1","price":0,"stockQuantity":1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price").isNotEmpty());
    }

    @Test
    void create_withNegativeStock_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Neg","sku":"NEG-1","price":5.00,"stockQuantity":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.stockQuantity").isNotEmpty());
    }
}
