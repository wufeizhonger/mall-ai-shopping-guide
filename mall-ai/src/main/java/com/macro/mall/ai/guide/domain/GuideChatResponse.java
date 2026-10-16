package com.macro.mall.ai.guide.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 导购对话响应 (Shopping Guide Chat Response)
 *
 * @author zaixi
 * @since 2026-10
 */
@Schema(description = "AI 导购对话响应")
public record GuideChatResponse(

    @Schema(description = "会话ID，前端后续轮次原样回传", example = "sess-20261016-0001")
    String sessionId,

    @Schema(description = "导购回复内容", example = "推荐小米手环 9，支持运动模式与血氧监测……")
    String reply,

    @Schema(description = "本次对话引用/推荐的商品")
    List<ProductInfo> products
) {
}
