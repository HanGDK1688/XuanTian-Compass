package com.xuantian.compass;

import org.junit.Test;

import static org.junit.Assert.*;

public class XuanKongCalculatorTest {

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

    private static final int[] PERIOD_YEARS = {
            1870, 1890, 1910,
            1930, 1950, 1970,
            1990, 2010, 2030
    };

    /**
     * 检查九宫星盘是否包含1至9，
     * 并且每颗星只出现一次。
     */
    private void assertValidNineStars(int[] stars) {

        assertNotNull("星盘不能为null", stars);
        assertEquals("九宫必须有9个位置", 9, stars.length);

        boolean[] found = new boolean[10];

        for (int star : stars) {

            assertTrue("星数必须在1至9之间",
                    star >= 1 && star <= 9);

            assertFalse("九宫不能出现重复星数",
                    found[star]);

            found[star] = true;
        }

        for (int i = 1; i <= 9; i++) {
            assertTrue("缺少星数：" + i, found[i]);
        }
    }

    /**
     * 测试九个元运。
     */
    @Test
    public void testNinePeriods() {

        for (int i = 0; i < 9; i++) {

            XuanKongCalculator.PeriodInfo info =
                    XuanKongCalculator.getPeriod(
                            PERIOD_YEARS[i]);

            assertNotNull("元运不能为空", info);

            assertEquals(
                    "元运编号错误",
                    i + 1,
                    info.period
            );

            XuanKongCalculator.FlyingChart chart =
                    XuanKongCalculator.getPeriodChart(
                            PERIOD_YEARS[i]);

            assertNotNull(chart);

            assertEquals(i + 1, chart.centerStar);

            assertValidNineStars(chart.palaceStars);
        }
    }

    /**
     * 测试二十四山是否都能正确识别。
     */
    @Test
    public void testTwentyFourMountains() {

        assertEquals(24, MOUNTAINS.length);

        for (int i = 0; i < MOUNTAINS.length; i++) {

            int index =
                    XuanKongCalculator.getMountainIndex(
                            MOUNTAINS[i]);

            assertEquals(
                    "二十四山索引错误：" + MOUNTAINS[i],
                    i,
                    index
            );

            int palace =
                    XuanKongCalculator.getMountainPalace(
                            MOUNTAINS[i]);

            assertTrue(
                    "山向没有对应宫位：" + MOUNTAINS[i],
                    palace >= 1 && palace <= 8
            );
        }
    }

    /**
     * 测试每一山的相反方向。
     */
    @Test
    public void testOppositeMountains() {

        for (int i = 0; i < 24; i++) {

            String sitting = MOUNTAINS[i];

            String expectedFacing =
                    MOUNTAINS[(i + 12) % 24];

            XuanKongCalculator.ResidenceChart chart =
                    XuanKongCalculator.getResidenceChart(
                            2010,
                            sitting
                    );

            assertNotNull(chart);

            assertEquals(
                    "坐山与朝向不对应",
                    expectedFacing,
                    chart.facingMountain
            );
        }
    }

    /**
     * 216局自动结构校验：
     * 24山 × 9个元运。
     */
    @Test
    public void testAll216ResidenceCharts() {

        int tested = 0;

        for (int year : PERIOD_YEARS) {

            for (String mountain : MOUNTAINS) {

                XuanKongCalculator.ResidenceChart chart =
                        XuanKongCalculator.getResidenceChart(
                                year,
                                mountain
                        );

                assertNotNull(
                        "排盘失败：" + year + "年，" + mountain,
                        chart
                );

                assertValidNineStars(chart.periodStars);
                assertValidNineStars(chart.mountainStars);
                assertValidNineStars(chart.facingStars);

                assertEquals(
                        "运盘年份错误",
                        year,
                        chart.year
                );

                assertEquals(
                        "元运错误",
                        XuanKongCalculator
