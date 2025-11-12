package com.springboot.wooden.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.Objects;

@Entity
@Table(name ="ITEM_STOCK_TBL", uniqueConstraints = @UniqueConstraint(columnNames = {"item_no"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemStock {

    @Id
    @Column(name = "item_no")
    private Long itemNo;

    // Item 과 1:1 관계
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "item_no", nullable = false)
    private Item item;      // 상품

    @Column(name = "is_qty", nullable = false)
    private int isQty;      // 현재 재고

    @Column(name = "total_in", nullable = false)
    private int totalIn;    // 누적 재고 (=생산완료)

    @Column(name = "total_out", nullable = false)
    private int totalOut;   // 누적 출고 (=주문완료)

    // 연관 엔티티 교체 시 null 방지
    public void changeItem(Item item) {
        this.item = Objects.requireNonNull(item);
    }

    // 입고(생산완료) 처리: 현재재고 및 누적입고 증가
    public void produce(int qty) {    // 입고(생산완료)
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        this.isQty += qty;
        this.totalIn += qty;
    }
    // 출고(납품완료) 처리: 현재재고 감소, 누적출고 증가. 재고 음수 방지
    public void sell(int qty) {       // 출고(주문완료)
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        int next = this.isQty - qty;
        if (next < 0) throw new IllegalStateException("재고가 부족합니다");
        this.isQty = next;
        this.totalOut += qty;
    }
    // 통합 증감 메서드
    public void applyDelta(int delta) {
        if (delta == 0) throw new IllegalArgumentException("delta must not be 0");
        if (delta > 0) produce(delta);
        else sell(-delta);
    }
}

// 완제품 재고 엔티티 Item과 1:1 공유 PK로 묶고, 현재재고/누적입고/누적출고를 일관성 있게 증가·감소시키는 메서드만 노출
