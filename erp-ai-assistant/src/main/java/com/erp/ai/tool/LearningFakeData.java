package com.erp.ai.tool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Day16：学习假数据常量（单测 / 内存回退）。
 * <p>
 * <b>职责</b>：在无 MySQL 或单测场景下，提供与学习库种子一致的只读存货 / 库存 / 期间样本。
 * 运行时默认仍从学习库 MySQL 表 {@code learning_*} 读取（见 {@code docs/MYSQL.md}）；
 * 本类由 {@code MemoryLearningDataRepository} 委托。
 * <p>
 * <b>只读 / 禁写</b>：全部为不可变 Map / record，<strong>无任何写入 API</strong>。
 * 不提供 writeInventory 之类方法——学习助手不得改账；假数据仅用于演示查询答疑。
 * <p>
 * <b>Day16 路径</b>：Handler → Repository →（失败回退或单测）本常量。
 * <p>
 * <b>上下游</b>：上游 {@code LearningDataRepository} 实现；下游无。
 * <pre>
 * ITEM-A001 @ 原料仓 → 120
 * ITEM-A001 @ 成品仓 → 0
 * 期间 2026-08 @ 主公司 → OPEN
 * </pre>
 */
public final class LearningFakeData {

    /**
     * 存货主数据快照（编码 / 名称 / 规格）。
     *
     * @param itemCode 存货编码
     * @param name     名称
     * @param spec     规格说明
     */
    public record Item(
            /** 存货编码 */
            String itemCode,
            /** 存货名称 */
            String name,
            /** 规格 */
            String spec
    ) {
    }

    /**
     * 现存量子集（编码 + 仓库 → 数量）。
     *
     * @param itemCode  存货编码
     * @param warehouse 仓库名
     * @param qty       现存量
     */
    public record Inventory(
            /** 存货编码 */
            String itemCode,
            /** 仓库 */
            String warehouse,
            /** 数量 */
            double qty
    ) {
    }

    /**
     * 会计期间状态快照。
     *
     * @param company 公司
     * @param period  期间（yyyy-MM）
     * @param status  OPEN / CLOSED 等
     */
    public record PeriodStatus(
            /** 公司名 */
            String company,
            /** 期间 */
            String period,
            /** 状态 */
            String status
    ) {
    }

    /** 存货主数据表（不可变） */
    private static final Map<String, Item> ITEMS = Map.of(
            "ITEM-A001", new Item("ITEM-A001", "A001 原材料", "规格-学习假数据"),
            "A001", new Item("A001", "A001 原材料", "规格-学习假数据（别名编码）")
    );

    /** 库存表（不可变；含别名编码镜像） */
    private static final Map<String, Inventory> INVENTORY = buildInventory();

    /** 期间状态表（不可变） */
    private static final Map<String, PeriodStatus> PERIODS = Map.of(
            key("主公司", "2026-08"), new PeriodStatus("主公司", "2026-08", "OPEN"),
            key("主公司", "2026-07"), new PeriodStatus("主公司", "2026-07", "CLOSED")
    );

    /** 工具类：禁止实例化 */
    private LearningFakeData() {
    }

    /**
     * 构建库存假数据（原料仓 120 / 成品仓 0，含 A001 别名）。
     *
     * @return 不可变库存 Map
     */
    private static Map<String, Inventory> buildInventory() {
        Map<String, Inventory> map = new LinkedHashMap<>();
        putInv(map, "ITEM-A001", "原料仓", 120);
        putInv(map, "ITEM-A001", "成品仓", 0);
        putInv(map, "A001", "原料仓", 120);
        putInv(map, "A001", "成品仓", 0);
        return Map.copyOf(map);
    }

    /**
     * 向可变 Map 放入一条库存。
     *
     * @param map  目标 Map
     * @param item 存货编码
     * @param wh   仓库
     * @param qty  数量
     */
    private static void putInv(Map<String, Inventory> map, String item, String wh, double qty) {
        map.put(key(item, wh), new Inventory(item, wh, qty));
    }

    /**
     * 复合主键：两端 trim 后用 {@code |} 连接。
     *
     * @param a 第一段
     * @param b 第二段
     * @return 键字符串
     */
    private static String key(String a, String b) {
        return a.trim() + "|" + b.trim();
    }

    /**
     * 按编码查存货；空白编码返回 empty。
     *
     * @param itemCode 存货编码
     * @return 命中则有值，否则 empty（不抛异常）
     */
    public static Optional<Item> findItem(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ITEMS.get(itemCode.trim()));
    }

    /**
     * 按编码+仓库查现存量；任一为 null 返回 empty。
     *
     * @param itemCode  存货编码
     * @param warehouse 仓库
     * @return 命中则有值，否则 empty
     */
    public static Optional<Inventory> findInventory(String itemCode, String warehouse) {
        if (itemCode == null || warehouse == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(INVENTORY.get(key(itemCode, warehouse)));
    }

    /**
     * 按公司+期间查状态；任一为 null 返回 empty。
     *
     * @param company 公司
     * @param period  期间
     * @return 命中则有值，否则 empty
     */
    public static Optional<PeriodStatus> findPeriod(String company, String period) {
        if (company == null || period == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PERIODS.get(key(company, period)));
    }
}
