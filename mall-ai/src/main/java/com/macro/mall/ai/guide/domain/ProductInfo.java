package com.macro.mall.ai.guide.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * 导购商品信息 (Product Info for Shopping Guide)
 *
 * <p>导购 Agent 可向用户展示的商品视图，字段保持精简，
 * 生产环境可由 mall-portal 的 Feign 接口或商品 mapper 装配。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Schema(description = "导购商品信息")
public record ProductInfo(

    @Schema(description = "商品ID", example = "1")
    Long id,

    @Schema(description = "商品名称", example = "小米手环 9")
    String name,

    @Schema(description = "品牌", example = "小米")
    String brand,

    @Schema(description = "品类", example = "智能穿戴")
    String category,

    @Schema(description = "商品售价（元）", example = "249.00")
    BigDecimal price,

    @Schema(description = "实时库存", example = "120")
    int stock,

    @Schema(description = "商品副标题/卖点", example = "血氧监测 14天续航")
    String subtitle,

    @Schema(description = "标签，用于语义匹配", example = "[\"运动\",\"健康\",\"续航\"]")
    List<String> tags
) {
}
