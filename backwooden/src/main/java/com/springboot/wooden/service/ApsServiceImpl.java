package com.springboot.wooden.service;

import com.springboot.wooden.domain.*;
import com.springboot.wooden.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ApsServiceImpl implements ApsService {

    private final DemandPlanRepository demandPlanRepository;
    private final PlannedOrderRepository plannedOrderRepository;
    private final ItemStockRepository itemStockRepository;
    private final PartStockRepository partStockRepository;
    private final BOMRepository bomRepository;
    private final PartOrderService partOrderService;

    /**
     * APS 메인 로직
     */
    @Override
    public int runPlanning(int horizonWeeks) {
        LocalDate today = LocalDate.now();
        LocalDate to = today.plusWeeks(horizonWeeks);

        List<DemandPlan> demands = demandPlanRepository.findByDeliDateBetween(today, to);

        Map<Long, Map<LocalDate, Integer>> demandMap = new HashMap<>();
        for (DemandPlan d : demands) {
            Long itemNo = d.getItem().getItemNo();
            LocalDate deli = d.getDeliDate();
            demandMap
                    .computeIfAbsent(itemNo, k -> new HashMap<>())
                    .merge(deli, d.getQty(), Integer::sum);
        }

        int created = 0;

        for (Map.Entry<Long, Map<LocalDate, Integer>> entry : demandMap.entrySet()) {
            Long itemNo = entry.getKey();

            Item item = demands.stream()
                    .filter(d -> d.getItem().getItemNo().equals(itemNo))
                    .findFirst()
                    .orElseThrow()
                    .getItem();

            int onHand = getItemOnHand(itemNo);

            List<Map.Entry<LocalDate, Integer>> byDate = new ArrayList<>(entry.getValue().entrySet());
            byDate.sort(Map.Entry.comparingByKey());

            for (Map.Entry<LocalDate, Integer> d2 : byDate) {
                LocalDate deliDate = d2.getKey();
                int need = d2.getValue();

                int shortage = Math.max(0, need - onHand);
                onHand = Math.max(0, onHand - need);

                if (shortage > 0) {
                    PlannedOrder planOrder = PlannedOrder.builder()
                            .planType("PLAN")
                            .item(item)
                            .deliDate(deliDate)
                            .qty(shortage)
                            .status("PLANNED")
                            .remark("APS: item shortage")
                            .build();
                    plannedOrderRepository.save(planOrder);
                    created++;

                    List<BOM> bomList = bomRepository.findAllByItem_ItemNo(itemNo);
                    for (BOM bom : bomList) {
                        int needPart = bom.getQtyPerItem() * shortage;
                        int partOnHand = getPartOnHand(bom.getPart().getPartNo());
                        int partShort = Math.max(0, needPart - partOnHand);

                        if (partShort > 0) {
                            PlannedOrder buyOrder = PlannedOrder.builder()
                                    .planType("BUYER")     // 구매 쪽
                                    .part(bom.getPart())
                                    .deliDate(deliDate)
                                    .qty(partShort)
                                    .status("PLANNED")
                                    .remark("APS: part shortage from item " + itemNo)
                                    .build();
                            plannedOrderRepository.save(buyOrder);
                            created++;
                        }
                    }
                }
            }
        }

        return created;
    }

    /**
     * 상태가 PLANNED 인 계획 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlannedOrder> getPlannedOrders() {
        return plannedOrderRepository.findByStatus("PLANNED");
    }

    /**
     * 계획 확정
     * - PLAN  → 생산 쪽으로 넘기기
     * - BUYER → 구매 발주로 넘기기
     */
    @Override
    public Long release(Long planId) {
        PlannedOrder po = plannedOrderRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("plan not found: " + planId));

        // 상태 변경
        po.changeStatus("RELEASED");

        // TODO: 실제 도메인 서비스로 전개
        if ("PLAN".equals(po.getPlanType())) {
            // 예: productionService.createWorkOrderFromPlan(po);
            // 또는 itemStockService.adjust(po.getItem().getItemNo(), +po.getQty());
        } else if ("BUYER".equals(po.getPlanType())) {
            partOrderService.addFromPlan(po);
        }

        return po.getId();
    }

    private int getItemOnHand(Long itemNo) {
        return itemStockRepository.findByItemNoForUpdate(itemNo)
                .map(ItemStock::getIsQty)
                .orElse(0);
    }

    private int getPartOnHand(Long partNo) {
        return partStockRepository.findAll().stream()
                .filter(s -> s.getPart().getPartNo().equals(partNo))
                .findFirst()
                .map(PartStock::getPsQty)
                .orElse(0);
    }
}
