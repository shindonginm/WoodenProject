package com.springboot.wooden.service;

import com.springboot.wooden.domain.DemandPlan;
import com.springboot.wooden.domain.Item;
import com.springboot.wooden.dto.ForecastSeriesDto;
import com.springboot.wooden.dto.WeeklyHistoryDto;
import com.springboot.wooden.repository.DemandPlanRepository;
import com.springboot.wooden.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastServiceImpl implements ForecastService {

    private final WeeklyHistoryService weeklyHistoryService;
    private final ForecastEngineClient engine;

    private final ItemRepository itemRepository;
    private final DemandPlanRepository demandPlanRepository;

    @Override
    public ForecastSeriesDto getForecastSeries(Long itemNo, int horizonWeeks) {
        WeeklyHistoryDto hist = weeklyHistoryService.getWeeklyHistory(itemNo, 52);

        List<ForecastSeriesDto.HistPoint> histPoints = hist.getHistory().stream()
                .map(p -> ForecastSeriesDto.HistPoint.builder()
                        .date(p.getDate())   // "yyyy-MM-dd"
                        .qty(p.getQty())
                        .build())
                .toList();

        List<ForecastSeriesDto.FcPoint> forecast = engine.forecastWeekly(histPoints, horizonWeeks);

        return ForecastSeriesDto.builder()
                .itemNo(hist.getItemNo())
                .itemName(hist.getItemName())
                .history(histPoints)
                .forecast(forecast)
                .build();
    }

    /**
     * ARIMA 예측 결과를 DemandPlan(수요 레저)에 저장
     * - ForecastSeriesDto.FcPoint.date: "yyyy-MM-dd" → LocalDate 로 parse
     * - FcPoint.mean: Double → 수요 int 로 반올림
     */
    @Override
    @Transactional
    public int applyForecastToDemand(Long itemNo, int horizonWeeks) {
        // 1) 예측 시리즈 확보
        ForecastSeriesDto series = getForecastSeries(itemNo, horizonWeeks);

        // 2) 품목 엔티티 조회
        Item item = itemRepository.findById(itemNo)
                .orElseThrow(() -> new IllegalArgumentException("item not found: " + itemNo));

        // 3) 기존 FORECAST 수요 삭제
        demandPlanRepository.deleteByItem_ItemNoAndSourceType(itemNo, "FORECAST");

        // 4) 문자열 날짜 파싱용 포맷터
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        int created = 0;

        for (ForecastSeriesDto.FcPoint fc : series.getForecast()) {

            if (fc.getDate() == null) continue;
            LocalDate deliDate = LocalDate.parse(fc.getDate(), fmt);

            double meanVal = (fc.getMean() != null) ? fc.getMean() : 0.0;
            int qty = (int) Math.round(meanVal);

            if (qty <= 0) {
                continue;
            }

            DemandPlan dp = DemandPlan.builder()
                    .item(item)
                    .deliDate(deliDate)
                    .qty(qty)
                    .sourceType("FORECAST")
                    .sourceId(null)
                    .priority(1)
                    .build();

            demandPlanRepository.save(dp);
            created++;
        }

        return created;
    }
}
