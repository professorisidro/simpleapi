package com.professorisidro.simpleapi.customer;

import com.professorisidro.simpleapi.common.BusinessException;
import com.professorisidro.simpleapi.common.ResourceNotFoundException;
import com.professorisidro.simpleapi.customer.dto.CustomerRequest;
import com.professorisidro.simpleapi.customer.dto.CustomerResponse;
import com.professorisidro.simpleapi.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("A customer with email '" + request.email() + "' already exists");
        }
        Customer customer = new Customer();
        apply(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getEntity(id);
        if (customerRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessException("A customer with email '" + request.email() + "' already exists");
        }
        apply(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = getEntity(id);
        if (orderRepository.existsByCustomerId(id)) {
            throw new BusinessException("Cannot delete customer with existing orders");
        }
        customerRepository.delete(customer);
    }

    private Customer getEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
    }

    static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt());
    }
}
