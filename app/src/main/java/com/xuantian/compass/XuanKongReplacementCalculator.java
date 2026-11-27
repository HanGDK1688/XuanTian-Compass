package com.xuantian.compass;

import java.util.Locale;

public class XuanKongReplacementCalculator {

    // 每山15度
    public static final double MOUNTAIN_ANGLE = 15.0;

    // 下卦中心范围：左右各4.5度
    public static final double DOWN_GUA_LIMIT = 4.5;

    // 替卦外侧范围：左右各3度
    public static final double REPLACEMENT_LIMIT = 7.5;

    // 二十四山顺序
    private static final String[] MOUNTAINS = {
            "子", "癸", "丑",
            "艮", "寅", "甲",
            "卯", "乙", "辰",
            "巽", "巳", "丙",
            "午", "丁", "未",
            "坤", "申", "庚",
            "酉", "辛", "戌",
            "乾", "亥", "壬"
    };

    // 九宫顺序：
    // 中宫、乾、兑、艮、离、坎、坤、震、巽
    private static final String[][] PALACE_MOUNTAINS = {
            {},
            {"戌", "乾", "亥"},
            {"庚", "酉", "辛"},
            {"丑", "艮", "寅"},
            {"丙", "午", "丁"},
            {"壬", "子", "癸"},
            {"未", "坤", "申"},
            {"甲", "卯", "乙"},
            {"辰", "巽", "巳"}
    };

    // true为阳，false为阴
    // 地元龙、天元龙、人元龙
    private static final boolean[][] PALACE_YIN_YANG = {
            {},
            {false, true, true},
            {true, false, false},
            {false, true, true},
            {true, false, false},
            {true, false, false},
            {false, true, true},
            {true, false, false},
            {false, true, true}
    };

    /*
     * 二十四山替星表。
     *
     * 一白：子、甲、癸、申
     * 二黑：坤、壬、乙、卯、未
     * 六白：乾、戌、巳、辰、巽、亥
     * 七赤：艮、丑、丙、辛、酉
     * 九紫：寅、午、庚、丁
     *
     * 这是当前项目选定的替星数据。
     * 后续仍需结合具体替卦流派规则验证。
     */
    public static int getReplacementStar(String mountain) {

        if (mountain == null) {
            return -1;
        }

        switch (mountain.trim()) {

            case "子":
            case "甲":
            case "癸":
            case "申":
                return 1;

            case "坤":
            case "壬":
            case "乙":
            case "卯":
            case "未":
                return 2;

            case "乾":
            case "戌":
            case "巳":
            case "辰":
            case "巽":
            case "亥":
                return 6;

            case "艮":
            case "丑":
            case "丙":
            case "辛":
            case "酉":
                return 7;

            case "寅":
            case "午":
            case "庚":
            case "丁":
                return 9;

            default:
                return -1;
        }
    }

    public static class AngleResult {

        public double angle;
        public String mountain;
        public String previousMountain;
        public String nextMountain;

        public double centerAngle;
        public double deviation;

        public String classification;
        public String direction;
        public String description;

        public boolean isDownGua;
        public boolean isReplacementGua;
        public boolean isBoundary;

        public AngleResult() {
        }
    }

    public static class ReplacementResult {

        // 实际测量的原山
        public String originalMountain;

        // 原运星
        public int originalStar;

        // 按原运星原宫同元龙找到的替卦山
        public String replacementMountain;

        // 替星
        public int replacementStar;

        // 原运星所在宫
        public int originalPalace;

        // 替星所在宫
        public int replacementPalace;

        // 三元龙索引
        public int dragonIndex;

        public boolean originalPalaceYang;
        public boolean replacementPalaceYang;

        // 方案A、方案B顺逆
        public boolean methodAForward;
        public boolean methodBForward;

        public String methodA;
        public String methodB;

        public boolean hasReplacement;

        public String description;

        public ReplacementResult() {
        }
    }

    /**
     * 将角度规范到0至360度。
     */
    public static double normalizeAngle(double angle) {

        double result = angle % 360.0;

        if (result < 0) {
            result += 360.0;
        }

        return result;
    }

    /**
     * 计算两个方向之间的有符号角度差。
     */
    private static double signedAngleDifference(
            double angle,
            double center) {

        double difference = normalizeAngle(angle - center);

        if (difference > 180.0) {
            difference -= 360.0;
        }

        return difference;
    }

