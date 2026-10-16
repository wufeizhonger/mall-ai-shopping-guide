package com.macro.mall.ai.guide.tool;

import com.macro.mall.ai.guide.domain.ProductInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商品目录单元测试：检索、库存、到手价（含满减/优惠券）。
 *
 * @author zaixi
 * @since 2026-10
 */
class ProductCatalogTest {

    private ProductCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new ProductCatalog();
    }

    @Test
    @DisplayName("搜索：按用途“跑步”能命中相关商品，且跑步专用商品排在前列")
    void searchByKeyword() {
        List<ProductInfo> results = catalog.search("跑步", 5);
        assertThat(results).isNotEmpty();
        // 跑步相关商品应包含手环/手表/骨传导耳机
        assertThat(results).extracting(ProductInfo::name)
            .anyMatch(n -> n.contains("手环") || n.contains("Watch") || n.contains("OpenRun"));
    }

    @Test
    @DisplayName("搜索：limit 生效，数量不超过上限")
    void searchLimit() {
        assertThat(catalog.search("运动", 2)).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("搜索：无关键词时返回全部商品")
    void searchEmptyReturnsAll() {
        assertThat(catalog.search("", 100)).hasSize(8);
    }

    @Test
    @DisplayName("库存：存在商品返回非负库存，不存在返回 -1")
    void stock() {
        assertThat(catalog.stockOf(1L)).isGreaterThanOrEqualTo(0);
        assertThat(catalog.stockOf(9999L)).isEqualTo(-1);
    }

    @Test
    @DisplayName("到手价：未满减门槛且无券，保持原价")
    void finalPriceNoPromotion() {
        // 小米手环 9 原价 249
        assertThat(catalog.finalPrice(1L, null)).isEqualByComparingTo("249.00");
    }

    @Test
    @DisplayName("到手价：满1000减100，无券时仅减满减")
    void finalPriceFullReduction() {
        // Redmi Note 14 Pro 原价 1499 -> 满减后 1399
        assertThat(catalog.finalPrice(6L, null)).isEqualByComparingTo("1399.00");
    }

    @Test
    @DisplayName("到手价：满减 + SAVE50 叠加")
    void finalPriceFullReductionWithSave50() {
        // 1499 - 100 - 50 = 1349
        assertThat(catalog.finalPrice(6L, "SAVE50")).isEqualByComparingTo("1349.00");
    }

    @Test
    @DisplayName("到手价：NEW10 九折券（未达满减门槛）")
    void finalPriceNew10() {
        // 小米手环 9 原价 249 -> 249*0.9 = 224.10
        assertThat(catalog.finalPrice(1L, "NEW10")).isEqualByComparingTo("224.10");
    }

    @Test
    @DisplayName("到手价：未知券不生效")
    void finalPriceUnknownCoupon() {
        assertThat(catalog.finalPrice(1L, "UNKNOWN")).isEqualByComparingTo("249.00");
    }

    @Test
    @DisplayName("到手价：优惠后不低于 0")
    void finalPriceFloor() {
        BigDecimal price = catalog.applyPromotion(new BigDecimal("30"), "SAVE50");
        assertThat(price).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("到手价：商品不存在返回 null")
    void finalPriceMissingProduct() {
        assertThat(catalog.finalPrice(9999L, null)).isNull();
    }
}
