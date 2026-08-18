package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/** 单测内存回退。 */
public class MemoryLearningDataRepository implements LearningDataRepository {

    @Override
    public Optional<LearningFakeData.Item> findItem(String itemCode) {
        return LearningFakeData.findItem(itemCode);
    }

    @Override
    public Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse) {
        return LearningFakeData.findInventory(itemCode, warehouse);
    }

    @Override
    public Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period) {
        return LearningFakeData.findPeriod(company, period);
    }
}
