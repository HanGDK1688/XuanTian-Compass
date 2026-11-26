package com.xuantian.compass;

/**
 * 八宅游年法基础计算器。
 *
 * 本项目当前采用：
 * 住宅坐山所属八卦宫位确定宅卦，
 * 再按八宅游年法判断八个方位。
 *
 * 不使用户主出生年份命卦。
 */
public class EightMansionsCalculator {

    public static class Result {
        public String sittingMountain;
        public String houseName;
        public String houseTrigram;
        public String[] directions;
        public String[] fortunes;
        public boolean[] auspicious;

        public Result() {
            directions = new String[8];
            fortunes = new String[8];
            auspicious = new boolean[8];
        }
    }

    private static final String[] DIRECTIONS = {
            "北方", "东北", "东方", "东南",
            "南方", "西南", "西方", "西北"
    };

    /*
     * 八宅方位排列：
     * 北、东北、东、东南、南、西南、西、西北。
     *
     * 每一行依次对应：
     * 生气、天医、延年、伏位、
     * 绝命、五鬼、六煞、祸害。
     */
    private static final String[] STAR_NAMES = {
            "生气", "天医", "延年", "伏位",
            "绝命", "五鬼", "六煞", "祸害"
    };

    /*
     * 每宅的八方吉凶。
     * 方位索引：
     * 0北坎、1东北艮、2东震、3东南巽、
     * 4南离、5西南坤、6西兑、7西北乾。
     *
     * 星名索引：
     * 0生气、1天医、2延年、3伏位、
     * 4绝命、5五鬼、6六煞、7祸害。
     */
    private static final int[][] HOUSE_TABLE = {

            // 坎宅
            {3, 5, 1, 0, 2, 4, 7, 6},

            // 艮宅
            {5, 3, 6, 4, 7, 0, 1, 2},

            // 震宅
            {1, 7, 3, 2, 0, 6, 4, 5},

            // 巽宅
            {0, 4, 2, 3, 1, 5, 7, 6},

            // 离宅
            {2, 6, 0, 1, 3, 7, 5, 4},

            // 坤宅
            {4, 2, 7, 5, 6, 3, 1, 0},

            // 兑宅
            {6, 1, 4, 7, 5, 2, 0, 3},

            // 乾宅
            {7, 0, 5, 6, 4, 1, 3, 2}
    };

    private static final String[] HOUSE_NAMES = {
            "坎宅", "艮宅", "震宅", "巽宅",
            "离宅", "坤宅", "兑宅", "乾宅"
    };

    private static final String[] HOUSE_TRIGRAMS = {
            "坎", "艮", "震", "巽",
            "离", "坤", "兑", "乾"
    };

    /*
     * 二十四山对应宅卦。
     * 顺序与 XuanKongCalculator 的二十四山一致。
     */
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

    /*
     * 八卦宫位对应的宅卦索引：
     * 0坎、1艮、2震、3巽、
     * 4离、5坤、6兑、7乾。
     */
    private static final int[] MOUNTAIN_HOUSE = {
            0, 0, 5,
            1, 1, 2,
            2, 2, 3,
            3, 3, 4,
            4, 4, 5,
            5, 5, 6,
            6, 6, 7,
            7, 7, 0
    };

    /**
     * 根据住宅坐山计算八宅结果。
     *
     * @param sittingMountain 二十四山之一
     * @return 八宅结果；坐山无效时返回 null
     */
    public static Result calculate(String sittingMountain) {

        if (sittingMountain == null) {
            return null;
        }

        String mountain = sittingMountain.trim();

        int mountainIndex = -1;

        for (int i = 0; i < MOUNTAINS.length; i++) {
            if (MOUNTAINS[i].equals(mountain)) {
                mountainIndex = i;
                break;
            }
        }

        if (mountainIndex < 0) {
            return null;
        }

        int houseIndex = MOUNTAIN_HOUSE[mountainIndex];

        Result result = new Result();

        result.sittingMountain = mountain;
        result.houseName = HOUSE_NAMES[houseIndex];
        result.houseTrigram = HOUSE_TRIGRAMS[houseIndex];

        for (int i = 0; i < 8; i++) {

            int starIndex = HOUSE_TABLE[houseIndex][i];

            result.directions[i] = DIRECTIONS[i];
            result.fortunes[i] = STAR_NAMES[starIndex];

            result.auspicious[i] = starIndex <= 3;
        }

        return result;
    }

    public static String getFortuneName(int index) {

        if (index < 0 || index >= STAR_NAMES.length) {
            return "";
        }

        return STAR_NAMES[index];
    }

    public static String getDirectionName(int index) {

        if (index < 0 || index >= DIRECTIONS.length) {
            return "";
        }

        return DIRECTIONS[index];
    }

    public static String format(Result result) {

        if (result == null) {
            return "坐山资料无效，无法进行八宅分析。";
        }

        StringBuilder text = new StringBuilder();

        text.append("八宅分析\n\n");
        text.append("坐山：")
                .append(result.sittingMountain)
                .append("\n");

        text.append("宅卦：")
                .append(result.houseName)
                .append("（")
                .append(result.houseTrigram)
                .append("卦）\n\n");

        text.append("八方吉凶：\n");

        for (int i = 0; i < 8; i++) {

            text.append(result.directions[i])
                    .append("：")
                    .append(result.fortunes[i])
                    .append(result.auspicious[i]
                            ? "（吉方）"
                            : "（凶方）")
                    .append("\n");
        }

        text.append("\n说明：本结果按住宅坐山所属宅卦计算，")
                .append("不涉及户主命卦。");

        return text.toString();
    }
}
