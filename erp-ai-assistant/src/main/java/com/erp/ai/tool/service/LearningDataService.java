package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;
import com.erp.ai.tool.entity.LearningInventory;
import com.erp.ai.tool.entity.LearningItem;
import com.erp.ai.tool.entity.LearningPeriod;
import com.erp.ai.tool.mapper.LearningDataMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 学习主数据服务：通过 MyBatis XML 读 MySQL {@code learning_*} 表。
 * <p>
 * <b>职责</b>：默认运行时 {@link LearningDataRepository} 实现；把实体行映射为
 * {@link LearningFakeData} 记录供 Handler 使用。
 * <p>
 * <b>只读 / 禁写</b>：仅调用 Mapper 的 select*；不封装 insert/update——
 * 因为工具域禁止写学习库业务数据（审计表写入在 ToolExecutor，不经本服务）。
 * <p>
 * <b>Day16 路径</b>：Handler → 本服务 → {@link LearningDataMapper} → MySQL。
 * <p>
 * <b>上下游</b>：上游 Spring 注入给 Handlers；下游 LearningDataMapper。
 */
@Service
public class LearningDataService implements LearningDataRepository {

    /** MyBatis 只读 Mapper */
    private final LearningDataMapper learningDataMapper;

    /**
     * @param learningDataMapper 学习表 Mapper
     */
    public LearningDataService(LearningDataMapper learningDataMapper) {
        this.learningDataMapper = learningDataMapper;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 空白编码直接 empty；行 null 亦 empty。
     */
    @Override
    public Optional<LearningFakeData.Item> findItem(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return Optional.empty();
        }
        LearningItem row = learningDataMapper.selectItem(itemCode.trim());
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(new LearningFakeData.Item(row.getItemCode(), row.getName(), row.getSpec()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * qty 为 null 视为无效行，返回 empty。
     */
    @Override
    public Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse) {
        if (itemCode == null || warehouse == null) {
            return Optional.empty();
        }
        LearningInventory row = learningDataMapper.selectInventory(itemCode.trim(), warehouse.trim());
        if (row == null || row.getQty() == null) {
            return Optional.empty();
        }
        return Optional.of(new LearningFakeData.Inventory(
                row.getItemCode(), row.getWarehouse(), row.getQty().doubleValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period) {
        if (company == null || period == null) {
            return Optional.empty();
        }
        LearningPeriod row = learningDataMapper.selectPeriod(company.trim(), period.trim());
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(new LearningFakeData.PeriodStatus(row.getCompany(), row.getPeriod(), row.getStatus()));
    }
}
