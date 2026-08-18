package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 学习库会计期间实体，映射表 {@code learning_period}。
 * <p>
 * <b>职责</b>：承载只读查询 queryPeriodStatus 的行数据（公司 / 期间 / 状态）。
 * <p>
 * <b>只读 / 禁写</b>：工具不得 close/open 期间；无对应写工具，因开关账属高风险财务动作。
 * <p>
 * <b>Day16 路径</b>：Mapper.selectPeriod → Service → QueryPeriodStatusTool。
 * <p>
 * <b>上下游</b>：上游 Mapper；下游 Service 映射为 LearningFakeData.PeriodStatus。
 */
@TableName("learning_period")
public class LearningPeriod {

    /** 公司名（学习种子常用「主公司」） */
    private String company;
    /** 期间，如 2026-08 */
    private String period;
    /** 状态 OPEN / CLOSED 等 */
    private String status;

    /** @return 公司 */
    public String getCompany() { return company; }
    /** @param company 公司 */
    public void setCompany(String company) { this.company = company; }
    /** @return 期间 */
    public String getPeriod() { return period; }
    /** @param period 期间 */
    public void setPeriod(String period) { this.period = period; }
    /** @return 状态 */
    public String getStatus() { return status; }
    /** @param status 状态 */
    public void setStatus(String status) { this.status = status; }
}
