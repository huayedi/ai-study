package com.erp.ai.tool.mapper;

import com.erp.ai.tool.entity.LearningInventory;
import com.erp.ai.tool.entity.LearningItem;
import com.erp.ai.tool.entity.LearningPeriod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学习主数据 MyBatis Mapper（仅 SELECT）。
 * <p>
 * <b>职责</b>：声明 learning_item / learning_inventory / learning_period 的按键查询，
 * SQL 位于对应 XML。
 * <p>
 * <b>只读 / 禁写</b>：接口<strong>故意不声明</strong> insert/update/delete——
 * 从 Mapper 层保证工具域不能改业务学习表；无写工具的根本原因是助手职责止于答疑查数。
 * <p>
 * <b>Day16 路径</b>：LearningDataService → 本接口 → XML → MySQL。
 * <p>
 * <b>上下游</b>：上游 Service；下游 MyBatis XML。
 */
@Mapper
public interface LearningDataMapper {

    /**
     * 按存货编码查主数据行。
     *
     * @param itemCode 存货编码
     * @return 行或 null
     */
    LearningItem selectItem(@Param("itemCode") String itemCode);

    /**
     * 按存货+仓库查现存行。
     *
     * @param itemCode  存货编码
     * @param warehouse 仓库
     * @return 行或 null
     */
    LearningInventory selectInventory(@Param("itemCode") String itemCode,
                                      @Param("warehouse") String warehouse);

    /**
     * 按公司+期间查状态行。
     *
     * @param company 公司
     * @param period  期间
     * @return 行或 null
     */
    LearningPeriod selectPeriod(@Param("company") String company,
                                @Param("period") String period);
}
