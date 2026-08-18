package com.erp.ai.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.tool.entity.ToolCallAudit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolCallAuditMapper extends BaseMapper<ToolCallAudit> {

    int insertAudit(ToolCallAudit row);
}
