package com.erp.ai.store;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/**
 * 内存回退（单测 / store=memory）。生产学习默认走 {@link JdbcLearningDataRepository}。
 */
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
