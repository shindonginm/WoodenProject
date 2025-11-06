package com.springboot.wooden.repository;

import com.springboot.wooden.domain.DemandPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DemandPlanRepository extends JpaRepository<DemandPlan, Long> {

    @EntityGraph(attributePaths = {"item"})
    List<DemandPlan> findByDeliDateBetween(LocalDate from, LocalDate to);

    List<DemandPlan> findByItem_ItemNo(Long itemNo);
}
