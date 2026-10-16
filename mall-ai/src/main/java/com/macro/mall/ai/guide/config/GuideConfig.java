package com.macro.mall.ai.guide.config;

import com.macro.mall.ai.guide.tool.ShoppingTools;
import com.macro.mall.ai.security.InputSanitizationAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 导购 Agent 配置 (Shopping Guide Config)
 *
 * <p>构建专用的 {@link ChatClient}：注入输入安全 Advisor 与
 * 导购工具集（Function Calling），并设置导购系统提示词。
 * 与主 ChatClient 隔离，互不影响。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Configuration
public class GuideConfig {

    private static final String GUIDE_SYSTEM = """
        你是电商平台的「智能导购助手」，目标是理解用户需求并推荐合适的商品，促成下单。
        要求：
        1. 主动询问关键信息（用途、预算、偏好），结合工具 searchProducts 检索真实商品，不要凭空编造商品；
        2. 涉及库存、价格、优惠、到手价时，分别调用 checkStock、calcFinalPrice 获取准确结果；
        3. 回答简洁、有条理，先给结论再说明理由，推荐时给出商品名称、价格与核心卖点；
        4. 若没有匹配商品，如实告知并给出替代建议。
        """;

    @Bean("guideChatClient")
    public ChatClient guideChatClient(ChatClient.Builder builder,
                                      ShoppingTools shoppingTools,
                                      InputSanitizationAdvisor sanitizer) {
        return builder
            .defaultAdvisors(sanitizer)
            .defaultTools(shoppingTools)
            .defaultSystem(GUIDE_SYSTEM)
            .build();
    }
}
