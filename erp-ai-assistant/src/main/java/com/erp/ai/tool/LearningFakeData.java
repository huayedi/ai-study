package com.erp.ai.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Day16：学习假数据常量（单测 / 内存回退）。
 * <p>
 * 运行时默认从学习库 MySQL 表 {@code learning_*} 读取（见 {@code docs/MYSQL.md}）。
 * <pre>
 * ITEM-A001 @ 原料仓 → 120
 * ITEM-A001 @ 成品仓 → 0
 * 期间 2026-08 @ 主公司 → OPEN
 * </pre>
 */
public final class LearningFakeData {

    public record Item(String itemCode, String name, String spec) {
    }

    public record Inventory(String itemCode, String warehouse, double qty) {
    }

    public record PeriodStatus(String company, String period, String status) {
    }

    private static final Map<String, Item> ITEMS = Map.of(
            "ITEM-A001", new Item("ITEM-A001", "A001 原材料", "规格-学习假数据"),
            "A001", new Item("A001", "A001 原材料", "规格-学习假数据（别名编码）")
    );

    private static final Map<String, Inventory> INVENTORY = buildInventory();

    private static final Map<String, PeriodStatus> PERIODS = Map.of(
            key("主公司", "2026-08"), new PeriodStatus("主公司", "2026-08", "OPEN"),
            key("主公司", "2026-07"), new PeriodStatus("主公司", "2026-07", "CLOSED")
    );

    private LearningFakeData() {
    }

    private static Map<String, Inventory> buildInventory() {
        Map<String, Inventory> map = new LinkedHashMap<>();
        putInv(map, "ITEM-A001", "原料仓", 120);
        putInv(map, "ITEM-A001", "成品仓", 0);
        putInv(map, "A001", "原料仓", 120);
        putInv(map, "A001", "成品仓", 0);
        return Map.copyOf(map);
    }

    private static void putInv(Map<String, Inventory> map, String item, String wh, double qty) {
        map.put(key(item, wh), new Inventory(item, wh, qty));
    }

    private static String key(String a, String b) {
        return a.trim() + "|" + b.trim();
    }

    public static Optional<Item> findItem(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ITEMS.get(itemCode.trim()));
    }

    public static Optional<Inventory> findInventory(String itemCode, String warehouse) {
        if (itemCode == null || warehouse == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(INVENTORY.get(key(itemCode, warehouse)));
    }

    public static Optional<PeriodStatus> findPeriod(String company, String period) {
        if (company == null || period == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PERIODS.get(key(company, period)));
    }
}
