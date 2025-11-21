package com.springboot.wooden.controller;

import com.springboot.wooden.dto.ForecastSeriesDto;
import com.springboot.wooden.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    /**
     * 예측 그래프용 (메인페이지)
     * GET /api/forecast/series?itemNo=1&h=12
     */
    @GetMapping("/series")
    public ForecastSeriesDto getSeries(@RequestParam Long itemNo,
                                       @RequestParam(name = "h", defaultValue = "12") int horizonWeeks) {
        return forecastService.getForecastSeries(itemNo, horizonWeeks);
    }

    /**
     * 🔹 예측 결과를 DemandPlan(수요 레저)에 저장
     * POST /api/forecast/apply?itemNo=1&h=12
     * 반환: 생성된 DemandPlan 건수(int)
     */
    @PostMapping("/apply")
    public int applyForecastToDemand(@RequestParam Long itemNo,
                                     @RequestParam(name = "h", defaultValue = "12") int horizonWeeks) {
        return forecastService.applyForecastToDemand(itemNo, horizonWeeks);
    }
}
