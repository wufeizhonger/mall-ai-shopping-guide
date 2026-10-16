package com.macro.mall.ai.guide.memory;

import java.util.List;

/**
 * 导购会话记忆存储 (Guide Memory Store)
 *
 * <p>按会话 ID 维护多轮对话历史。默认提供进程内滑动窗口实现，
 * 生产环境可替换为 Redis / JDBC 实现以支持多实例与持久化。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
public interface GuideMemoryStore {

    /**
     * 追加一轮对话
     *
     * @param sessionId 会话 ID
     * @param role      角色（user / assistant）
     * @param content   文本
     */
    void append(String sessionId, String role, String content);

    /**
     * 读取某会话最近的对话历史（已按时间正序，最新在末尾）
     *
     * @param sessionId 会话 ID
     * @return 对话轮次列表
     */
    List<ConversationTurn> recent(String sessionId);

    /**
     * 清空某会话
     *
     * @param sessionId 会话 ID
     */
    void clear(String sessionId);
}
