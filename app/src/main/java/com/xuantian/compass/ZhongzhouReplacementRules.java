package com.xuantian.compass;

/**
 * 中州派替卦规则
 *
 * 独立于原有二十四山替星表。
 * 不直接修改基础玄空飞星计算器。
 */
public final class ZhongzhouReplacementRules {

    private ZhongzhouReplacementRules() {
    }

    public enum Status {

        INVALID_INPUT,

        DOWN_GUA,

        SAME_PALACE_SAME_YINYANG,

        SAME_PALACE_DIFFERENT_YINYANG,

        OUT_OF_PALACE,

        FIVE_YELLOW_SPECIAL,

        NO_REPLACEMENT_STAR
    }

    public static class Result {

        public Status status;

        public String originalMountain;

        public String adjacentMountain;

        public int originalPalace;

        public int adjacentPalace;

        public int dragonIndex;

        public boolean originalYang;

        public boolean adjacentYang;

        public int replacementStar = -1;

        public boolean needsReplacement;

        public String description;
    }

    /*
     * 九宫山向顺序：
     *
     * 乾：戌、乾、亥
     * 兑：庚、酉、辛
     * 艮：丑、艮、寅
     * 离：丙、午、丁
     * 坎：壬、子、癸
     * 坤：未、坤、申
     * 震：甲、卯、乙
     * 巽：辰、巽、巳
     */
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

    /*
     * true = 阳
     * false = 阴
     *
     * 顺序：
     * 地元龙、天元龙、人元龙
     */
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

    /**
     * 中州派十三山替星表。
     */
    public static int getReplacementStar(String mountain) {

        if (mountain == null) {
            return -1;
        }

        switch (mountain.trim()) {

            case "甲":
            case "申":
                return 1;

            case "壬":
            case "卯":
            case "乙":
                return 2;

            case "丑":
            case "艮":
            case "丙":
                return 7;

            case "巽":
            case "辰":
            case "巳":
                return 6;

            case "庚":
            case "寅":
                return 9;

            default:
                return -1;
        }
    }

    /**
     * 获取山所在宫位。
     */
    public static int getMountainPalace(String mountain) {

        if (mountain == null) {
            return -1;
        }

        String value = mountain.trim();

        for (int palace = 1; palace <= 8; palace++) {

            for (int i = 0; i < 3; i++) {

                if (PALACE_MOUNTAINS[palace][i]
                        .equals(value)) {

                    return palace;
                }
            }
        }

        return -1;
    }

    /**
     * 获取山的三元龙索引。
     *
     * 0：地元龙
     * 1：天元龙
     * 2：人元龙
     */
    public static int getDragonIndex(String mountain) {

        int palace = getMountainPalace(mountain);

        if (palace < 1) {
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
     * 获取指定山的阴阳。
     */
    public static boolean isYang(String mountain) {

        int palace = getMountainPalace(mountain);

        int dragon = getDragonIndex(mountain);

        if (palace < 1 || dragon < 0) {

            throw new IllegalArgumentException(
                    "山向输入错误"
            );
        }

        return PALACE_YIN_YANG[palace][dragon];
    }

    /**
     * 判断实际坐山与兼山的关系。
     *
     * replacementAngle：
     * true  = 已进入兼向判断角度范围
     * false = 下卦角度范围
     *
     * 注意：
     * 进入兼向范围不代表一定需要替卦。
     */
    public static Result analyzePair(
            String originalMountain,
            String adjacentMountain,
            boolean replacementAngle) {

        Result result = new Result();

        result.originalMountain = originalMountain;

        result.adjacentMountain = adjacentMountain;

        if (originalMountain == null
                || adjacentMountain == null) {

            result.status = Status.INVALID_INPUT;

            result.description = "山向数据为空";

            return result;
        }

        int originalPalace =
                getMountainPalace(originalMountain);

        int adjacentPalace =
                getMountainPalace(adjacentMountain);

        if (originalPalace < 1
                || adjacentPalace < 1) {

            result.status = Status.INVALID_INPUT;

            result.description = "无法识别山向";

            return result;
        }

        result.originalPalace = originalPalace;

        result.adjacentPalace = adjacentPalace;

        result.dragonIndex =
                getDragonIndex(originalMountain);

        result.originalYang =
                isYang(originalMountain);

        result.adjacentYang =
                isYang(adjacentMountain);

        if (!replacementAngle) {

            result.status = Status.DOWN_GUA;

            result.description = "处于下卦角度范围";

            return result;
        }

        if (originalPalace != adjacentPalace) {

            result.status = Status.OUT_OF_PALACE;

            result.needsReplacement = true;

            result.description = "不同卦宫，属于出卦兼向";

            return result;
        }

        if (result.originalYang
                == result.adjacentYang) {

            result.status =
                    Status.SAME_PALACE_SAME_YINYANG;

            result.description =
                    "同宫同阴阳，不替";

            return result;
        }

        result.status =
                Status.SAME_PALACE_DIFFERENT_YINYANG;

        result.needsReplacement = true;

        result.description =
                "同宫阴阳不同，进入替卦判断";

        return result;
    }

    /**
     * 判断某山是否属于中州派十三山。
     */
    public static boolean hasReplacementStar(String mountain) {

        return getReplacementStar(mountain) >= 1;
    }
}
