package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/**
 * 单测 / 无库场景的内存回退 {@link LearningDataRepository}。
 * <p>
 * <b>职责</b>：原样委托 {@link LearningFakeData} 静态只读查找，不访问 MySQL。
 * <p>
 * <b>只读 / 禁写</b>：无写入方法；假数据 Map 不可变。不用于生产写账。
 * <p>
 * <b>Day16 路径</b>：单测直接 new 本类注入 Handler，或作为降级实现。
 * <p>
 * <b>上下游</b>：上游测试/可选装配；下游 LearningFakeData。
 */
public class MemoryLearningDataRepository implements LearningDataRepository {

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LearningFakeData.Item> findItem(String itemCode) {
        return LearningFakeData.findItem(itemCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse) {
        return LearningFakeData.findInventory(itemCode, warehouse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period) {
        return LearningFakeData.findPeriod(company, period);
    }
}
