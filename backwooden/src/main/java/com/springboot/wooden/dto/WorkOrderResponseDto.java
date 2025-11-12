package com.springboot.wooden.dto;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderResponseDto {
    private Long woNo;           // 작업지시 번호
    private Long plannedOrderId; // 원천 APS 계획 ID
    private Long itemNo;         // 품목 번호
    private String itemName;     // 품목 명
    private Integer qty;         // 작업 수량
    private LocalDate deliDate;  // 납기
    private String status;       // PLANNED / RELEASED / IN_PROGRESS / DONE / CANCELED 상태
    private String remark;       // 비고
}
