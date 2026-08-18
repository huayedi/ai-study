package com.erp.ai.tool.service;

import com.erp.ai.tool.LearningFakeData;

import java.util.Optional;

/**
 * 学习主数据只读仓储（tool 域隔离）。
 * <p>
 * <b>职责</b>：抽象存货 / 库存 / 期间三类只读查询，供三个 query* Handler 依赖。
 * 实现可为 MySQL（{@link LearningDataService}）或内存（{@link MemoryLearningDataRepository}）。
 * <p>
 * <b>只读 / 禁写</b>：接口<strong>仅</strong>定义 find* 方法，故意不包含 save/update/delete——
 * 从类型系统上杜绝写工具依赖写仓储；学习助手不得改账。
 * <p>
 * <b>Day16 路径</b>：Handler → 本接口 → Mapper/假数据。
 * <p>
 * <b>上下游</b>：上游 Handlers；下游具体实现类。
 */
public interface LearningDataRepository {

    /**
     * 按存货编码查主数据。
     *
     * @param itemCode 存货编码，空白由实现返回 empty
     * @return 命中 Optional，未命中 empty（不抛）
     */
    Optional<LearningFakeData.Item> findItem(String itemCode);

    /**
     * 按存货+仓库查现存量。
     *
     * @param itemCode  存货编码
     * @param warehouse 仓库
     * @return 命中 Optional，缺参或未命中 empty
     */
    Optional<LearningFakeData.Inventory> findInventory(String itemCode, String warehouse);

    /**
     * 按公司+期间查状态。
     *
     * @param company 公司
     * @param period  期间
     * @return 命中 Optional，缺参或未命中 empty
     */
    Optional<LearningFakeData.PeriodStatus> findPeriod(String company, String period);
}
