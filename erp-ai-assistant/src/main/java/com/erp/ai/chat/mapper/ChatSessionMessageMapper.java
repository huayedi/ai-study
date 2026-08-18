package com.erp.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.chat.entity.ChatSessionMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMessageMapper extends BaseMapper<ChatSessionMessage> {

    List<ChatSessionMessage> selectBySessionId(@Param("sessionId") String sessionId);

    int insertMessage(ChatSessionMessage row);

    int countBySessionId(@Param("sessionId") String sessionId);

    List<Long> selectOldestIds(@Param("sessionId") String sessionId, @Param("limit") int limit);

    int deleteMessageById(@Param("id") Long id);
}
