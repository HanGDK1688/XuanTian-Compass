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
     * 每个宫位的三元龙顺序：
     * 地元龙、天元龙、人元龙。
     *
     * 宫位索引：
     * 0中宫，1乾，2兑，3艮，4离，
     * 5坎，6坤，7震，8巽。
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
     * 三元龙阴阳：
     * 地元龙、天元龙、人元龙。
     *
     * true = 阳
     * false = 阴
     *
     * 采用当前项目选定的三元龙阴阳表。
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

    public static class ResidenceChart {
        public int year;
        public int period;

        public String sittingMountain;
        public String facingMountain;

        public int sittingPalace;
        public int facingPalace;

        public int mountainCenterStar;
        public int facingCenterStar;

        public boolean mountainForward;
        public boolean facingForward;

        public int[] periodStars;
        public int[] mountainStars;
        public int[] facingStars;

        public String mountainDirection;
        public String facingDirection;

        public ResidenceChart() {
        }
    }

    public static void initialize(Context context) {
        SolarTermCalculator.load(
                context.getApplicationContext());
    }

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

    public static PeriodInfo getPeriodAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        return getPeriod(effectiveYear);
    }

    public static int getCurrentEffectiveYear() {
        return SolarTermCalculator
                .getCurrentEffectiveYear();
    }

    public static PeriodInfo getCurrentPeriod() {
        return getPeriodAt(
                System.currentTimeMillis());
    }

    public static int getCurrentYear() {

        Calendar calendar = Calendar.getInstance(
                BEIJING_TIME_ZONE);

        return calendar.get(Calendar.YEAR);
    }

    public static int getPeriodCenterStar(int year) {

        PeriodInfo p = getPeriod(year);

        return p == null ? -1 : p.period;
    }

    public static int getPeriodCenterStarAt(
            long beijingTimestamp) {

        PeriodInfo p = getPeriodAt(beijingTimestamp);

        return p == null ? -1 : p.period;
    }

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

    public static int getAnnualCenterStarAt(
            long beijingTimestamp) {

        int effectiveYear =
                SolarTermCalculator.getEffectiveYear(
                        beijingTimestamp);

        return getAnnualCenterStar(effectiveYear);
    }

    public static int getCurrentAnnualCenterStar() {
        return getAnnualCenterStarAt(
                System.currentTimeMillis());
    }

    public static int[] flyFromCenter(int centerStar) {
        return flyFromCenter(centerStar, true);
    }

    public static int[] flyFromCenter(
            int centerStar, boolean forward) {

        int[] result = new int[9];

        if (centerStar < 1 || centerStar > 9) {
            return result;
        }

        for (int i = 0; i < 9; i++) {

            int orderIndex;

            if (forward) {
                orderIndex = i;
            } else {
                orderIndex = (9 - i) % 9;
            }

            int palaceIndex = FLY_ORDER[i];

            int star = centerStar + orderIndex;

            while (star > 9) {
                star -= 9;
            }

            result[palaceIndex] = star;
        }

        return result;
    }

    public static FlyingChart getPeriodChart(int year) {

        int center = getPeriodCenterStar(year);

        if (center < 1) {
            return null;
        }

        return new FlyingChart(center, year);
    }

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

    public static FlyingChart getAnnualChart(int year) {

        return new FlyingChart(
                getAnnualCenterStar(year),
                year
        );
    }

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

    /**
     * 获取住宅三盘。
     *
     * year：已经按入住日期及立春确定的节气年份。
     * sittingMountain：坐山，例如“卯”。
     *
     * 朝向自动取坐山相反的二十四山。
     */
    public static ResidenceChart getResidenceChart(
            int year, String sittingMountain) {

        int sittingIndex = getMountainIndex(
                sittingMountain);

        if (sittingIndex < 0) {
            return null;
        }

        int facingIndex = (sittingIndex + 12) % 24;

        String facingMountain =
                MOUNTAINS[facingIndex];

        PeriodInfo periodInfo = getPeriod(year);

        if (periodInfo == null) {
            return null;
        }

        int sittingPalace =
                getMountainPalace(sittingMountain);

        int facingPalace =
                getMountainPalace(facingMountain);

        if (sittingPalace < 1 || facingPalace < 1) {
            return null;
        }

        FlyingChart periodChart =
                getPeriodChart(year);

        if (periodChart == null) {
            return null;
        }

        int mountainCenter =
                periodChart.palaceStars[sittingPalace];

        int facingCenter =
                periodChart.palaceStars[facingPalace];

        boolean mountainForward =
                getStarFlyingDirection(
                        mountainCenter,
                        sittingMountain);

        boolean facingForward =
                getStarFlyingDirection(
                        facingCenter,
                        facingMountain);

        ResidenceChart chart = new ResidenceChart();

        chart.year = year;
        chart.period = periodInfo.period;

        chart.sittingMountain = sittingMountain;
        chart.facingMountain = facingMountain;

        chart.sittingPalace = sittingPalace;
        chart.facingPalace = facingPalace;

        chart.mountainCenterStar = mountainCenter;
        chart.facingCenterStar = facingCenter;

        chart.mountainForward = mountainForward;
        chart.facingForward = facingForward;

        chart.periodStars =
                periodChart.palaceStars.clone();

        chart.mountainStars =
                flyFromCenter(
                        mountainCenter,
                        mountainForward);

        chart.facingStars =
                flyFromCenter(
                        facingCenter,
                        facingForward);

        chart.mountainDirection =
                mountainForward ? "顺飞" : "逆飞";

        chart.facingDirection =
                facingForward ? "顺飞" : "逆飞";

        return chart;
    }

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

    public static int getMountainPalace(String mountain) {

        if (mountain == null) {
            return -1;
        }

        for (int palace = 1; palace < 9; palace++) {

            for (int i = 0; i < 3; i++) {

                if (PALACE_MOUNTAINS[palace][i]
                        .equals(mountain.trim())) {
                    return palace;
                }
            }
        }

        return -1;
    }

    private static int getStarOriginalPalace(int star) {

        switch (star) {
            case 1: return 5;
            case 2: return 6;
            case 3: return 7;
            case 4: return 8;
            case 5: return 0;
            case 6: return 1;
            case 7: return 2;
            case 8: return 3;
            case 9: return 4;
            default: return -1;
        }
    }

    private static boolean getStarFlyingDirection(
            int star, String originalMountain) {

        int originalIndex =
                getMountainIndex(originalMountain);

        if (originalIndex < 0) {
            return true;
        }

        int originalPalace =
                getMountainPalace(originalMountain);

        if (originalPalace < 1) {
            return true;
        }

        int originalDragon =
                getDragonIndex(
                        originalPalace,
                        originalMountain);

        if (originalDragon < 0) {
            return true;
        }

        if (star == 5) {

            return PALACE_YIN_YANG[originalPalace]
                    [originalDragon];
        }

        int starPalace =
                getStarOriginalPalace(star);

        if (starPalace < 1) {
            return true;
        }

        return PALACE_YIN_YANG[starPalace]
                [originalDragon];
    }

    private static int getDragonIndex(
            int palace, String mountain) {

        if (palace < 1 || palace > 8) {
            return -1;
        }

        for (int i = 0; i < 3; i++) {

            if (PALACE_MOUNTAINS[palace][i]
                    .equals(mountain)) {
                return i;
            }
        }

        return -1;
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

    public static String formatResidenceChart(
            ResidenceChart chart) {

        if (chart == null) {
            return "住宅排盘数据不完整";
        }

        StringBuilder b = new StringBuilder();

        b.append("住宅节气年份：")
                .append(chart.year)
                .append("\n");

        b.append("所属元运：")
                .append(chart.period)
                .append("运\n");

        b.append("坐山：")
                .append(chart.sittingMountain)
                .append("\n");

        b.append("朝向：")
                .append(chart.facingMountain)
                .append("\n\n");

        b.append("运盘、山盘、向盘\n");

        int[][] order = {
                {1, 5, 3},
                {2, 0, 7},
                {6, 4, 8}
        };

        for (int[] row : order) {

            for (int index : row) {

                b.append(getPalaceName(index))
                        .append("\n");

                b.append("运：")
                        .append(chart.periodStars[index])
                        .append("  ");

                b.append("山：")
                        .append(chart.mountainStars[index])
                        .append("  ");

                b.append("向：")
                        .append(chart.facingStars[index])
                        .append("\n\n");
            }
        }

        b.append("山星：")
                .append(chart.mountainCenterStar)
                .append("入中，")
                .append(chart.mountainDirection)
                .append("\n");

        b.append("向星：")
                .append(chart.facingCenterStar)
                .append("入中，")
                .append(chart.facingDirection);

        return b.toString();
    }
}
