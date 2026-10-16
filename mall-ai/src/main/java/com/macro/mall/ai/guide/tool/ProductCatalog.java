package com.macro.mall.ai.guide.tool;

import com.macro.mall.ai.guide.domain.ProductInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 导购商品目录 (Product Catalog)
 *
 * <p>当前为进程内示例目录，提供商品检索、库存与到手价计算；
 * 方法均为纯逻辑，便于单测。后续可替换为 mall-portal Feign 或
 * 商品 mapper，而 {@link ShoppingTools} 与导购服务无需改动。</p>
 *
 * @author zaixi
 * @since 2026-10
 */
@Component
public class ProductCatalog {

    /** 满减门槛（元） */
    private static final BigDecimal FULL_REDUCTION_THRESHOLD = new BigDecimal("1000");
    /** 满减金额（元） */
    private static final BigDecimal FULL_REDUCTION_AMOUNT = new BigDecimal("100");
    /** 固定金额券 SAVE50 */
    private static final BigDecimal COUPON_SAVE50 = new BigDecimal("50");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final List<ProductInfo> products;

    public ProductCatalog() {
        this.products = List.of(
            new ProductInfo(1L, "小米手环 9", "小米", "智能穿戴",
                new BigDecimal("249.00"), 320, "血氧监测 14天超长续航",
                List.of("运动", "健康", "跑步", "续航", "手环")),
            new ProductInfo(2L, "Redmi Watch 5", "小米", "智能穿戴",
                new BigDecimal("499.00"), 150, "大屏血氧 独立GPS",
                List.of("运动", "健康", "跑步", "gps", "手表")),
            new ProductInfo(3L, "华为 FreeBuds 6i", "华为", "蓝牙耳机",
                new BigDecimal("399.00"), 210, "主动降噪 旗舰音质",
                List.of("耳机", "降噪", "音乐", "通勤")),
            new ProductInfo(4L, "韶音 OpenRun Air", "韶音", "蓝牙耳机",
                new BigDecimal("699.00"), 60, "骨传导 开放双耳 跑步专用",
                List.of("耳机", "骨传导", "运动", "跑步", "安全")),
            new ProductInfo(5L, "小米平板 Redmi Pad Pro", "小米", "平板电脑",
                new BigDecimal("1299.00"), 85, "12.1英寸大屏 学习办公",
                List.of("平板", "学习", "办公", "大屏")),
            new ProductInfo(6L, "Redmi Note 14 Pro", "小米", "手机",
                new BigDecimal("1499.00"), 140, "2亿像素 IP68防水",
                List.of("手机", "拍照", "5g", "续航")),
            new ProductInfo(7L, "Keep 体脂秤 S2", "Keep", "健康设备",
                new BigDecimal("129.00"), 400, "15项身体数据 APP互联",
                List.of("健康", "体脂", "体重", "健身")),
            new ProductInfo(8L, "佳明 Forerunner 165", "佳明", "智能穿戴",
                new BigDecimal("2180.00"), 25, "专业跑表 训练负荷分析",
                List.of("手表", "跑步", "专业", "gps", "训练"))
        );
    }

    /**
     * 按关键词（名称/品牌/品类/卖点/标签）检索商品，按匹配标签数与价格排序
     */
    public List<ProductInfo> search(String keyword, int limit) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Scored> matched = new ArrayList<>();
        for (ProductInfo p : products) {
            int score = matchScore(p, kw);
            if (score > 0) {
                matched.add(new Scored(p, score));
            }
        }
        matched.sort(Comparator.comparingInt((Scored s) -> s.score).reversed()
            .thenComparing(s -> s.product.price()));
        int n = limit <= 0 ? matched.size() : Math.min(limit, matched.size());
        List<ProductInfo> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(matched.get(i).product);
        }
        return result;
    }

    public ProductInfo findById(Long id) {
        if (id == null) {
            return null;
        }
        return products.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    /**
     * 查询实时库存；不存在的商品返回 -1
     */
    public int stockOf(Long id) {
        ProductInfo p = findById(id);
        return p == null ? -1 : p.stock();
    }

    /**
     * 计算指定商品在优惠券下的到手价（含满减）。
     *
     * <p>规则：先按价格判定满减（满 1000 减 100），再叠加优惠券；
     * SAVE50 为直减 50 元，NEW10 为 9 折券；结果保留两位且不低于 0。</p>
     *
     * @param productId  商品 ID
     * @param couponCode 优惠券码（可空）
     * @return 到手价；商品不存在返回 null
     */
    public BigDecimal finalPrice(Long productId, String couponCode) {
        ProductInfo p = findById(productId);
        if (p == null) {
            return null;
        }
        return applyPromotion(p.price(), couponCode);
    }

    /**
     * 纯函数：对给定原价应用满减与优惠券，便于单测覆盖各种组合。
     */
    public BigDecimal applyPromotion(BigDecimal originalPrice, String couponCode) {
        if (originalPrice == null) {
            return null;
        }
        BigDecimal price = originalPrice;
        if (price.compareTo(FULL_REDUCTION_THRESHOLD) >= 0) {
            price = price.subtract(FULL_REDUCTION_AMOUNT);
        }
        if (couponCode != null) {
            String code = couponCode.trim().toUpperCase(Locale.ROOT);
            switch (code) {
                case "SAVE50" -> price = price.subtract(COUPON_SAVE50);
                case "NEW10" -> price = price.multiply(new BigDecimal("0.9"));
                default -> { /* 未知券不生效 */ }
            }
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private static int matchScore(ProductInfo p, String kw) {
        if (kw.isEmpty()) {
            return 1; // 无关键词时返回全部
        }
        int score = 0;
        if (p.name().toLowerCase(Locale.ROOT).contains(kw)) score += 3;
        if (p.brand().toLowerCase(Locale.ROOT).contains(kw)) score += 2;
        if (p.category().toLowerCase(Locale.ROOT).contains(kw)) score += 2;
        if (p.subtitle().toLowerCase(Locale.ROOT).contains(kw)) score += 1;
        for (String tag : p.tags()) {
            if (tag.toLowerCase(Locale.ROOT).contains(kw)) score += 2;
        }
        return score;
    }

    private record Scored(ProductInfo product, int score) {
    }
}
