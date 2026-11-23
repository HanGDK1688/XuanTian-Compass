package com.xuantian.compass;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

public class XuanKongCalculator {

    private static final int[] FLY_ORDER = {
            0, 1, 2, 3, 4, 5, 6, 7, 8
    };

    private static final String[] PALACE_NAMES = {
            "中宫", "西北乾宫", "西方兑宫",
            "东北艮宫", "南方离宫", "北方坎宫",
            "西南坤宫", "东方震宫", "东南巽宫"
    };

    private static final String[] STAR_NAMES = {
            "",
            "一白贪狼", "二黑巨门", "三碧禄存",
            "四绿文曲", "五黄廉贞", "六白武曲",
            "七赤破军", "八白左辅", "九紫右弼"
    };

    private static final String[] ELEMENTS = {
            "",
            "水", "土", "木", "木", "土",
            "金", "金", "土", "火"
    };

    private static final String[] DIRECTIONS = {
            "中", "西北", "西", "东北",
            "南", "北", "西南", "东", "东南"
    };

    private static final TimeZone BEIJING_TIME_ZONE =
            TimeZone.getTimeZone("GMT+08:00");

    public static class PeriodInfo {
        public int period;
        public int startYear;
        public int endYear;
        public String name;

        public PeriodInfo(int period, int startYear,
                          int endYear, String name) {
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
            this.palaceStars = flyFromCenter(centerStar);
        }
    }

    /**
     * 初始化离线节气数据。
     * 应在 Activity 中调用一次。
     */
    public static void initialize(Context context) {
        SolarTermCalculator.load(
                context.getApplicationContext());
    }

    /**
     * 查询年份所属三元九运。
     * 这里传入的是已经确定的节气年份。
     */
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

                int n = i + 1;

                String name;

                if (n <= 3) {
                   name = "上元第" + n + "运";
                } else if (n <= 6) {
                   name = "中元第" + n + "运";
                } else {
                   name = "下元第" + n + "运";
                }

                return new PeriodInfo(
                        n,
                        periods[i][0],
                        periods[i][1],
                        name
                );
            }
        }

        return null;
    }

    /**
     * 根据一个北京时间时间戳查询当前九运。
     */
    public static PeriodInfo getPeriodAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        return getPeriod(effectiveYear);
    }

    /**
     * 获取当前北京时间对应的节气年份。
     */
    public static int getCurrentEffectiveYear() {

        return SolarTermCalculator
                .getCurrentEffectiveYear();
    }

    /**
     * 获取当前北京时间对应的九运。
     */
    public static PeriodInfo getCurrentPeriod() {

        return getPeriodAt(
                System.currentTimeMillis());
    }

    /**
     * 获取北京时间当前公历年份。
     */
    public static int getCurrentYear() {

        Calendar calendar = Calendar.getInstance(
                BEIJING_TIME_ZONE);

        return calendar.get(Calendar.YEAR);
    }

    /**
     * 运盘入中星。
     */
    public static int getPeriodCenterStar(int year) {

        PeriodInfo p = getPeriod(year);

        return p == null ? -1 : p.period;
    }

    /**
     * 根据指定北京时间时刻计算运盘入中星。
     */
    public static int getPeriodCenterStarAt(
            long beijingTimestamp) {

        PeriodInfo p = getPeriodAt(beijingTimestamp);

        return p == null ? -1 : p.period;
    }

    /**
     * 流年年盘入中星。
     * 2024年为三碧入中，之后每年逆行一位。
     */
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

    /**
     * 根据北京时间时刻计算流年入中星。
     * 立春前使用上一节气年份。
     */
    public static int getAnnualCenterStarAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        return getAnnualCenterStar(effectiveYear);
    }

    /**
     * 获取当前流年入中星。
     */
    public static int getCurrentAnnualCenterStar() {

        return getAnnualCenterStarAt(
                System.currentTimeMillis());
    }

    /**
     * 九宫顺飞。
     */
    public static int[] flyFromCenter(int centerStar) {

        int[] result = new int[9];

        if (centerStar < 1 || centerStar > 9) {
            return result;
        }

        for (int i = 0; i < FLY_ORDER.length; i++) {

            int palaceIndex = FLY_ORDER[i];

            int star = centerStar + i;

            while (star > 9) {
                star -= 9;
            }

            result[palaceIndex] = star;
        }

        return result;
    }

    /**
     * 获取运盘。
     */
    public static FlyingChart getPeriodChart(int year) {

        int center = getPeriodCenterStar(year);

        if (center < 1) {
            return null;
        }

        return new FlyingChart(center, year);
    }

    /**
     * 根据指定北京时间时刻获取运盘。
     */
    public static FlyingChart getPeriodChartAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        int center = getPeriodCenterStar(effectiveYear);

        if (center < 1) {
            return null;
        }

        return new FlyingChart(center, effectiveYear);
    }

    /**
     * 获取流年盘。
     */
    public static FlyingChart getAnnualChart(int year) {

        return new FlyingChart(
                getAnnualCenterStar(year),
                year
        );
    }

    /**
     * 根据指定北京时间时刻获取流年盘。
     */
    public static FlyingChart getAnnualChartAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        return new FlyingChart(
                getAnnualCenterStar(effectiveYear),
                effectiveYear
        );
    }

    public static StarInfo getStarInfo(int n) {

        if (n < 1 || n > 9) {
            return null;
        }

        return new StarInfo(n);
    }

    public static String getPalaceName(int index) {

        if (index < 0 || index >= 9) {
            return "";
        }

        return PALACE_NAMES[index];
    }

    public static String getDirection(int index) {

        if (index < 0 || index >= 9) {
            return "";
        }

        return DIRECTIONS[index];
    }

    public static String getStarName(int n) {

        if (n < 1 || n > 9) {
            return "";
        }

        return STAR_NAMES[n];
    }

    public static String getElement(int n) {

        if (n < 1 || n > 9) {
            return "";
        }

        return ELEMENTS[n];
    }

    /**
     * 传统九宫格显示。
     */
    public static String formatChart(FlyingChart chart) {

        if (chart == null) {
            return "暂无排盘数据";
        }

        StringBuilder b = new StringBuilder();

        b.append("节气年份：")
                .append(chart.year)
                .append("\n");

        b.append("中宫入中星：")
                .append(chart.centerStar)
                .append(" ")
                .append(getStarName(chart.centerStar))
                .append("\n\n");

        int[][] order = {
                {1, 5, 3},
                {2, 0, 7},
                {6, 4, 8}
        };

        for (int[] row : order) {

            for (int index : row) {

                int star = chart.palaceStars[index];

                b.append(getPalaceName(index))
                        .append("：")
                        .append(star)
                        .append(" ")
                        .append(getStarName(star))
                        .append("    ");
            }

            b.append("\n");
        }

        return b.toString();
    }
}
