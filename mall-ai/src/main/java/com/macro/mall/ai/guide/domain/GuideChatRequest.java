package com.macro.mall.ai.guide.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 导购对话请求 (Shopping Guide Chat Request)
 *
 * @author zaixi
 * @since 2026-10
 */
@Schema(description = "AI 导购对话请求")
public record GuideChatRequest(

    @Size(max = 64, message = "会话ID长度不能超过64字符")
    @Schema(description = "会话ID，用于多轮对话；为空时服务端生成", example = "sess-20261016-0001")
    String sessionId,

    @NotBlank(message = "消息不能为空")
    @Size(max = 500, message = "消息长度不能超过500字符")
    @Schema(description = "用户消息", example = "想跑步用，预算300以内，有什么推荐？")
    String message,

    @Size(max = 32, message = "优惠券码长度不能超过32字符")
    @Schema(description = "优惠券码（可选），用于计算到手价", example = "SAVE50")
    String couponCode
) {
}
