package com.macro.mall.ai.guide.tool;

import com.macro.mall.ai.guide.domain.ProductInfo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 导购 Agent 工具集 (Shopping Tools)
 *
 * <p>使用 Spring AI {@link Tool} 声明可被模型调用的函数，
 * 由模型在对话中自主决定是否调用、调用顺序与参数，
 * 覆盖电商导购核心动作：商品检索、库存查询、到手价计算。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Component
public class ShoppingTools {

    private final ProductCatalog catalog;

    public ShoppingTools(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @Tool(name = "searchProducts",
        description = "根据用户的关键词、品类、用途或预算搜索商品，返回匹配的商品列表（含名称、价格、卖点、库存）。当用户想找商品或需要推荐时调用。")
    public List<ProductInfo> searchProducts(
        @ToolParam(description = "搜索关键词，例如：跑步、耳机、降噪、平板、预算300") String keyword) {
        return catalog.search(keyword, 5);
    }

    @Tool(name = "checkStock",
        description = "查询指定商品的实时库存数量。当用户关心是否有货、能否下单时调用。")
    public int checkStock(
        @ToolParam(description = "商品ID") Long productId) {
        return catalog.stockOf(productId);
    }

    @Tool(name = "calcFinalPrice",
        description = "计算商品在指定优惠券下的到手价（已包含满减）。当用户询问价格、优惠、到手价时调用。")
    public BigDecimal calcFinalPrice(
        @ToolParam(description = "商品ID") Long productId,
        @ToolParam(required = false, description = "优惠券码，例如 SAVE50、NEW10；没有可留空") String couponCode) {
        return catalog.finalPrice(productId, couponCode);
    }
}
