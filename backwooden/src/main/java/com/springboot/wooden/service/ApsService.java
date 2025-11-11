package com.springboot.wooden.service;

import com.springboot.wooden.domain.PlannedOrder;

import java.util.List;

public interface ApsService {

    int runPlanning(int horizonWeeks);

    List<PlannedOrder> getPlannedOrders();

    Long release(Long planId);
}
