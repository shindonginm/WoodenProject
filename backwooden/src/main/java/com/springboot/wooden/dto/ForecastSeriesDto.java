package com.springboot.wooden.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForecastSeriesDto {
    private Long itemNo;
    private String itemName; // 옵션
    private List<HistPoint> history;     // 과거 실적(주간, 빈 주=0)
    private List<FcPoint>   forecast;    // 예측 결과

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistPoint {
        private String date;   // "yyyy-MM-dd" (주 시작일, 월요일)
        private Double qty;    // 실적 합계
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FcPoint {
        private String date;   // "yyyy-MM-dd" (주 시작일, 월요일)
        private Double mean;   // 평균 예측(=p50 대용 가능)
        private Double p10;
        private Double p50;
        private Double p90;
    }
}
