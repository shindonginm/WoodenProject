package com.springboot.wooden.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanRequestDTO {

    // plan_no 매핑
    private Long planNo;
    private Long itemNo;
    private int planQty;
    private String planState;
    private LocalDate planStartDate;
    private LocalDate planEndDate;

}
