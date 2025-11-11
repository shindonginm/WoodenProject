package com.springboot.wooden.service;

import com.springboot.wooden.domain.PlannedOrder;
import com.springboot.wooden.dto.PartOrderRequestDto;
import com.springboot.wooden.dto.PartOrderResponseDto;

import java.util.List;

public interface PartOrderService {
    List<PartOrderResponseDto> getAll();
    PartOrderResponseDto addPartOrder(PartOrderRequestDto dto);
    PartOrderResponseDto updatePartOrder(Long poNo, PartOrderRequestDto dto);
    void deletePartOrder(Long poNo);
    List<PartOrderResponseDto> getCompletedList();
    PartOrderResponseDto addFromPlan(PlannedOrder plannedOrder);
}
