package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 学习库存货主数据实体，映射表 {@code learning_item}。
 * <p>
 * <b>职责</b>：承载只读查询 queryItem 的行数据（编码 / 名称 / 规格）。
 * <p>
 * <b>只读 / 禁写</b>：工具域仅通过 Mapper select 读取本表；无工具负责 insert/update。
 * 不提供写工具是为了避免 AI 改主数据。
 * <p>
 * <b>Day16 路径</b>：LearningDataMapper.selectItem → LearningDataService → QueryItemTool。
 * <p>
 * <b>上下游</b>：上游 Mapper；下游 Service 映射为 LearningFakeData.Item。
 */
@TableName("learning_item")
public class LearningItem {

    /** 存货编码（主键） */
    @TableId
    private String itemCode;
    /** 存货名称 */
    private String name;
    /** 规格 */
    private String spec;

    /** @return 存货编码 */
    public String getItemCode() { return itemCode; }
    /** @param itemCode 存货编码 */
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    /** @return 名称 */
    public String getName() { return name; }
    /** @param name 名称 */
    public void setName(String name) { this.name = name; }
    /** @return 规格 */
    public String getSpec() { return spec; }
    /** @param spec 规格 */
    public void setSpec(String spec) { this.spec = spec; }
}
