package com.erp.ai.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.tool.entity.ToolCallAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具调用审计 Mapper。
 * <p>
 * <b>职责</b>：向 {@code tool_call_audit} 插入一条审计行（XML {@code insertAudit}），
 * 并继承 MP {@link BaseMapper} 以备扩展查询。
 * <p>
 * <b>与「无写工具」的关系</b>：此处写入的是<strong>审计日志</strong>，不是库存/期间业务数据。
 * 业务写工具仍然不存在；Executor 在查询成败后旁路调用本 Mapper。
 * <p>
 * <b>Day16 / Day18</b>：凡经 ToolExecutor.run 的调用均可落审计。
 * <p>
 * <b>上下游</b>：上游 ToolExecutor；下游 XML insert。
 */
@Mapper
public interface ToolCallAuditMapper extends BaseMapper<ToolCallAudit> {

    /**
     * 插入一条审计记录。
     *
     * @param row 已填字段的实体（id/createdAt 可由 DB 生成）
     * @return 影响行数
     */
    int insertAudit(ToolCallAudit row);
}
