package com.erp.ai.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.common.entity.AiCallAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link AiCallAudit} 的 MyBatis Mapper。
 *
 * <h2>职责</h2>
 * 提供审计行插入与 Day22 聚合统计；同时继承 {@link BaseMapper} 获得通用 CRUD。
 *
 * <h2>为何既有 BaseMapper 又有自定义方法</h2>
 * 通用 CRUD 覆盖简单场景；{@link #insertAudit} / {@link #selectStats}
 * 的 SQL 写在 XML，便于控制字段默认值与聚合列别名。
 *
 * <h2>与 Spring 的关系</h2>
 * {@code @Mapper} + {@code MybatisPlusConfig} 的 {@code @MapperScan} 扫描注册。
 *
 * <h2>学习要点</h2>
 * 方法签名与 {@code resources/mapper} 下 XML 的 {@code id} 必须一一对应。
 */
@Mapper
public interface AiCallAuditMapper extends BaseMapper<AiCallAudit> {

    /**
     * 插入一条审计记录（自定义 XML，可含 created_at 等默认填充）。
     *
     * @param row 待插入实体；关键字段应由调用方填好
     * @return 影响行数；通常为 1
     */
    int insertAudit(AiCallAudit row);

    /**
     * Day22：聚合统计（总次数、成功数、平均耗时、Token 合计等）。
     * <p>
     * 返回 Map 的 key 由 XML 列别名决定；无数据时可能为 {@code null} 或空 Map（视驱动/XML）。
     *
     * @return 聚合结果 Map；调用方应做 null 防护
     */
    java.util.Map<String, Object> selectStats();
}
