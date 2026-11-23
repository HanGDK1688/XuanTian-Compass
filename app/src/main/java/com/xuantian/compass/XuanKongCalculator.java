package com.xuantian.compass;

import java.util.Calendar;
import java.util.Locale;

public class XuanKongCalculator {

    // 洛书九宫顺飞顺序
    // 中、乾、兑、艮、离、坎、坤、震、巽
    private static final int[] FLY_ORDER = {
            4, 5, 6, 7, 8, 9, 1, 2, 3
    };

    private static final String[] PALACE_NAMES = {
            "中宫",
            "西北乾宫",
            "西方兑宫",
            "东北艮宫",
            "南方离宫",
            "北方坎宫",
            "西南坤宫",
            "东方震宫",
            "东南巽宫"
    };

    private static final String[] STAR_NAMES = {
            "",
            "一白贪狼",
            "二黑巨门",
            "三碧禄存",
            "四绿文曲",
            "五黄廉贞",
            "六白武曲",
            "七赤破军",
            "八白左辅",
            "九紫右弼"
    };

    private static final String[] ELEMENTS = {
            "",
            "水",
            "土",
            "木",
            "木",
            "土",
            "金",
            "金",
            "土",
            "火"
    };

    private static final String[] DIRECTIONS = {
            "中",
            "西北",
            "西",
            "东北",
            "南",
            "北",
            "西南",
            "东",
            "东南"
    };

    public static class PeriodInfo {

        public int period;
        public int startYear;
        public int endYear;
        public String name;

        public PeriodInfo(
                int period,
                int startYear,
                int endYear,
                String name) {

            this.period = period;
            this.startYear = startYear;
            this.endYear = endYear;
            this.name = name;
        }
    }

    public static class StarInfo {

        public int number;
        public String name;
        public String element;

        public StarInfo(int number) {

            this.number = number;
            this.name = STAR_NAMES[number];
            this.element = ELEMENTS[number];
        }
    }

    public static class FlyingChart {

        public int centerStar;
        public int[] palaceStars;
        public int year;

        public FlyingChart(int centerStar, int year) {

            this.centerStar = centerStar;
            this.year = year;

            this.palaceStars =
                    flyFromCenter(centerStar);
        }
    }

    // 获取三元九运
    public static PeriodInfo getPeriod(int year) {

        int[][] periods = {
                {1864, 1883},
                {1884, 1903},
                {1904, 1923},
                {1924, 1943},
                {1944, 1963},
                {1964, 1983},
                {1984, 2003},
                {2004, 2023},
                {2024, 2043}
        };

        for (int i = 0; i < periods.length; i++) {

            if (year >= periods[i][0]
                    && year <= periods[i][1]) {

                int number = i + 1;

                String name;

                if (number <= 3) {
                    name = "上元第" + number + "运";
                } else if (number <= 6) {
                    name = "中元第" + (number - 3) + "运";
                } else {
                    name = "下元第" + (number - 6) + "运";
                }

                return new PeriodInfo(
                        number,
                        periods[i][0],
                        periods[i][1],
                        name
                );
            }
        }

        return null;
    }

    // 获取当前年份
    public static int getCurrentYear() {

        return Calendar.getInstance()
                .get(Calendar.YEAR);
    }

    // 获取运星入中
    public static int getPeriodCenterStar(int year) {

        PeriodInfo info = getPeriod(year);

        if (info == null) {
            return -1;
        }

        return info.period;
    }

    // 获取流年年星入中
    // 以2024年九运三碧入中为基准
    public static int getAnnualCenterStar(int year) {

        int difference = year - 2024;

        int result = 3 - difference;

        while (result > 9) {
            result -= 9;
        }

        while (result < 1) {
            result += 9;
        }

        return result;
    }

    // 从中宫起星，按洛书顺序飞布
    public static int[] flyFromCenter(int centerStar) {

        int[] result = new int[9];

        if (centerStar < 1 || centerStar > 9) {
            return result;
        }

        for (int i = 0; i < FLY_ORDER.length; i++) {

            int palace = FLY_ORDER[i];

            int star = centerStar + i;

            while (star > 9) {
                star -= 9;
            }

            result[palace - 1] = star;
        }

        return result;
    }

    // 获取运盘
    public static FlyingChart getPeriodChart(int year) {

        int center = getPeriodCenterStar(year);

        if (center < 1) {
            return null;
        }

        return new FlyingChart(center, year);
    }

    // 获取流年盘
    public static FlyingChart getAnnualChart(int year) {

        int center = getAnnualCenterStar(year);

        return new FlyingChart(center, year);
    }

    // 获取九宫星曜信息
    public static StarInfo getStarInfo(int number) {

        if (number < 1 || number > 9) {
            return null;
        }

        return new StarInfo(number);
    }

    // 获取宫位名称
    public static String getPalaceName(int index) {

        if (index < 0 || index >= 9) {
            return "";
        }

        return PALACE_NAMES[index];
    }

    // 获取宫位方向
    public static String getDirection(int index) {

        if (index < 0 || index >= 9) {
            return "";
        }

        return DIRECTIONS[index];
    }

    // 获取星曜名称
    public static String getStarName(int number) {

        if (number < 1 || number > 9) {
            return "";
        }

        return STAR_NAMES[number];
    }

    // 获取星曜五行
    public static String getElement(int number) {

        if (number < 1 || number > 9) {
            return "";
        }

        return ELEMENTS[number];
    }

    // 九宫飞星格式化输出
    public static String formatChart(FlyingChart chart) {

        if (chart == null) {
            return "暂无排盘数据";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("年份：")
                .append(chart.year)
                .append("\n");

        builder.append("中宫：")
                .append(chart.centerStar)
                .append(" ")
                .append(getStarName(chart.centerStar))
                .append("\n\n");

        int[][] displayOrder = {
                {3, 8, 1},
                {2, 4, 6},
                {7, 0, 5}
        };

        for (int[] row : displayOrder) {

            for (int index : row) {

                int star = chart.palaceStars[index];

                builder.append(String.format(
                        Locale.CHINA,
                        "%s：%d",
                        getPalaceName(index),
                        star
                ));

                builder.append("  ");
            }

            builder.append("\n");
        }

        return builder.toString();
    }
}
