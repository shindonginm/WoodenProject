package com.springboot.wooden.service;

import com.springboot.wooden.dto.ForecastSeriesDto;

public interface ForecastService {
    ForecastSeriesDto getForecastSeries(Long itemNo, int horizonWeeks);

    int applyForecastToDemand(Long itemNo, int horizonWeeks);
}
