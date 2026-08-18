package com.erp.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.ai.chat.entity.ChatSessionMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话消息 MyBatis Mapper（chat 域数据访问）。
 * <p>
 * <b>职责</b>：对表 {@code chat_session_message} 提供按会话查询、插入、计数、取最旧 id、按 id 删除等方法；
 * SQL 定义在 classpath {@code mapper/chat/ChatSessionMessageMapper.xml}。
 * <p>
 * <b>在系统中的位置</b>：mapper 层，供 {@link com.erp.ai.chat.service.SessionStore} 调用；
 * Controller / Service 业务编排不直接拼 SQL。继承 {@link BaseMapper} 以保留 MyBatis-Plus 通用能力，
 * 本学习项目实际会话读写走下方自定义方法。
 * <p>
 * <b>对应学习 Day</b>：Week1 多轮会话持久化；与工具审计表（tool 域）分离。
 * <p>
 * <b>调用链 / 上下游</b>：
 * {@code SessionStore} → 本接口 → XML SQL → H2/MySQL 等库中的 {@code chat_session_message}。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>只操作会话消息表，不写 ERP 业务单据</li>
 *   <li>{@code selectBySessionId} 按 id 升序，保证历史时间顺序稳定</li>
 *   <li>{@code selectOldestIds} + {@code deleteMessageById} 配合做滑动窗口裁剪</li>
 * </ul>
 *
 * @see ChatSessionMessage
 * @see com.erp.ai.chat.service.SessionStore
 */
@Mapper
public interface ChatSessionMessageMapper extends BaseMapper<ChatSessionMessage> {

    /**
     * 按会话 ID 查询全部消息，按主键升序（时间先后）。
     * <p>
     * 业务含义：加载多轮历史以拼进 LLM messages；空会话返回空列表而非 null（MyBatis 常规行为）。
     *
     * @param sessionId 会话 ID，不可为业务空语义时仍可查询（结果为空）
     * @return 该会话下的消息行列表，按 id ASC
     */
    List<ChatSessionMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 插入一条会话消息；使用自增主键回填 {@link ChatSessionMessage#getId()}。
     * <p>
     * 边界：XML 只写 session_id / role / content；created_at 依赖库默认。
     *
     * @param row 待插入实体，需已设置 sessionId、role、content
     * @return 影响行数（成功一般为 1）
     */
    int insertMessage(ChatSessionMessage row);

    /**
     * 统计某会话当前消息条数，用于判断是否需要 trim。
     *
     * @param sessionId 会话 ID
     * @return 消息条数；无记录时为 0
     */
    int countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 选取某会话中最旧的若干条消息主键，供超限删除。
     * <p>
     * 排序：id ASC；条数由 {@code limit} 控制（通常为 count - max）。
     *
     * @param sessionId 会话 ID
     * @param limit     最多返回多少个最旧 id；应 &gt; 0，否则 SQL LIMIT 语义依赖驱动
     * @return 最旧消息的主键列表
     */
    List<Long> selectOldestIds(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * 按主键删除单条消息。
     *
     * @param id 消息主键
     * @return 影响行数（存在则为 1，否则 0）
     */
    int deleteMessageById(@Param("id") Long id);
}
