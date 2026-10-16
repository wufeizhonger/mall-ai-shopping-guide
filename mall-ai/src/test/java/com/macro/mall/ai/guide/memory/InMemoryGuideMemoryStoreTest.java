package com.macro.mall.ai.guide.memory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 会话记忆单元测试：追加、读取、窗口裁剪、会话隔离与清空。
 *
 * @author zaixi
 * @since 2026-10
 */
class InMemoryGuideMemoryStoreTest {

    @Test
    @DisplayName("追加与读取：按时间正序返回")
    void appendAndRead() {
        InMemoryGuideMemoryStore store = new InMemoryGuideMemoryStore(10);
        store.append("s1", "user", "你好");
        store.append("s1", "assistant", "您好，请问需要什么？");

        List<ConversationTurn> turns = store.recent("s1");
        assertThat(turns).hasSize(2);
        assertThat(turns.get(0)).isEqualTo(new ConversationTurn("user", "你好"));
        assertThat(turns.get(1).role()).isEqualTo("assistant");
    }

    @Test
    @DisplayName("窗口裁剪：超过最大轮数时丢弃最旧，保留最新")
    void slidingWindow() {
        InMemoryGuideMemoryStore store = new InMemoryGuideMemoryStore(3);
        for (int i = 1; i <= 5; i++) {
            store.append("s1", "user", "msg-" + i);
        }
        List<ConversationTurn> turns = store.recent("s1");
        assertThat(turns).hasSize(3);
        assertThat(turns).extracting(ConversationTurn::content)
            .containsExactly("msg-3", "msg-4", "msg-5");
    }

    @Test
    @DisplayName("会话隔离：不同会话互不可见")
    void sessionIsolation() {
        InMemoryGuideMemoryStore store = new InMemoryGuideMemoryStore(10);
        store.append("s1", "user", "s1-content");
        store.append("s2", "user", "s2-content");

        assertThat(store.recent("s1")).hasSize(1);
        assertThat(store.recent("s2")).hasSize(1);
        assertThat(store.recent("s1").get(0).content()).isEqualTo("s1-content");
    }

    @Test
    @DisplayName("清空：clear 后历史为空")
    void clear() {
        InMemoryGuideMemoryStore store = new InMemoryGuideMemoryStore(10);
        store.append("s1", "user", "content");
        store.clear("s1");
        assertThat(store.recent("s1")).isEmpty();
    }

    @Test
    @DisplayName("空参数：null 安全处理")
    void nullSafe() {
        InMemoryGuideMemoryStore store = new InMemoryGuideMemoryStore(10);
        store.append(null, null, null);
        assertThat(store.recent("any")).isEmpty();
    }
}
