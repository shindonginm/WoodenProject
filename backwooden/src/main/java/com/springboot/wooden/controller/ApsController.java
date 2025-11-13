package com.springboot.wooden.controller;

import com.springboot.wooden.domain.PlannedOrder;
import com.springboot.wooden.dto.PlannedOrderResponseDto;
import com.springboot.wooden.repository.PlannedOrderRepository;
import com.springboot.wooden.service.ApsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aps")
@RequiredArgsConstructor
public class ApsController {

    private final ApsService apsService;
    private final PlannedOrderRepository plannedOrderRepository;

    /*
     * APS 실행: horizonWeeks 주 만큼의 계획 생성
     * 예: POST /api/aps/run?horizonWeeks=12
     * 반환: 생성된 PlannedOrder 개수
     */
    @PostMapping("/run")
    public int run(@RequestParam(defaultValue = "12") int horizonWeeks) {
        return apsService.runPlanning(horizonWeeks);
    }

    /*
     * 상태가 PLANNED 인 계획 목록 조회
     * GET /api/aps/planned-orders
     */
    @GetMapping("/planned-orders")
    public List<PlannedOrderResponseDto> getPlannedOrders() {
        return apsService.getPlannedOrders()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /*
     * 개별 계획 확정
     * - PLAN  → WorkOrder 생성 예정
     * - BUYER → PartOrder 생성 (이미 연결함)
     *
     * POST /api/aps/planned-orders/{planId}/release
     */
    @PostMapping("/planned-orders/{planId}/release")
    public PlannedOrderResponseDto release(@PathVariable Long planId) {
        Long id = apsService.release(planId); // 서비스는 Long(planId) 반환

        PlannedOrder po = plannedOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("plan not found: " + id));

        return toDto(po);
    }

    private PlannedOrderResponseDto toDto(PlannedOrder po) {
        Long itemNo = (po.getItem() != null) ? po.getItem().getItemNo() : null;
        String itemName = (po.getItem() != null) ? po.getItem().getItemName() : null;

        Long partNo = (po.getPart() != null) ? po.getPart().getPartNo() : null;
        String partName = (po.getPart() != null) ? po.getPart().getPartName() : null;

        Long demandId = (po.getDemand() != null) ? po.getDemand().getId() : null;

        return PlannedOrderResponseDto.builder()
                .planId(po.getId())
                .planType(po.getPlanType())
                .status(po.getStatus())
                .itemNo(itemNo)
                .itemName(itemName)
                .partNo(partNo)
                .partName(partName)
                .demandId(demandId)
                .deliDate(po.getDeliDate())
                .qty(po.getQty())
                .remark(po.getRemark())
                .build();
    }
}