    /**
     * 根据罗盘角度判断二十四山。
     *
     * 0度为正北，顺时针增加。
     */
    public static AngleResult analyzeBearing(double bearing) {

        if (Double.isNaN(bearing)
                || Double.isInfinite(bearing)) {
            return null;
        }

        double angle = normalizeAngle(bearing);

        int index = (int) Math.floor(
                (angle + 7.5) / 15.0
        ) % 24;

        String mountain = MOUNTAINS[index];

        double center = index * 15.0;

        double deviation =
                signedAngleDifference(angle, center);

        AngleResult result = new AngleResult();

        result.angle = angle;
        result.mountain = mountain;

        result.previousMountain =
                MOUNTAINS[(index + 23) % 24];

        result.nextMountain =
                MOUNTAINS[(index + 1) % 24];

        result.centerAngle = center;
        result.deviation = deviation;

        if (Math.abs(deviation) <= DOWN_GUA_LIMIT) {

            result.classification = "下卦";
            result.description = "标准下卦范围";

            result.isDownGua = true;
            result.isReplacementGua = false;
            result.isBoundary = false;

            result.direction = "正向";

        } else if (Math.abs(deviation)
                < REPLACEMENT_LIMIT) {

            result.classification = "替卦";

            result.isDownGua = false;
            result.isReplacementGua = true;
            result.isBoundary = false;

            if (deviation > 0) {

                result.direction =
                        "兼" + result.nextMountain;

            } else {

                result.direction =
                        "兼" + result.previousMountain;
            }

            result.description = "进入替卦范围";

        } else {

            result.classification = "边界";

            result.isDownGua = false;
            result.isReplacementGua = false;
            result.isBoundary = true;

            result.direction = "边界待确认";

            result.description =
                    "位于山向分界边缘，需要人工复核";
        }

        return result;
    }

