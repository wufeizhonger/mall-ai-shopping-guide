package com.macro.mall.ai.guide.memory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内滑动窗口会话记忆 (In-memory Sliding-window Guide Memory)
 *
 * <p>每个会话保留最近 {@code maxTurns} 轮对话，超出后从最旧开始丢弃，
 * 控制上下文长度与 Token 成本。线程安全，可直接用于并发请求。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Component
public class InMemoryGuideMemoryStore implements GuideMemoryStore {

    private final int maxTurns;
    private final Map<String, Deque<ConversationTurn>> sessions = new ConcurrentHashMap<>();

    public InMemoryGuideMemoryStore(
        @Value("${mall.ai.guide.memory.max-turns:10}") int maxTurns) {
        this.maxTurns = maxTurns > 0 ? maxTurns : 10;
    }

    @Override
    public void append(String sessionId, String role, String content) {
        if (sessionId == null || role == null || content == null) {
            return;
        }
        Deque<ConversationTurn> deque =
            sessions.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(new ConversationTurn(role, content));
            while (deque.size() > maxTurns) {
                deque.pollFirst();
            }
        }
    }

    @Override
    public List<ConversationTurn> recent(String sessionId) {
        Deque<ConversationTurn> deque = sessions.get(sessionId);
        if (deque == null) {
            return List.of();
        }
        synchronized (deque) {
            return new ArrayList<>(deque);
        }
    }

    @Override
    public void clear(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
}
