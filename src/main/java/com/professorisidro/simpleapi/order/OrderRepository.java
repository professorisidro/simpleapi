package com.professorisidro.simpleapi.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByCustomerId(Long customerId);
}
