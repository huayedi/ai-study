package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;
import com.erp.ai.tool.entity.LearningInventory;
import com.erp.ai.tool.entity.LearningItem;
import com.erp.ai.tool.entity.LearningPeriod;
import com.erp.ai.tool.mapper.LearningDataMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 学习主数据服务：通过 MyBatis XML 读 MySQL learning_* 表。
 */
@Service
public class LearningDataService implements LearningDataRepository {

    private final LearningDataMapper learningDataMapper;

    public LearningDataService(LearningDataMapper learningDataMapper) {
        this.learningDataMapper = learningDataMapper;
    }

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
