package com.springboot.wooden.dto;

import lombok.*;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeeklyHistoryDto {
    private Long itemNo;
    private String itemName;
    private List<Point> history;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Point {
        private String date;
        private double qty;
    }
}
