package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("learning_period")
public class LearningPeriod {

    private String company;
    private String period;
    private String status;

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
