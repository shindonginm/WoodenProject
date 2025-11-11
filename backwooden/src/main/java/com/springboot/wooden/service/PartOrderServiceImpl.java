package com.springboot.wooden.service;

import com.springboot.wooden.domain.*;
import com.springboot.wooden.dto.PartOrderRequestDto;
import com.springboot.wooden.dto.PartOrderResponseDto;
import com.springboot.wooden.repository.BuyerRepository;
import com.springboot.wooden.repository.PartOrderRepository;
import com.springboot.wooden.repository.PartRepository;
import com.springboot.wooden.repository.PartStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartOrderServiceImpl implements PartOrderService {

    private final PartOrderRepository partOrderRepository;
    private final BuyerRepository buyerRepository;
    private final PartRepository partRepository;
    private final PartStockRepository partStockRepository;

    @Override
    public List<PartOrderResponseDto> getAll() {
        return partOrderRepository.findByPoStateNot("입고완료")
                .stream().map(this::toDto).toList();
    }

    public List<PartOrderResponseDto> getCompletedList() {
        return partOrderRepository.findByPoState("입고완료")
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public PartOrderResponseDto addPartOrder(PartOrderRequestDto dto) {
        Buyer buyer = buyerRepository.findById(dto.getBuyerNo())
                .orElseThrow(() -> new IllegalArgumentException("거래처 없음: " + dto.getBuyerNo()));
        Part part = partRepository.findById(dto.getPartNo())
                .orElseThrow(() -> new IllegalArgumentException("부품 없음: " + dto.getPartNo()));

        if (!"입고대기".equals(dto.getPoState())) {
            throw new IllegalArgumentException("등록은 '입고대기' 상태로만 가능합니다.");
        }
        if (dto.getPoDate() == null) {
            throw new IllegalArgumentException("입고일자는 필수입니다.");
        }

        PartOrder saved = partOrderRepository.save(PartOrder.builder()
                .buyer(buyer)
                .part(part)
                .poQty(dto.getPoQty())
                .poPrice(dto.getPoPrice())
                .poState(dto.getPoState())
                .poDate(dto.getPoDate())
                .buyerAddr(dto.getBuyerAddr())
                // 스냅샷
                .buyerCompSnap(buyer.getBuyerComp())
                .partNameSnap(part.getPartName())
                .build());

        return toDto(saved);
    }

    @Override
    @Transactional
    public PartOrderResponseDto updatePartOrder(Long poNo, PartOrderRequestDto dto) {
        PartOrder po = partOrderRepository.findById(poNo)
                .orElseThrow(() -> new IllegalArgumentException("발주 없음: " + poNo));

        Buyer buyer = buyerRepository.findById(dto.getBuyerNo())
                .orElseThrow(() -> new IllegalArgumentException("거래처 없음: " + dto.getBuyerNo()));
        Part part = partRepository.findById(dto.getPartNo())
                .orElseThrow(() -> new IllegalArgumentException("부품 없음: " + dto.getPartNo()));

        String before = po.getPoState();
        String after  = dto.getPoState();

        po.changeBuyer(buyer);
        po.changePart(part);
        po.changeBuyerCompSnap(buyer != null ? buyer.getBuyerComp() : po.getBuyerCompSnap());
        po.changePartNameSnap(part != null ? part.getPartName() : po.getPartNameSnap());
        po.changePoQty(dto.getPoQty());
        po.changePoPrice(dto.getPoPrice());
        po.changePoState(after);
        po.changePoDate(dto.getPoDate());      // poDate = 입고일자
        po.changeBuyerAddr(dto.getBuyerAddr());

        if (!"입고완료".equals(before) && "입고완료".equals(after)) {
            var ps = getOrCreatePartStock(part);      // 없으면 생성해서 0부터 시작
            ps.changeQty(+po.getPoQty());             // @Version이 있으면 낙관적락 적용됨

            if (po.getPoDate() == null) {
                po.changePoDate(java.time.LocalDate.now());
            }
        }
        return toDto(po);
    }

    @Override
    @Transactional
    public void deletePartOrder(Long poNo) {
        partOrderRepository.deleteById(poNo);
    }

    /** 재고행 없으면 0으로 생성 (공유PK = partNo) */
    private PartStock getOrCreatePartStock(Part part) {
        Long partNo = part.getPartNo();
        return partStockRepository.findById(partNo)
                .orElseGet(() -> partStockRepository.save(
                        PartStock.builder()
                                .psNo(partNo)
                                .part(part)
                                .psQty(0)
                                .build()
                ));
    }

    @Override
    @Transactional
    public PartOrderResponseDto addFromPlan(PlannedOrder po) {

        if (!"BUYER".equals(po.getPlanType())) {
            throw new IllegalArgumentException("BUYER 타입 계획만 발주로 생성할 수 있습니다. planType=" + po.getPlanType());
        }

        var part = po.getPart();
        if (part == null) {
            throw new IllegalStateException("BUYER 계획인데 part 정보가 없습니다. planId=" + po.getId());
        }

        var buyer = part.getBuyer();

        if (buyer == null) {
            throw new IllegalStateException("부품에 연결된 구매처가 없습니다. partNo=" + part.getPartNo());
        }

        int qty = po.getQty();
        int unitPrice = part.getPartPrice();
        int totalPrice = unitPrice * qty;

        PartOrderRequestDto dto = PartOrderRequestDto.builder()
                .buyerNo(buyer.getBuyerNo())
                .partNo(part.getPartNo())
                .poQty(qty)
                .poPrice(totalPrice)
                .poState("입고대기")
                .poDate(po.getDeliDate())
                .buyerAddr(buyer.getBuyerAddr())
                .build();

        return addPartOrder(dto);
    }

    private PartOrderResponseDto toDto(PartOrder po) {
        String buyerComp = (po.getBuyer() != null)
                ? po.getBuyer().getBuyerComp()
                : po.getBuyerCompSnap();

        String partName = (po.getPart() != null)
                ? po.getPart().getPartName()
                : po.getPartNameSnap();

        return PartOrderResponseDto.builder()
                .poNo(po.getPoNo())
                .buyerComp(buyerComp)
                .partName(partName)
                .poQty(po.getPoQty())
                .poPrice(po.getPoPrice())
                .poState(po.getPoState())
                .poDate(po.getPoDate())
                .buyerAddr(po.getBuyerAddr())
                .build();
    }
}
