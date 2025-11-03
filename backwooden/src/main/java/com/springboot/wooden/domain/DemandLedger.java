package com.springboot.wooden.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "demand_ledger_tbl")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "demand_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no")
    private Item item;

    @Column(name = "required_date", nullable = false)
    private LocalDate requiredDate;

    @Column(name = "demand_qty", nullable = false)
    private Integer qty;

    @Column(name = "source_type", length = 30, nullable = false)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "priority")
    private Integer priority;

    public void changeItem(Item item) {
        this.item = item;
    }

    public void changeRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public void changeQty(Integer qty) {
        this.qty = qty;
    }

    public void changeSource(String sourceType, Long sourceId) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    public void changePriority(Integer priority) {
        this.priority = priority;
    }
}
