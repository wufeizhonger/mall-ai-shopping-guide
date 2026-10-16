package com.macro.mall.ai.guide.service;

import com.macro.mall.ai.guide.domain.GuideChatRequest;
import com.macro.mall.ai.guide.domain.GuideChatResponse;
import com.macro.mall.ai.guide.domain.ProductInfo;
import com.macro.mall.ai.guide.memory.ConversationTurn;
import com.macro.mall.ai.guide.memory.GuideMemoryStore;
import com.macro.mall.ai.guide.tool.ProductCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 导购 Agent 服务 (Shopping Guide Service)
 *
 * <p>编排导购 ChatClient、会话记忆与商品目录，提供同步与流式导购对话。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Service
public class ShoppingGuideService {

    private static final long SSE_TIMEOUT_MS = 60_000L;

    private final ChatClient guideChatClient;
    private final GuideMemoryStore memory;
    private final ProductCatalog catalog;

    public ShoppingGuideService(@Qualifier("guideChatClient") ChatClient guideChatClient,
                                GuideMemoryStore memory,
                                ProductCatalog catalog) {
        this.guideChatClient = guideChatClient;
        this.memory = memory;
        this.catalog = catalog;
    }

    /**
     * 同步导购对话
     */
    public GuideChatResponse chat(GuideChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        String userInput = buildUserInput(memory.recent(sessionId), request.message());

        String reply = guideChatClient.prompt()
            .user(userInput)
            .call()
            .content();

        memory.append(sessionId, "user", request.message());
        memory.append(sessionId, "assistant", reply);

        // 兜底：按用户消息检索商品，用于前端展示推荐卡片
        List<ProductInfo> products = catalog.search(request.message(), 3);
        return new GuideChatResponse(sessionId, reply, products);
    }

    /**
     * 流式导购对话（SSE，打字机效果）
     */
    public SseEmitter stream(GuideChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        String userInput = buildUserInput(memory.recent(sessionId), request.message());
        memory.append(sessionId, "user", request.message());

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        StringBuilder accumulated = new StringBuilder();

        guideChatClient.prompt()
            .user(userInput)
            .stream()
            .content()
            .subscribe(
                chunk -> {
                    accumulated.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    memory.append(sessionId, "assistant", accumulated.toString());
                    emitter.complete();
                });
        return emitter;
    }

    private static String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "sess-" + UUID.randomUUID();
        }
        return sessionId.trim();
    }

    private static String buildUserInput(List<ConversationTurn> history, String message) {
        if (history == null || history.isEmpty()) {
            return message;
        }
        StringBuilder sb = new StringBuilder("以下是与该用户的历史对话，请结合上下文继续：\n");
        for (ConversationTurn turn : history) {
            String role = "user".equals(turn.role()) ? "用户" : "导购";
            sb.append(role).append("：").append(turn.content()).append('\n');
        }
        sb.append("\n用户当前问题：").append(message);
        return sb.toString();
    }
}
