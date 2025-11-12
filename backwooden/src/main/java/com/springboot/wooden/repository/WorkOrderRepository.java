package com.springboot.wooden.repository;

import com.springboot.wooden.domain.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @EntityGraph(attributePaths = {"item","plannedOrder"})
    List<WorkOrder> findByStatus(String status);

    @Override
    @EntityGraph(attributePaths = {"item", "plannedOrder"})
    List<WorkOrder> findAll();
}
