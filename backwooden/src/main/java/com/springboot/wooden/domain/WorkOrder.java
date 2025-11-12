package com.springboot.wooden.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_order_tbl")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wo_no")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private PlannedOrder plannedOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no", nullable = false)
    private Item item;

    @Column(name = "wo_qty", nullable = false)
    private Integer qty;

    @Column(name = "deli_date", nullable = false)
    private LocalDate deliDate;

    @Column(name = "wo_status", length = 20, nullable = false)
    private String status;

    @Column(name = "remark")
    private String remark;

    public void changeStatus(String status) { this.status = status; }
    public void changeQty(Integer qty) { this.qty = qty; }
    public void changeDeliDate(LocalDate deliDate) { this.deliDate = deliDate; }
    public void changeRemark(String remark) { this.remark = remark; }
}
