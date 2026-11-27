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

    public enum Rule {
        SHEN,
        ZHONGZHOU
    }

    public static class Result {

        public Rule rule;

        public String originalMountain;
        public String adjacentMountain;

        public int originalStar = -1;

        public boolean replacementAngle;

        public XuanKongReplacementCalculator.ReplacementResult shenResult;

        public ZhongzhouReplacementRules.Result zhongzhouResult;

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

        if (result.rule == Rule.SHEN) {

            if (!replacementAngle) {
                result.description =
                        "当前处于下卦角度范围，不生成替卦候选盘";
                return result;
            }

            calculateShen(result);

        } else {

            calculateZhongzhou(result);
        }

        return result;
    }

    /**
     * 沈氏规则。
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

        if (shen.replacementStar < 1
                || shen.replacementStar > 9) {

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
                XuanKongCalculator.isValidNineStarChart(
                        result.starsA);

        result.availableB =
                XuanKongCalculator.isValidNineStarChart(
                        result.starsB);

        result.hasReplacement =
                result.availableA || result.availableB;

        result.description =
                "当前采用沈氏二十四山替星规则。\n"
                + (shen.description == null
                ? ""
                : shen.description);
    }

    /**
     * 中州规则。
     */
    private static void calculateZhongzhou(Result result) {

        if (result.adjacentMountain == null) {
            result.description =
                    "缺少实际角度识别山向，无法进行中州规则判断";
            return;
        }

        ZhongzhouReplacementRules.Result zhongzhou =
                ZhongzhouReplacementRules.calculateReplacement(
                        result.originalStar,
                        result.originalMountain,
                        result.adjacentMountain,
                        result.replacementAngle
                );

        result.zhongzhouResult = zhongzhou;

        if (zhongzhou == null) {
            result.description = "中州规则计算失败";
            return;
        }

        /*
         * 下卦、同宫同阴阳、输入无效等情况，
         * 不生成普通替卦候选盘。
         */
        if (!result.replacementAngle
                || !zhongzhou.hasReplacement
                || zhongzhou.replacementStar < 1
                || zhongzhou.replacementStar > 9) {

            result.hasReplacement = false;
            result.availableA = false;
            result.availableB = false;

            result.description =
                    zhongzhou.description == null
                    ? "当前状态不生成普通替卦候选盘"
                    : zhongzhou.description;

            return;
        }

        /*
         * 出卦：
         *
         * 保留中州派替星分析信息，
         * 但不生成普通候选九宫盘。
         */
        if (zhongzhou.status
                == ZhongzhouReplacementRules.Status.OUT_OF_PALACE) {

            result.hasReplacement = false;
            result.availableA = false;
            result.availableB = false;

            result.description =
                    "当前为出卦状态。\n"
                    + (zhongzhou.description == null
                    ? ""
                    : zhongzhou.description)
                    + "\n保留替星分析信息，不生成普通替卦候选盘。";

            return;
        }

        /*
         * 五黄特殊状态不生成普通替星盘。
         */
        if (zhongzhou.status
                == ZhongzhouReplacementRules.Status.FIVE_YELLOW_SPECIAL) {

            result.hasReplacement = false;
            result.availableA = false;
            result.availableB = false;

            result.description =
                    zhongzhou.description == null
                    ? "五黄特殊处理，不生成普通替星盘"
                    : zhongzhou.description;

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
                XuanKongCalculator.isValidNineStarChart(
                        result.starsA);

        result.availableB =
                XuanKongCalculator.isValidNineStarChart(
                        result.starsB);

        result.hasReplacement =
                result.availableA || result.availableB;

        result.description =
                "当前采用中州十三山替星规则。\n"
                + (zhongzhou.description == null
                ? ""
                : zhongzhou.description);
    }

    public static String getRuleName(Rule rule) {

        if (rule == Rule.ZHONGZHOU) {
            return "中州规则";
        }

        return "沈氏规则";
    }

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
                return "出卦，保留替星分析，不生成普通替卦盘";

            case FIVE_YELLOW_SPECIAL:
                return "五黄特殊处理，不生成普通替星";

            case NO_REPLACEMENT_STAR:
                return "无对应替星，不生成普通替卦盘";

            default:
                return "未知状态";
        }
    }

    private static String normalize(String mountain) {

        if (mountain == null) {
            return null;
        }

        String value = mountain.trim();

        return value.length() == 0 ? null : value;
    }
}
