package com.springboot.wooden.service;

import com.springboot.wooden.domain.PlannedOrder;
import com.springboot.wooden.dto.WorkOrderResponseDto;

import java.util.List;

public interface WorkOrderService {
    WorkOrderResponseDto createFromPlan(PlannedOrder plan);

    WorkOrderResponseDto release(Long woNo);   // PLANNED -> RELEASED
    WorkOrderResponseDto start(Long woNo);     // RELEASED -> IN_PROGRESS
    WorkOrderResponseDto complete(Long woNo);  // IN_PROGRESS -> DONE (+재고증가)
    WorkOrderResponseDto cancel(Long woNo);    // * -> CANCELED

    List<WorkOrderResponseDto> getAll();
    List<WorkOrderResponseDto> getByStatus(String status);
}
