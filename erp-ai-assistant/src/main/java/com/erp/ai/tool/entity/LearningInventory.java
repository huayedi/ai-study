package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 学习库现存实体，映射表 {@code learning_inventory}。
 * <p>
 * <b>职责</b>：承载只读查询 queryInventory 的行数据（编码 / 仓库 / 数量）。
 * <p>
 * <b>只读 / 禁写</b>：工具只能查 qty，不能调库存。无 writeInventory 工具——
 * 改数量须人工 ERP，防止学习环境脏写。
 * <p>
 * <b>Day16 路径</b>：Mapper.selectInventory → Service → QueryInventoryTool。
 * <p>
 * <b>上下游</b>：上游 Mapper；下游 Service 映射为 LearningFakeData.Inventory。
 */
@TableName("learning_inventory")
public class LearningInventory {

    /** 存货编码 */
    private String itemCode;
    /** 仓库名称 */
    private String warehouse;
    /** 现存量 */
    private BigDecimal qty;

    /** @return 存货编码 */
    public String getItemCode() { return itemCode; }
    /** @param itemCode 存货编码 */
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    /** @return 仓库 */
    public String getWarehouse() { return warehouse; }
    /** @param warehouse 仓库 */
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    /** @return 数量 */
    public BigDecimal getQty() { return qty; }
    /** @param qty 数量 */
    public void setQty(BigDecimal qty) { this.qty = qty; }
}
