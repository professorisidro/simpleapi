package com.professorisidro.simpleapi.order;

import com.professorisidro.simpleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIntegrationTest extends IntegrationTestSupport {

    @Test
    void create_computesTotalAndSnapshotsPrice() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.total@example.com");
        long productId = createProduct(auth, "OT-1", "199.90", 10);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customerId, productId, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(399.80))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(199.90))
                .andExpect(jsonPath("$.items[0].subtotal").value(399.80));
    }

    @Test
    void create_withUnknownCustomer_returns404() throws Exception {
        String auth = bearer();
        long productId = createProduct(auth, "OT-404C", "10.00", 5);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(999999L, productId, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withUnknownProduct_returns404() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.404p@example.com");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customerId, 999999L, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withEmptyItems_returns400() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.empty@example.com");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_replacesItemsAndRecomputesTotal() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.update@example.com");
        long productId = createProduct(auth, "OT-UPD", "50.00", 100);
        long orderId = createOrder(auth, customerId, productId, 1); // total 50.00

        mockMvc.perform(put("/api/orders/" + orderId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId
                                + ",\"status\":\"PAID\",\"items\":[{\"productId\":" + productId
                                + ",\"quantity\":3}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(150.00));
    }

    @Test
    void deleteCustomer_withOrders_returns409() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.block.customer@example.com");
        long productId = createProduct(auth, "OT-BC", "10.00", 5);
        createOrder(auth, customerId, productId, 1);

        mockMvc.perform(delete("/api/customers/" + customerId).header("Authorization", auth))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteProduct_referencedByOrder_returns409() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.block.product@example.com");
        long productId = createProduct(auth, "OT-BP", "10.00", 5);
        createOrder(auth, customerId, productId, 1);

        mockMvc.perform(delete("/api/products/" + productId).header("Authorization", auth))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_order_returns204() throws Exception {
        String auth = bearer();
        long customerId = createCustomer(auth, "order.delete@example.com");
        long productId = createProduct(auth, "OT-DEL", "10.00", 5);
        long orderId = createOrder(auth, customerId, productId, 1);

        mockMvc.perform(delete("/api/orders/" + orderId).header("Authorization", auth))
                .andExpect(status().isNoContent());
    }

    // --- helpers ---

    private String orderJson(long customerId, long productId, int quantity) {
        return "{\"customerId\":" + customerId + ",\"items\":[{\"productId\":" + productId
                + ",\"quantity\":" + quantity + "}]}";
    }

    private long createCustomer(String auth, String email) throws Exception {
        String body = mockMvc.perform(post("/api/customers")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Customer\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createProduct(String auth, String sku, String price, int stock) throws Exception {
        String body = mockMvc.perform(post("/api/products")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Product\",\"sku\":\"" + sku + "\",\"price\":" + price
                                + ",\"stockQuantity\":" + stock + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createOrder(String auth, long customerId, long productId, int quantity) throws Exception {
        String body = mockMvc.perform(post("/api/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(customerId, productId, quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
