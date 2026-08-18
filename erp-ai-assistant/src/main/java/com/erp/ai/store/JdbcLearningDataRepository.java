package com.erp.ai.store;

import com.erp.ai.tool.LearningFakeData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcLearningDataRepository implements LearningDataRepository {

    private final JdbcTemplate jdbc;

    public JdbcLearningDataRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<LearningFakeData.Item> findItem(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return Optional.empty();
        }
        List<LearningFakeData.Item> rows = jdbc.query(
                "SELECT item_code, name, spec FROM learning_item WHERE item_code = ?",
                (rs, i) -> new LearningFakeData.Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getString("spec")
                ),
                itemCode.trim()
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse) {
        if (itemCode == null || warehouse == null) {
            return Optional.empty();
        }
        List<LearningFakeData.Inventory> rows = jdbc.query(
                "SELECT item_code, warehouse, qty FROM learning_inventory WHERE item_code = ? AND warehouse = ?",
                (rs, i) -> new LearningFakeData.Inventory(
                        rs.getString("item_code"),
                        rs.getString("warehouse"),
                        rs.getBigDecimal("qty").doubleValue()
                ),
                itemCode.trim(),
                warehouse.trim()
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period) {
        if (company == null || period == null) {
            return Optional.empty();
        }
        List<LearningFakeData.PeriodStatus> rows = jdbc.query(
                "SELECT company, period, status FROM learning_period WHERE company = ? AND period = ?",
                (rs, i) -> new LearningFakeData.PeriodStatus(
                        rs.getString("company"),
                        rs.getString("period"),
                        rs.getString("status")
                ),
                company.trim(),
                period.trim()
        );
        return rows.stream().findFirst();
    }
}
