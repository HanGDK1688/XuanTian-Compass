package com.xuantian.compass;

/**
 * 中州派替卦规则
 *
 * 正式方法：A
 * 按原运星所在宫位的同元龙阴阳决定顺逆。
 *
 * 比较方法：B
 * 按替星原始宫位的同元龙阴阳决定顺逆。
 *
 * 阳顺飞，阴逆飞。
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

        public int originalStar = -1;

        public int originalPalace = -1;
        public int adjacentPalace = -1;

        public int dragonIndex = -1;

        public boolean originalYang;
        public boolean adjacentYang;

        public String replacementMountain;

        public int replacementPalace = -1;
        public int replacementStar = -1;

        public boolean needsReplacement;
        public boolean hasReplacement;

        public boolean methodAForward;
        public boolean methodBForward;

        public String methodA;
        public String methodB;

        public String description;
    }

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
     * 0：地元龙
     * 1：天元龙
     * 2：人元龙
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

                if (PALACE_MOUNTAINS[palace][i].equals(value)) {
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

        if (mountain == null) {
            return -1;
        }

        int palace = getMountainPalace(mountain);

        if (palace < 1) {
            return -1;
        }

        String value = mountain.trim();

        for (int i = 0; i < 3; i++) {

            if (PALACE_MOUNTAINS[palace][i].equals(value)) {
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
            throw new IllegalArgumentException("山向输入错误");
        }

        return PALACE_YIN_YANG[palace][dragon];
    }

    /**
     * 获取九星原始宫位。
     */
    private static int getStarOriginalPalace(int star) {

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
     * 在指定宫位中寻找相同元龙的山。
     */
    private static String getMountainByDragon(
            int palace,
            int dragonIndex) {

        if (palace < 1 || palace > 8
                || dragonIndex < 0
                || dragonIndex > 2) {

            return null;
        }

        return PALACE_MOUNTAINS[palace][dragonIndex];
    }

    private static String getDirectionText(boolean forward) {

        return forward ? "阳顺飞" : "阴逆飞";
    }

    /**
     * 判断实际坐山与兼山的关系。
     */
    public static Result analyzePair(
            String originalMountain,
            String adjacentMountain,
            boolean replacementAngle) {

        Result result = new Result();

        if (originalMountain == null
                || adjacentMountain == null) {

            result.status = Status.INVALID_INPUT;
            result.description = "山向数据为空";

            return result;
        }

        String original = originalMountain.trim();
        String adjacent = adjacentMountain.trim();

        result.originalMountain = original;
        result.adjacentMountain = adjacent;

        int originalPalace =
                getMountainPalace(original);

        int adjacentPalace =
                getMountainPalace(adjacent);

        if (originalPalace < 1 || adjacentPalace < 1) {

            result.status = Status.INVALID_INPUT;
            result.description = "无法识别山向";

            return result;
        }

        result.originalPalace = originalPalace;
        result.adjacentPalace = adjacentPalace;

        result.dragonIndex =
                getDragonIndex(original);

        if (result.dragonIndex < 0) {

            result.status = Status.INVALID_INPUT;
            result.description = "无法识别原山元龙";

            return result;
        }

        result.originalYang = isYang(original);
        result.adjacentYang = isYang(adjacent);

        /*
         * 下卦不进入替卦计算。
         */
        if (!replacementAngle) {

            result.status = Status.DOWN_GUA;
            result.description = "处于下卦角度范围";

            return result;
        }

        /*
         * 出卦：
         *
         * 保留 OUT_OF_PALACE 状态。
         * 但不在这里终止替星计算。
         *
         * 这是本次修复的关键。
         */
        if (originalPalace != adjacentPalace) {

            result.status = Status.OUT_OF_PALACE;

            result.needsReplacement = true;
            result.hasReplacement = false;

            result.description =
                    "不同卦宫，属于出卦兼向；保留出卦状态并继续检查替星";

            return result;
        }

        /*
         * 同宫同阴阳，不替。
         */
        if (result.originalYang == result.adjacentYang) {

            result.status =
                    Status.SAME_PALACE_SAME_YINYANG;

            result.needsReplacement = false;
            result.hasReplacement = false;

            result.description = "同宫同阴阳，不替";

            return result;
        }

        /*
         * 同宫异阴阳，进入替卦判断。
         */
        result.status =
                Status.SAME_PALACE_DIFFERENT_YINYANG;

        result.needsReplacement = true;

        result.description =
                "同宫阴阳不同，进入替卦判断";

        return result;
    }

    /**
     * 中州派完整替卦计算。
     */
    public static Result calculateReplacement(
            int originalStar,
            String originalMountain,
            String adjacentMountain,
            boolean replacementAngle) {

        Result result = analyzePair(
                originalMountain,
                adjacentMountain,
                replacementAngle
        );

        /*
         * 原运星必须为一白至九紫。
         */
        if (originalStar < 1 || originalStar > 9) {

            result.status = Status.INVALID_INPUT;
            result.hasReplacement = false;
            result.replacementStar = -1;

            result.description =
                    "原运星必须在一白至九紫之间";

            return result;
        }

        result.originalStar = originalStar;

        /*
         * 以下情况不能继续普通替星计算。
         *
         * 注意：
         * OUT_OF_PALACE 不在这里。
         */
        if (result.status == Status.INVALID_INPUT
                || result.status == Status.DOWN_GUA
                || result.status == Status.SAME_PALACE_SAME_YINYANG) {

            result.hasReplacement = false;
            result.replacementStar = -1;

            return result;
        }

        /*
         * 五黄特殊处理。
         *
         * 五黄不生成普通替星。
         * 顺逆依据原山元龙阴阳。
         *
         * 即使属于出卦，也必须执行五黄特殊处理。
         */
        if (originalStar == 5) {

            result.status = Status.FIVE_YELLOW_SPECIAL;

            result.hasReplacement = false;
            result.replacementStar = -1;
            result.replacementMountain = null;
            result.replacementPalace = -1;

            result.methodAForward = result.originalYang;
            result.methodBForward = result.originalYang;

            result.methodA =
                    getDirectionText(result.methodAForward);

            result.methodB =
                    getDirectionText(result.methodBForward);

            result.description =
                    "五黄入中，无普通替星；按原山元龙阴阳定顺逆";

            return result;
        }

        /*
         * 找出原运星所在宫位。
         */
        int starPalace =
                getStarOriginalPalace(originalStar);

        if (starPalace < 1 || starPalace > 8) {

            result.status = Status.INVALID_INPUT;
            result.hasReplacement = false;

            result.description =
                    "无法确定原运星宫位";

            return result;
        }

        /*
         * 在原运星宫位中，
         * 寻找与原山相同元龙的山。
         */
        String replacementMountain =
                getMountainByDragon(
                        starPalace,
                        result.dragonIndex
                );

        if (replacementMountain == null) {

            result.status = Status.INVALID_INPUT;
            result.hasReplacement = false;

            result.description =
                    "无法找到对应元龙替卦山";

            return result;
        }

        result.replacementMountain =
                replacementMountain;

        result.replacementPalace =
                starPalace;

        /*
         * 根据中州派十三山表取替星。
         */
        int replacementStar =
                getReplacementStar(replacementMountain);

        result.replacementStar = replacementStar;

        if (replacementStar < 1 || replacementStar > 9) {

            result.status = Status.NO_REPLACEMENT_STAR;

            result.hasReplacement = false;

            result.description =
                    "对应替卦山不在中州派十三山替星表内；不生成普通替卦盘";

            return result;
        }

        result.hasReplacement = true;

        /*
         * 方法 A：
         * 依据原运星宫位中相同元龙的阴阳决定顺逆。
         */
        result.methodAForward =
                PALACE_YIN_YANG[starPalace]
                        [result.dragonIndex];

        /*
         * 方法 B：
         * 依据替星原始宫位中相同元龙的阴阳决定顺逆。
         */
        int replacementStarPalace =
                getStarOriginalPalace(replacementStar);

        if (replacementStarPalace < 1
                || replacementStarPalace > 8) {

            result.status = Status.INVALID_INPUT;
            result.hasReplacement = false;
            result.replacementStar = -1;

            result.description =
                    "无法确定替星原始宫位";

            return result;
        }

        result.methodBForward =
                PALACE_YIN_YANG[replacementStarPalace]
                        [result.dragonIndex];

        result.methodA =
                getDirectionText(result.methodAForward);

        result.methodB =
                getDirectionText(result.methodBForward);

        /*
         * 关键修复：
         *
         * 如果原来是出卦，保留 OUT_OF_PALACE。
         * 不允许后面的普通替星计算覆盖出卦状态。
         */
        if (result.status != Status.OUT_OF_PALACE) {

            result.status =
                    Status.SAME_PALACE_DIFFERENT_YINYANG;
        }

        result.description =
                result.status == Status.OUT_OF_PALACE
                        ? "出卦状态保留；替星信息已计算，仅供规则分析"
                        : "中州派替卦计算完成；方法A为正式结果，方法B仅供比较";

        return result;
    }

    /**
     * 判断某山是否属于中州派十三山。
     */
    public static boolean hasReplacementStar(String mountain) {

        return getReplacementStar(mountain) >= 1;
    }
}
