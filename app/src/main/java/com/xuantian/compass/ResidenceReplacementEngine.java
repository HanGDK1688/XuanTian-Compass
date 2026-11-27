package com.xuantian.compass;

/**
 * 住宅双流派替卦计算引擎。
 *
 * 沈氏规则：调用原有 XuanKongReplacementCalculator。
 * 中州规则：调用独立 ZhongzhouReplacementRules。
 *
 * 两套规则分别计算，结果不混合。
 */
public final class ResidenceReplacementEngine {

    private ResidenceReplacementEngine() {
    }

    /**
     * 当前采用的替卦规则。
     */
    public enum Rule {
        SHEN,
        ZHONGZHOU
    }

    /**
     * 单个坐山或朝向的计算结果。
     */
    public static class Result {

        public Rule rule;

        public String originalMountain;
        public String adjacentMountain;

        public int originalStar = -1;

        public boolean replacementAngle;

        // 沈氏计算结果
        public XuanKongReplacementCalculator.ReplacementResult shenResult;

        // 中州计算结果
        public ZhongzhouReplacementRules.Result zhongzhouResult;

        // 当前规则的方案A、B候选盘
        public int[] starsA;
        public int[] starsB;

        public boolean availableA;
        public boolean availableB;

        public boolean hasReplacement;

        public String description;

        public Result() {
        }
    }

    /**
     * 根据所选流派计算替卦。
     *
     * @param rule 当前规则
     * @param originalStar 原运星
     * @param originalMountain 原坐山或原朝向
     * @param adjacentMountain 实际兼入的相邻山
     * @param replacementAngle 是否进入替卦角度范围
     */
    public static Result calculate(
            Rule rule,
            int originalStar,
            String originalMountain,
            String adjacentMountain,
            boolean replacementAngle) {

        Result result = new Result();

        result.rule = rule == null ? Rule.SHEN : rule;
        result.originalStar = originalStar;
        result.originalMountain = normalize(originalMountain);
        result.adjacentMountain = normalize(adjacentMountain);
        result.replacementAngle = replacementAngle;

        if (originalStar < 1 || originalStar > 9) {
            result.description = "原运星数据无效";
            return result;
        }

        if (result.originalMountain == null) {
            result.description = "原山向数据无效";
            return result;
        }

        if (!replacementAngle) {
            result.description = "当前处于下卦角度范围，不生成替卦候选盘";
            return result;
        }

        if (result.rule == Rule.SHEN) {
            calculateShen(result);
        } else {
            calculateZhongzhou(result);
        }

        return result;
    }

    /**
     * 沈氏规则。
     *
     * 保留原有沈氏二十四山替星算法。
     */
    private static void calculateShen(Result result) {

        XuanKongReplacementCalculator.ReplacementResult shen =
                XuanKongReplacementCalculator.calculateReplacement(
                        result.originalStar,
                        result.originalMountain
                );

        result.shenResult = shen;

        if (shen == null) {
            result.description = "沈氏规则计算失败";
            return;
        }

        if (!shen.hasReplacement) {
            result.description = shen.description == null
                    ? "沈氏规则未找到有效替星"
                    : shen.description;
            return;
        }

        if (shen.replacementStar < 1 || shen.replacementStar > 9) {
            result.description = "沈氏替星数据无效";
            return;
        }

        result.starsA = XuanKongCalculator.flyFromCenter(
                shen.replacementStar,
                shen.methodAForward
        );

        result.starsB = XuanKongCalculator.flyFromCenter(
                shen.replacementStar,
                shen.methodBForward
        );

        result.availableA =
                XuanKongCalculator.isValidNineStarChart(result.starsA);

        result.availableB =
                XuanKongCalculator.isValidNineStarChart(result.starsB);

        result.hasReplacement =
                result.availableA || result.availableB;

        result.description = "当前采用沈氏二十四山替星规则。\n"
                + (shen.description == null ? "" : shen.description);
    }

    /**
     * 中州规则。
     *
     * 必须结合原山、实际相邻山和角度状态判断。
     */
    private static void calculateZhongzhou(Result result) {

        if (result.adjacentMountain == null) {
            result.description = "缺少实际兼入山向，无法进行中州规则判断";
            return;
        }

        ZhongzhouReplacementRules.Result zhongzhou =
                ZhongzhouReplacementRules.calculateReplacement(
                        result.originalStar,
                        result.originalMountain,
                        result.adjacentMountain,
                        true
                );

        result.zhongzhouResult = zhongzhou;

        if (zhongzhou == null) {
            result.description = "中州规则计算失败";
            return;
        }

        /*
         * 只有中州规则真正找到替星，
         * 才允许生成普通替卦候选盘。
         *
         * 下卦、同宫同阴阳、出卦、
         * 五黄特殊、无替星均不生成普通候选盘。
         */
        if (!zhongzhou.hasReplacement
                || zhongzhou.replacementStar < 1
                || zhongzhou.replacementStar > 9) {

            result.hasReplacement = false;
            result.availableA = false;
            result.availableB = false;

            result.description = zhongzhou.description;
            return;
        }

        /*
         * 中州方法A：正式方法。
         */
        result.starsA = XuanKongCalculator.flyFromCenter(
                zhongzhou.replacementStar,
                zhongzhou.methodAForward
        );

        /*
         * 中州方法B：比较方法。
         */
        result.starsB = XuanKongCalculator.flyFromCenter(
                zhongzhou.replacementStar,
                zhongzhou.methodBForward
        );

        result.availableA =
                XuanKongCalculator.isValidNineStarChart(result.starsA);

        result.availableB =
                XuanKongCalculator.isValidNineStarChart(result.starsB);

        result.hasReplacement =
                result.availableA || result.availableB;

        result.description =
                "当前采用中州十三山替星规则。\n"
                        + (zhongzhou.description == null
                        ? ""
                        : zhongzhou.description);
    }

    /**
     * 获取当前规则的名称。
     */
    public static String getRuleName(Rule rule) {

        if (rule == Rule.ZHONGZHOU) {
            return "中州规则";
        }

        return "沈氏规则";
    }

    /**
     * 获取中州规则状态的中文说明。
     */
    public static String getZhongzhouStatusText(
            ZhongzhouReplacementRules.Status status) {

        if (status == null) {
            return "暂无判断结果";
        }

        switch (status) {

            case INVALID_INPUT:
                return "输入数据无效";

            case DOWN_GUA:
                return "下卦，不需要替卦";

            case SAME_PALACE_SAME_YINYANG:
                return "同宫同阴阳，不替";

            case SAME_PALACE_DIFFERENT_YINYANG:
                return "同宫不同阴阳，进入替卦判断";

            case OUT_OF_PALACE:
                return "出卦，不生成普通替卦盘";

            case FIVE_YELLOW_SPECIAL:
                return "五黄特殊处理，不生成普通替星";

            case NO_REPLACEMENT_STAR:
                return "无对应替星，不生成普通替卦盘";

            default:
                return "未知状态";
        }
    }

    /**
     * 安全清理山名。
     */
    private static String normalize(String mountain) {

        if (mountain == null) {
            return null;
        }

        String value = mountain.trim();

        return value.length() == 0 ? null : value;
    }
}
