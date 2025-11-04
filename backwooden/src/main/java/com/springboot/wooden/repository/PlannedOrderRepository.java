package com.springboot.wooden.repository;

import com.springboot.wooden.domain.PlannedOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlannedOrderRepository extends JpaRepository<PlannedOrder, Long> {

    @EntityGraph(attributePaths = {"item","part","demand"})
    List<PlannedOrder> findByStatus(String status);
}
