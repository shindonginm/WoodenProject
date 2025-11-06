package com.springboot.wooden.repository;

import com.springboot.wooden.domain.Item;
import com.springboot.wooden.domain.Plan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlanRepositoryTest {

    @Autowired PlanRepository planRepository;
    @Autowired ItemRepository itemRepository;

    /** 테스트용 아이템 번호(실 DB에 있어야 함) */
    private final Long itemNoA = 1L;  // TODO: 존재하는 item_no로 변경
    private final Long itemNoB = 6L;  // TODO: 존재하는 item_no로 변경 (changeItem 테스트용)

    // ----------------------------------------------------------------------
    // 1) 생성: 기존 Item FK에 Plan 저장
    // ----------------------------------------------------------------------
    @Test
    void createPlan() {
        Item item = itemRepository.getReferenceById(itemNoA);

        Plan plan = Plan.builder()
                .item(item)                      // ★ FK 세팅
                .planQty(100)
                .planState("생산중")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(7))
                .build();

        Plan saved = planRepository.save(plan);
        System.out.println("planNo = " + saved.getPlanNo());
    }

    // ----------------------------------------------------------------------
    // 2) 조회: 단건 조회
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("Plan 단건 조회")
    void readPlan() {
        // 사전 조건: 하나 저장
        Item itemRef = itemRepository.getReferenceById(itemNoA);
        Plan saved = planRepository.save(Plan.builder()
                .item(itemRef)
                .planQty(50)
                .planState("SCHEDULED")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(3))
                .build());

        Plan found = planRepository.findById(saved.getPlanNo()).orElseThrow();
        assertThat(found.getPlanNo()).isEqualTo(saved.getPlanNo());
        assertThat(found.getItem().getItemNo()).isEqualTo(itemNoA);
        assertThat(found.getPlanState()).isEqualTo("SCHEDULED");
    }

    // ----------------------------------------------------------------------
    // 3) 수정: 수량/상태/기간 변경
    // ----------------------------------------------------------------------
    @Test
    @Transactional
    @DisplayName("Plan 수정: 수량/상태/기간 변경")
    void updatePlanFields() {
        Item itemRef = itemRepository.getReferenceById(itemNoA);
        Plan saved = planRepository.save(Plan.builder()
                .item(itemRef)
                .planQty(10000)
                .planState("SCHEDULED")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(1))
                .build());

        // 엔티티 변경 메서드로 수정
        saved.changePlanQty(30);
        saved.changePlanState("IN_PROGRESS");
        saved.changePlanStartDate(LocalDate.now().minusDays(1));
        saved.changePlanEndDate(LocalDate.now().plusDays(10));

        // 영속 상태이므로 flush 시점에 UPDATE → 테스트에서는 트랜잭션 종료 시 반영
        Plan again = planRepository.findById(saved.getPlanNo()).orElseThrow();
        assertThat(again.getPlanQty()).isEqualTo(30);
        assertThat(again.getPlanState()).isEqualTo("IN_PROGRESS");
    }

    // ----------------------------------------------------------------------
    // 4) 연관 변경: 다른 Item으로 교체
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("Plan 수정: Item 연관 변경(changeItem)")
    void changeItem() {
        assertThat(itemRepository.existsById(itemNoA)).isTrue();
        assertThat(itemRepository.existsById(itemNoB)).isTrue();

        Item itemA = itemRepository.getReferenceById(itemNoA);
        Item itemB = itemRepository.getReferenceById(itemNoB);

        Plan saved = planRepository.save(Plan.builder()
                .item(itemA)
                .planQty(5)
                .planState("SCHEDULED")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(2))
                .build());

        // ★ Plan 엔티티에 changeItem(Item item) 추가해 두었어야 함
        saved.changeItem(itemB);

        Plan again = planRepository.findById(saved.getPlanNo()).orElseThrow();
        assertThat(again.getItem().getItemNo()).isEqualTo(itemNoB);
    }

    // ----------------------------------------------------------------------
    // 5) 삭제
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("Plan 삭제")
    void deletePlan() {
        Item itemRef = itemRepository.getReferenceById(itemNoA);
        Plan saved = planRepository.save(Plan.builder()
                .item(itemRef)
                .planQty(1)
                .planState("SCHEDULED")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(1))
                .build());

        Long planNo = saved.getPlanNo();
        planRepository.deleteById(planNo);

        assertThat(planRepository.existsById(planNo)).isFalse();
    }
}
