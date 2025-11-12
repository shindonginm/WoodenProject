package com.springboot.wooden.service;

import com.springboot.wooden.domain.Item;
import com.springboot.wooden.domain.ItemStock;
import com.springboot.wooden.domain.PlannedOrder;
import com.springboot.wooden.domain.WorkOrder;
import com.springboot.wooden.dto.WorkOrderResponseDto;
import com.springboot.wooden.repository.ItemStockRepository;
import com.springboot.wooden.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ItemStockRepository itemStockRepository;

    @Override
    public WorkOrderResponseDto createFromPlan(PlannedOrder plan) {
        if (!"PLAN".equals(plan.getPlanType())) {
            throw new IllegalArgumentException("PLAN 타입만 작업지시 생성 가능: " + plan.getPlanType());
        }
        Item item = plan.getItem();
        if (item == null) {
            throw new IllegalStateException("PLAN인데 item 정보가 없습니다. planId=" + plan.getId());
        }

        WorkOrder wo = WorkOrder.builder()
                .plannedOrder(plan)
                .item(item)
                .qty(plan.getQty())
                .deliDate(plan.getDeliDate())
                .status("PLANNED")
                .remark("from APS plan " + plan.getId())
                .build();

        return toDto(workOrderRepository.save(wo));
    }

    @Override
    public WorkOrderResponseDto release(Long woNo) {
        WorkOrder wo = getOrThrow(woNo);
        if (!"PLANNED".equals(wo.getStatus())) {
            throw new IllegalStateException("PLANNED 상태만 RELEASED로 전환 가능");
        }
        wo.changeStatus("RELEASED");
        return toDto(wo);
    }

    @Override
    public WorkOrderResponseDto start(Long woNo) {
        WorkOrder wo = getOrThrow(woNo);
        if (!"RELEASED".equals(wo.getStatus())) {
            throw new IllegalStateException("RELEASED 상태만 IN_PROGRESS로 전환 가능");
        }
        wo.changeStatus("IN_PROGRESS");
        return toDto(wo);
    }

    @Override
    public WorkOrderResponseDto complete(Long woNo) {
        WorkOrder wo = getOrThrow(woNo);
        if (!"IN_PROGRESS".equals(wo.getStatus())) {
            throw new IllegalStateException("IN_PROGRESS 상태만 DONE으로 전환 가능");
        }
        wo.changeStatus("DONE");

        Item item = wo.getItem();
        int inc = wo.getQty();

        ItemStock stock = itemStockRepository.findByItemNoForUpdate(item.getItemNo())
                .orElseGet(() -> itemStockRepository.save(
                        ItemStock.builder()
                                .item(item)
                                .isQty(0)
                                .totalIn(0)
                                .totalOut(0)
                                .build()
                ));

        stock.produce(inc);

        return toDto(wo);
    }

    @Override
    public WorkOrderResponseDto cancel(Long woNo) {
        WorkOrder wo = getOrThrow(woNo);
        wo.changeStatus("CANCELED");
        return toDto(wo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<WorkOrderResponseDto> getAll() {
        return workOrderRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<WorkOrderResponseDto> getByStatus(String status) {
        return workOrderRepository.findByStatus(status).stream().map(this::toDto).toList();
    }

    /* helpers */
    private WorkOrder getOrThrow(Long woNo) {
        return workOrderRepository.findById(woNo)
                .orElseThrow(() -> new IllegalArgumentException("work order not found: " + woNo));
    }

    private WorkOrderResponseDto toDto(WorkOrder wo) {
        Long plannedId = (wo.getPlannedOrder() != null) ? wo.getPlannedOrder().getId() : null;
        Long itemNo = (wo.getItem() != null) ? wo.getItem().getItemNo() : null;
        String itemName = (wo.getItem() != null) ? wo.getItem().getItemName() : null;

        return WorkOrderResponseDto.builder()
                .woNo(wo.getId())
                .plannedOrderId(plannedId)
                .itemNo(itemNo)
                .itemName(itemName)
                .qty(wo.getQty())
                .deliDate(wo.getDeliDate())
                .status(wo.getStatus())
                .remark(wo.getRemark())
                .build();
    }
}
