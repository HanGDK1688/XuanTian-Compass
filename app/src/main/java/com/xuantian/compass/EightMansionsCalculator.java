package com.xuantian.compass;

/**
 * 八宅游年法计算器。
 *
 * 当前采用：
 * 1. 住宅坐山所属后天八卦宫位定宅卦。
 * 2. 以宅卦本宫为伏位。
 * 3. 按八宅大游年法排列八方吉凶。
 *
 * 不计算户主出生年份命卦。
 *
 * 方位顺序：
 * 北、东北、东、东南、南、西南、西、西北。
 */
public class EightMansionsCalculator {

    public static class Result {

        public String sittingMountain;
        public String houseName;
        public String houseTrigram;
        public String houseGroup;

        public String[] directions;
        public String[] fortunes;
        public String[] starNames;
        public boolean[] auspicious;

        public Result() {
            directions = new String[8];
            fortunes = new String[8];
            starNames = new String[8];
            auspicious = new boolean[8];
        }
    }

    private static final String[] DIRECTIONS = {
            "北方（坎）",
            "东北（艮）",
            "东方（震）",
            "东南（巽）",
            "南方（离）",
            "西南（坤）",
            "西方（兑）",
            "西北（乾）"
    };

    /*
     * 星名编号：
     * 0 生气
     * 1 天医
     * 2 延年
     * 3 伏位
     * 4 绝命
     * 5 五鬼
     * 6 六煞
     * 7 祸害
     */
    private static final String[] STAR_NAMES = {
            "生气",
            "天医",
            "延年",
            "伏位",
            "绝命",
            "五鬼",
            "六煞",
            "祸害"
    };

    private static final String[] STAR_DETAILS = {
            "贪狼木 · 上吉",
            "巨门土 · 次吉",
            "武曲金 · 中吉",
            "辅弼木 · 小吉",
            "破军金 · 大凶",
            "廉贞火 · 中凶",
            "文曲水 · 次凶",
            "禄存土 · 小凶"
    };

    private static final String[] HOUSE_NAMES = {
            "坎宅",
            "艮宅",
            "震宅",
            "巽宅",
            "离宅",
            "坤宅",
            "兑宅",
            "乾宅"
    };

    private static final String[] HOUSE_TRIGRAMS = {
            "坎",
            "艮",
            "震",
            "巽",
            "离",
            "坤",
            "兑",
            "乾"
    };

    /*
     * 方位顺序：
     * 北、东北、东、东南、南、西南、西、西北。
     *
     * 表格来源口径：
     * 坐山起伏位的大游年法。
     *
     * 每个数字对应 STAR_NAMES 的编号。
     */
    private static final int[][] HOUSE_TABLE = {

            // 坎宅
            {3, 5, 1, 0, 2, 4, 7, 6},

            // 艮宅
            {5, 3, 6, 4, 7, 0, 2, 1},

            // 震宅
            {1, 6, 3, 2, 0, 7, 4, 5},

            // 巽宅
            {0, 4, 2, 3, 1, 5, 6, 7},

            // 离宅
            {2, 7, 0, 1, 3, 6, 5, 4},

            // 坤宅
            {4, 0, 7, 5, 6, 3, 1, 2},

            // 兑宅
            {7, 2, 4, 6, 5, 1, 3, 0},

            // 乾宅
            {6, 1, 5, 7, 4, 2, 0, 3}
    };

    /*
     * 二十四山顺序与 XuanKongCalculator 保持一致。
     *
     * 子癸壬为坎宅
     * 丑艮寅为艮宅
     * 甲卯乙为震宅
     * 辰巽巳为巽宅
     * 丙午丁为离宅
     * 未坤申为坤宅
     * 庚酉辛为兑宅
     * 戌乾亥为乾宅
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
     * 二十四山对应宅卦编号：
     *
     * 0 坎
     * 1 艮
     * 2 震
     * 3 巽
     * 4 离
     * 5 坤
     * 6 兑
     * 7 乾
     */
    private static final int[] MOUNTAIN_HOUSE = {
            0, 0, 1,
            1, 1, 2,
            2, 2, 3,
            3, 3, 4,
            4, 4, 5,
            5, 5, 6,
            6, 6, 7,
            7, 7, 0
    };

    /**
     * 根据坐山计算八宅。
     */
    public static Result calculate(String sittingMountain) {

        if (sittingMountain == null) {
            return null;
        }

        String mountain = sittingMountain.trim();

        int mountainIndex = getMountainIndex(mountain);

        if (mountainIndex < 0) {
            return null;
        }

        int houseIndex = MOUNTAIN_HOUSE[mountainIndex];

        Result result = new Result();

        result.sittingMountain = mountain;
        result.houseName = HOUSE_NAMES[houseIndex];
        result.houseTrigram = HOUSE_TRIGRAMS[houseIndex];

        result.houseGroup =
                houseIndex == 0
                || houseIndex == 2
                || houseIndex == 3
                || houseIndex == 4
                ? "东四宅"
                : "西四宅";

        for (int i = 0; i < 8; i++) {

            int starIndex = HOUSE_TABLE[houseIndex][i];

            result.directions[i] = DIRECTIONS[i];

            result.fortunes[i] = STAR_NAMES[starIndex];

            result.starNames[i] = STAR_DETAILS[starIndex];

            result.auspicious[i] =
                    starIndex == 0
                    || starIndex == 1
                    || starIndex == 2
                    || starIndex == 3;
        }

        return result;
    }

    private static int getMountainIndex(String mountain) {

        for (int i = 0; i < MOUNTAINS.length; i++) {

            if (MOUNTAINS[i].equals(mountain)) {
                return i;
            }
        }

        return -1;
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

    /**
     * 输出完整八宅分析文字。
     */
    public static String format(Result result) {

        if (result == null) {
            return "坐山资料无效，无法进行八宅分析。";
        }

        StringBuilder text = new StringBuilder();

        text.append("住宅坐山：")
                .append(result.sittingMountain)
                .append("\n");

        text.append("宅卦：")
                .append(result.houseName)
                .append("（")
                .append(result.houseTrigram)
                .append("卦）\n");

        text.append("宅型：")
                .append(result.houseGroup)
                .append("\n");

        text.append("起伏位：坐山本宫为伏位\n\n");

        text.append("八方吉凶：\n");

        for (int i = 0; i < 8; i++) {

            text.append(result.directions[i])
                    .append("：")
                    .append(result.fortunes[i])
                    .append(" · ")
                    .append(result.starNames[i])
                    .append(result.auspicious[i]
                            ? "（吉方）"
                            : "（凶方）")
                    .append("\n");
        }

        text.append("\n四吉方：生气、天医、延年、伏位");

        text.append("\n四凶方：绝命、五鬼、六煞、祸害");

        text.append("\n\n说明：本结果按住宅坐山起伏位，")
                .append("不涉及户主命卦，也不代表实际房间功能判断。");

        return text.toString();
    }
}
