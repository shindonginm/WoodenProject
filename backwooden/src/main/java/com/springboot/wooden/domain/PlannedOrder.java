package com.springboot.wooden.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "planned_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PlannedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @Column(name = "plan_type", length = 20, nullable = false)
    private String planType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id")
    private DemandLedger demand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_no")
    private Part part;

    @Column(name = "deli_date", nullable = false)
    private LocalDate deliDate;

    @Column(name = "plan_qty", nullable = false)
    private Integer qty;

    @Column(name = "plan_status", length = 30, nullable = false)
    private String status;

    @Column(name = "remark")
    private String remark;

    public void changeStatus(String status) {
        this.status = status;
    }

    public void changeQty(Integer qty) {
        this.qty = qty;
    }

    public void changeDeliDate(LocalDate deliDate) {
        this.deliDate = deliDate;
    }

    public void changeDemand(DemandLedger demand) {
        this.demand = demand;
    }

    public void changeRemark(String remark) {
        this.remark = remark;
    }

    public void changeToPlan(Item item) {
        this.planType = "PLAN";
        this.item = item;
        this.part = null;
    }

    public void changeToBuyer(Part part) {
        this.planType = "BUYER";
        this.part = part;
        this.item = null;
    }
}
