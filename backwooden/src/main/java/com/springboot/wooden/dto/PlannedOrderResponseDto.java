package com.springboot.wooden.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PlannedOrderResponseDto {

    private Long planId;        // 계획 ID
    private String planType;    // PLAN / BUYER
    private String status;      // 상태

    private Long itemNo;        // 완제품 품목 번호 (PLAN일 때)
    private String itemName;

    private Long partNo;        // 부품 번호 (BUYER일 때)
    private String partName;

    private Long demandId;      // 연결된 DemandPlan ID (있다면)
    private LocalDate deliDate; // 납기
    private Integer qty;        // 계획 수량

    private String remark;      // 비고
}