    /**
     * 获取二十四山索引。
     */
    public static int getMountainIndex(String mountain) {

        if (mountain == null) {
            return -1;
        }

        for (int i = 0; i < MOUNTAINS.length; i++) {

            if (MOUNTAINS[i].equals(mountain.trim())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 获取山所在宫位。
     */
    public static int getMountainPalace(String mountain) {

        if (mountain == null) {
            return -1;
        }

        for (int palace = 1; palace <= 8; palace++) {

            for (int i = 0; i < 3; i++) {

                if (PALACE_MOUNTAINS[palace][i]
                        .equals(mountain.trim())) {
                    return palace;
                }
            }
        }

        return -1;
    }

    /**
     * 获取某颗星的原始宫位。
     */
    public static int getStarOriginalPalace(int star) {

        switch (star) {

            case 1:
                return 5;

            case 2:
                return 6;

            case 3:
                return 7;

            case 4:
                return 8;

            case 5:
                return 0;

            case 6:
                return 1;

            case 7:
                return 2;

            case 8:
                return 3;

            case 9:
                return 4;

            default:
                return -1;
        }
    }

    /**
     * 获取山在所在宫位中的三元龙索引。
     *
     * 0：地元龙
     * 1：天元龙
     * 2：人元龙
     */
    public static int getDragonIndex(
            int palace,
            String mountain) {

        if (palace < 1 || palace > 8
                || mountain == null) {
            return -1;
        }

        for (int i = 0; i < 3; i++) {

            if (PALACE_MOUNTAINS[palace][i]
                    .equals(mountain.trim())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 获取某宫位指定三元龙的山。
     */
    public static String getMountainByDragon(
            int palace,
            int dragonIndex) {

        if (palace < 1 || palace > 8
                || dragonIndex < 0
                || dragonIndex > 2) {
            return null;
        }

        return PALACE_MOUNTAINS[palace][dragonIndex];
    }

    /**
     * 获取某宫位指定三元龙的阴阳。
     */
    public static boolean isYang(
            int palace,
            int dragonIndex) {

        if (palace < 1 || palace > 8
                || dragonIndex < 0
                || dragonIndex > 2) {

            throw new IllegalArgumentException(
                    "宫位或三元龙索引错误"
            );
        }

        return PALACE_YIN_YANG[palace][dragonIndex];
    }

    /**
     * 替卦计算。
     *
     * 第一步：
     * 根据原山确定三元龙。
     *
     * 第二步：
     * 根据原运星找到原运星所在宫。
     *
     * 第三步：
     * 在原运星所在宫，找到相同三元龙对应的山。
     *
     * 第四步：
     * 根据这座山查询替星。
     *
     * 方法A：
     * 按原运星原宫的同元龙阴阳判断顺逆。
     *
     * 方法B：
     * 按替星原宫的同元龙阴阳判断顺逆。
     *
     * 两种方法同时计算，供比较。
     */
    public static ReplacementResult calculateReplacement(
            int originalStar,
            String originalMountain) {

        if (originalStar < 1 || originalStar > 9) {
            return null;
        }

        int mountainIndex =
                getMountainIndex(originalMountain);

        if (mountainIndex < 0) {
            return null;
        }

        // 原山所在宫
        int originalMountainPalace =
                getMountainPalace(originalMountain);

        // 原山的三元龙
        int dragonIndex =
                getDragonIndex(
                        originalMountainPalace,
                        originalMountain
                );

        if (dragonIndex < 0) {
            return null;
        }

        // 原运星所在宫
        int originalStarPalace =
                getStarOriginalPalace(originalStar);

        if (originalStarPalace < 1
                || originalStarPalace > 8) {
            return null;
        }

        // 在原运星所在宫，找相同三元龙对应的山
        String replacementMountain =
                getMountainByDragon(
                        originalStarPalace,
                        dragonIndex
                );

        if (replacementMountain == null) {
            return null;
        }

        // 根据这座山查询替星
        int replacementStar =
                getReplacementStar(replacementMountain);

        if (replacementStar < 1) {
            return null;
        }

        // 替星所在宫
        int replacementStarPalace =
                getStarOriginalPalace(replacementStar);

        if (replacementStarPalace < 1
                || replacementStarPalace > 8) {
            return null;
        }

        // 方案A：原运星原宫同元龙阴阳
        boolean methodAYang =
                isYang(
                        originalStarPalace,
                        dragonIndex
                );

        // 方案B：替星原宫同元龙阴阳
        boolean methodBYang =
                isYang(
                        replacementStarPalace,
                        dragonIndex
                );

        ReplacementResult result =
                new ReplacementResult();

        result.originalMountain = originalMountain;

        result.originalStar = originalStar;

        result.replacementMountain =
                replacementMountain;

        result.replacementStar = replacementStar;

        result.originalPalace =
                originalStarPalace;

        result.replacementPalace =
                replacementStarPalace;

        result.dragonIndex = dragonIndex;

        result.originalPalaceYang = methodAYang;
        result.replacementPalaceYang = methodBYang;

        result.methodAForward = methodAYang;
        result.methodBForward = methodBYang;

        result.methodA =
                methodAYang ? "顺飞" : "逆飞";

        result.methodB =
                methodBYang ? "顺飞" : "逆飞";

        result.hasReplacement =
                originalStar != replacementStar;

        StringBuilder detail = new StringBuilder();

        detail.append("原山：")
                .append(originalMountain)
                .append("\n");

        detail.append("原运星：")
                .append(originalStar)
                .append("\n");

        detail.append("原运星所在宫：")
                .append(getPalaceName(originalStarPalace))
                .append("\n");

        detail.append("原山三元龙：")
                .append(getDragonName(dragonIndex))
                .append("\n");

        detail.append("对应替卦山：")
                .append(replacementMountain)
                .append("\n");

        detail.append("替星：")
                .append(replacementStar)
                .append("\n");

        detail.append("方法A：")
                .append(result.methodA)
                .append("\n");

        detail.append("方法B：")
                .append(result.methodB)
                .append("\n");

        detail.append("两种方法是否一致：")
                .append(methodAYang == methodBYang
                        ? "一致" : "不同");

        result.description = detail.toString();

        return result;
    }

    private static String getDragonName(int index) {

        switch (index) {

            case 0:
                return "地元龙";

            case 1:
                return "天元龙";

            case 2:
                return "人元龙";

            default:
                return "未知";
        }
    }

    /**
     * 获取九宫名称。
     */
    private static String getPalaceName(int palace) {

        switch (palace) {

            case 1:
                return "乾宫";

            case 2:
                return "兑宫";

            case 3:
                return "艮宫";

            case 4:
                return "离宫";

            case 5:
                return "坎宫";

            case 6:
                return "坤宫";

            case 7:
                return "震宫";

            case 8:
                return "巽宫";

            default:
                return "未知宫位";
        }
    }

    /**
     * 输出角度分析详情。
     */
    public static String formatAngleResult(
            AngleResult result) {

        if (result == null) {
            return "角度数据无效";
        }

        return String.format(
                Locale.CHINA,
                "测量角度：%.2f°\n"
                        + "所属山向：%s\n"
                        + "山向中心：%.2f°\n"
                        + "偏离中心：%.2f°\n"
                        + "兼向方向：%s\n"
                        + "排盘类型：%s\n"
                        + "说明：%s",
                result.angle,
                result.mountain,
                result.centerAngle,
                result.deviation,
                result.direction,
                result.classification,
                result.description
        );
    }
}
