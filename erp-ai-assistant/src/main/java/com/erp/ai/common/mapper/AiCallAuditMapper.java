package com.erp.ai.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.common.entity.AiCallAudit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiCallAuditMapper extends BaseMapper<AiCallAudit> {

    int insertAudit(AiCallAudit row);

    /** Day22：聚合统计 */
    java.util.Map<String, Object> selectStats();
}
