package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/**
 * 学习主数据只读仓储（tool 域隔离）。
 */
public interface LearningDataRepository {

    Optional<LearningFakeData.Item> findItem(String itemCode);

    Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse);

    Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period);
}
