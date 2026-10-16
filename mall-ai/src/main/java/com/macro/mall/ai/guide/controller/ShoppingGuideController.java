package com.macro.mall.ai.guide.controller;

import com.macro.mall.ai.guide.domain.GuideChatRequest;
import com.macro.mall.ai.guide.domain.GuideChatResponse;
import com.macro.mall.ai.guide.service.ShoppingGuideService;
import com.macro.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能导购控制器 (Shopping Guide Controller)
 *
 * <p>基于 Function Calling 的多轮导购对话，支持同步与 SSE 流式两种方式。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@RestController
@Tag(name = "ShoppingGuideController", description = "AI智能导购（Function Calling）")
@RequestMapping("/ai/guide")
@RequiredArgsConstructor
public class ShoppingGuideController {

    private final ShoppingGuideService shoppingGuideService;

    @Operation(summary = "导购对话", description = "多轮导购对话，返回导购回复与推荐商品")
    @PostMapping("/chat")
    public CommonResult<GuideChatResponse> chat(@Valid @RequestBody GuideChatRequest request) {
        return CommonResult.success(shoppingGuideService.chat(request));
    }

    @Operation(summary = "导购对话（流式）", description = "SSE 流式返回导购回复，打字机效果")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody GuideChatRequest request) {
        return shoppingGuideService.stream(request);
    }
}
