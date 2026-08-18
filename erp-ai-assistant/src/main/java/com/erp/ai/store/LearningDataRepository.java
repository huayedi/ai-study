package com.erp.ai.store;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/**
 * 学习主数据只读仓储（假存货/库存/期间）。
 * 默认读 MySQL 测试库；无库时可用内存回退（仅单测兜底）。
 */
public interface LearningDataRepository {

    Optional<LearningFakeData.Item> findItem(String itemCode);

    Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse);

    Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period);
}
