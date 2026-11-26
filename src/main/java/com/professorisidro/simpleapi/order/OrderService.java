package com.professorisidro.simpleapi.order;

import com.professorisidro.simpleapi.common.ResourceNotFoundException;
import com.professorisidro.simpleapi.customer.Customer;
import com.professorisidro.simpleapi.customer.CustomerRepository;
import com.professorisidro.simpleapi.order.dto.OrderItemRequest;
import com.professorisidro.simpleapi.order.dto.OrderItemResponse;
import com.professorisidro.simpleapi.order.dto.OrderRequest;
import com.professorisidro.simpleapi.order.dto.OrderResponse;
import com.professorisidro.simpleapi.product.Product;
import com.professorisidro.simpleapi.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public OrderResponse create(OrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        Order order = new Order();
        order.setCustomer(customer);
        if (request.status() != null) {
            order.setStatus(request.status());
        }
        buildItems(order, request.items());
        order.recalculateTotal();

        return toResponse(orderRepository.save(order));
    }

    public OrderResponse update(Long id, OrderRequest request) {
        Order order = getEntity(id);

        if (!order.getCustomer().getId().equals(request.customerId())) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
            order.setCustomer(customer);
        }
        if (request.status() != null) {
            order.setStatus(request.status());
        }

        order.clearItems();
        buildItems(order, request.items());
        order.recalculateTotal();

        return toResponse(orderRepository.save(order));
    }

    public void delete(Long id) {
        Order order = getEntity(id);
        orderRepository.delete(order); // cascades to its items
    }

    private void buildItems(Order order, List<OrderItemRequest> itemRequests) {
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice()); // snapshot of the current price
            item.recalculateSubtotal();

            order.addItem(item);
        }
    }

    private Order getEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotal(),
                items);
    }
}
