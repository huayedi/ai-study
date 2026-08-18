package com.erp.ai.tool.mapper;

import com.erp.ai.tool.entity.LearningInventory;
import com.erp.ai.tool.entity.LearningItem;
import com.erp.ai.tool.entity.LearningPeriod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearningDataMapper {

    LearningItem selectItem(@Param("itemCode") String itemCode);

    LearningInventory selectInventory(@Param("itemCode") String itemCode,
                                      @Param("warehouse") String warehouse);

    LearningPeriod selectPeriod(@Param("company") String company,
                                @Param("period") String period);
}
