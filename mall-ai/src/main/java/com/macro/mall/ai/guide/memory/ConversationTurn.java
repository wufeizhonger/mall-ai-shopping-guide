package com.macro.mall.ai.guide.memory;

/**
 * 导购对话轮次 (Conversation Turn)
 *
 * @param role    角色，user / assistant
 * @param content 文本内容
 * @author zaixi
 * @since 2026-10
 */
public record ConversationTurn(String role, String content) {
}
