package com.springboot.wooden.controller;

import com.springboot.wooden.domain.PlannedOrder;
import com.springboot.wooden.dto.WorkOrderResponseDto;
import com.springboot.wooden.repository.PlannedOrderRepository;
import com.springboot.wooden.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final PlannedOrderRepository plannedOrderRepository;

    @GetMapping
    public List<WorkOrderResponseDto> list(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            return workOrderService.getAll();
        }
        return workOrderService.getByStatus(status);
    }

    @PostMapping("/from-plan/{planId}")
    public WorkOrderResponseDto createFromPlan(@PathVariable Long planId) {
        PlannedOrder plan = plannedOrderRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("plan not found: " + planId));
        return workOrderService.createFromPlan(plan);
    }

    @PatchMapping("/{woNo}/release")
    public WorkOrderResponseDto release(@PathVariable Long woNo) {
        return workOrderService.release(woNo);
    }

    @PatchMapping("/{woNo}/start")
    public WorkOrderResponseDto start(@PathVariable Long woNo) {
        return workOrderService.start(woNo);
    }

    @PatchMapping("/{woNo}/complete")
    public WorkOrderResponseDto complete(@PathVariable Long woNo) {
        return workOrderService.complete(woNo);
    }

    @PatchMapping("/{woNo}/cancel")
    public WorkOrderResponseDto cancel(@PathVariable Long woNo) {
        return workOrderService.cancel(woNo);
    }
}
