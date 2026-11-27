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

    private void assertValidNineStars(int[] stars) {
        assertNotNull("星盘不能为null", stars);
        assertEquals("九宫必须有9个位置", 9, stars.length);

        boolean[] found = new boolean[10];

        for (int star : stars) {
            assertTrue("星数必须在1至9之间", star >= 1 && star <= 9);
            assertFalse("九宫不能出现重复星数", found[star]);
            found[star] = true;
        }

        for (int i = 1; i <= 9; i++) {
            assertTrue("缺少星数：" + i, found[i]);
        }
    }

    /**
     * 验证一组替卦候选盘。
     */
    private void assertReplacementChart(
            String description,
            boolean available,
            XuanKongReplacementCalculator.ReplacementResult result,
            int[] starsA,
            int[] starsB) {

        if (!available) {
            assertNull(description + "：不可用时结果应为null", result);
            assertNull(description + "：方案A应为null", starsA);
            assertNull(description + "：方案B应为null", starsB);
            return;
        }

        assertNotNull(description + "：替卦结果不能为空", result);
        assertTrue(description + "：替卦结果必须有效",
                result.hasReplacement);

        assertTrue(description + "：替星必须在1至9之间",
                result.replacementStar >= 1
                        && result.replacementStar <= 9);

        assertValidNineStars(starsA);
        assertValidNineStars(starsB);

        assertEquals(description + "：方案A中宫星必须等于替星",
                result.replacementStar, starsA[0]);

        assertEquals(description + "：方案B中宫星必须等于替星",
                result.replacementStar, starsB[0]);
    }

    /**
     * 测试九个元运。
     */
    @Test
    public void testNinePeriods() {
        for (int i = 0; i < 9; i++) {
            XuanKongCalculator.PeriodInfo info =
                    XuanKongCalculator.getPeriod(PERIOD_YEARS[i]);

            assertNotNull("元运不能为空", info);
            assertEquals("元运编号错误", i + 1, info.period);

            XuanKongCalculator.FlyingChart chart =
                    XuanKongCalculator.getPeriodChart(PERIOD_YEARS[i]);

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
            int index = XuanKongCalculator.getMountainIndex(MOUNTAINS[i]);
            assertEquals("二十四山索引错误：" + MOUNTAINS[i], i, index);

            int palace = XuanKongCalculator.getMountainPalace(MOUNTAINS[i]);
            assertTrue("山向没有对应宫位：" + MOUNTAINS[i],
                    palace >= 1 && palace <= 8);
        }
    }

    /**
     * 测试每一山的相反方向。
     */
    @Test
    public void testOppositeMountains() {
        for (int i = 0; i < 24; i++) {
            String sitting = MOUNTAINS[i];
            String expectedFacing = MOUNTAINS[(i + 12) % 24];

            XuanKongCalculator.ResidenceChart chart =
                    XuanKongCalculator.getResidenceChart(2010, sitting);

            assertNotNull(chart);
            assertEquals("坐山与朝向不对应",
                    expectedFacing, chart.facingMountain);
        }
    }

    /**
     * 216局自动结构校验：24山 × 9个元运。
     */
    @Test
    public void testAll216ResidenceCharts() {
        int tested = 0;

        for (int year : PERIOD_YEARS) {
            for (String mountain : MOUNTAINS) {
                XuanKongCalculator.ResidenceChart chart =
                        XuanKongCalculator.getResidenceChart(year, mountain);

                assertNotNull("排盘失败：" + year + "年，" + mountain, chart);

                assertValidNineStars(chart.periodStars);
                assertValidNineStars(chart.mountainStars);
                assertValidNineStars(chart.facingStars);

                assertEquals("运盘年份错误", year, chart.year);

                XuanKongCalculator.PeriodInfo info =
                        XuanKongCalculator.getPeriod(year);

                assertNotNull(info);
                assertEquals("元运错误", info.period, chart.period);

                assertEquals("运盘中宫星错误",
                        chart.period, chart.periodStars[0]);

                assertEquals("山盘中宫星错误",
                        chart.mountainCenterStar, chart.mountainStars[0]);

                assertEquals("向盘中宫星错误",
                        chart.facingCenterStar, chart.facingStars[0]);

                assertEquals("坐山错误",
                        mountain, chart.sittingMountain);

                assertEquals("朝向错误",
                        MOUNTAINS[(XuanKongCalculator.getMountainIndex(mountain)
                                + 12) % 24],
                        chart.facingMountain);

                tested++;
            }
        }

        assertEquals("必须完成216局测试", 216, tested);
    }

    /**
     * 测试216局中的四套替卦候选盘。
     */
    @Test
    public void testAll216ReplacementCandidateCharts() {
        int tested = 0;
        int mountainAvailableCount = 0;
        int facingAvailableCount = 0;

        for (int year : PERIOD_YEARS) {
            for (String mountain : MOUNTAINS) {

                XuanKongCalculator.ResidenceChart chart =
                        XuanKongCalculator.getResidenceChart(year, mountain);

                assertNotNull("住宅排盘不能为空", chart);

                String description =
                        year + "年，" + mountain + "山，"
                                + chart.facingMountain + "向";

                assertReplacementChart(
                        description + "坐山替卦",
                        chart.mountainReplacementAvailable,
                        chart.mountainReplacementResult,
                        chart.mountainReplacementStarsA,
                        chart.mountainReplacementStarsB
                );

                if (chart.mountainReplacementAvailable) {
                    mountainAvailableCount++;
                }

                assertReplacementChart(
                        description + "朝向替卦",
                        chart.facingReplacementAvailable,
                        chart.facingReplacementResult,
                        chart.facingReplacementStarsA,
                        chart.facingReplacementStarsB
                );

                if (chart.facingReplacementAvailable) {
                    facingAvailableCount++;
                }

                tested++;
            }
        }

        assertEquals("必须完成216局替卦候选盘测试",
                216, tested);

        assertTrue("应当至少存在一局坐山替卦候选盘",
                mountainAvailableCount > 0);

        assertTrue("应当至少存在一局朝向替卦候选盘",
                facingAvailableCount > 0);
    }

    /**
     * 测试替卦候选盘不会改变原来的基础下卦盘。
     */
    @Test
    public void testReplacementDoesNotChangeOriginalChart() {

        XuanKongCalculator.ResidenceChart chart =
                XuanKongCalculator.getResidenceChart(2014, "乙");

        assertNotNull(chart);

        assertArrayEquals(
                "原运盘不能改变",
                new int[]{8, 9, 1, 2, 3, 4, 5, 6, 7},
                chart.periodStars
        );

        assertArrayEquals(
                "原山盘不能改变",
                new int[]{6, 7, 8, 9, 1, 2, 3, 4, 5},
                chart.mountainStars
        );

        assertArrayEquals(
                "原向盘不能改变",
                new int[]{1, 9, 8, 7, 6, 5, 4, 3, 2},
                chart.facingStars
        );
    }

    /**
     * 2014年乙山辛向基准测试。
     */
    @Test
    public void test2014YiMountainXinFacing() {
        XuanKongCalculator.ResidenceChart chart =
                XuanKongCalculator.getResidenceChart(2014, "乙");

        assertNotNull(chart);

        assertEquals("2014年应为八运", 8, chart.period);

        assertEquals("乙山对应辛向", "辛", chart.facingMountain);

        assertArrayEquals("运盘结果不符",
                new int[]{8, 9, 1, 2, 3, 4, 5, 6, 7},
                chart.periodStars);

        assertArrayEquals("山盘结果不符",
                new int[]{6, 7, 8, 9, 1, 2, 3, 4, 5},
                chart.mountainStars);

        assertArrayEquals("向盘结果不符",
                new int[]{1, 9, 8, 7, 6, 5, 4, 3, 2},
                chart.facingStars);
    }

    /**
     * 测试顺飞九宫数组。
     *
     * 中宫入三碧，顺飞：
     * 中、乾、兑、艮、离、坎、坤、震、巽。
     */
    @Test
    public void testForwardFlyingArray() {

        int[] stars =
                XuanKongCalculator.flyFromCenter(3, true);

        assertArrayEquals(
                "三碧顺飞数组错误",
                new int[]{3, 4, 5, 6, 7, 8, 9, 1, 2},
                stars
        );

        assertValidNineStars(stars);
    }

    /**
     * 测试逆飞九宫数组。
     */
    @Test
    public void testReverseFlyingArray() {

        int[] stars =
                XuanKongCalculator.flyFromCenter(3, false);

        assertArrayEquals(
                "三碧逆飞数组错误",
                new int[]{3, 2, 1, 9, 8, 7, 6, 5, 4},
                stars
        );

        assertValidNineStars(stars);
    }

    /**
     * 测试顺飞与逆飞结果确实不同。
     */
    @Test
    public void testForwardAndReverseArraysAreDifferent() {

        int[] forward =
                XuanKongCalculator.flyFromCenter(2, true);

        int[] reverse =
                XuanKongCalculator.flyFromCenter(2, false);

        assertValidNineStars(forward);
        assertValidNineStars(reverse);

        assertFalse(
                "顺飞和逆飞不能生成相同的九宫盘",
                java.util.Arrays.equals(forward, reverse)
        );
    }

    /**
     * 测试替卦方案A、B方向不同的情况。
     *
     * 三碧原运星、子山：
     * 方案A逆飞，方案B顺飞。
     */
    @Test
    public void testReplacementMethodsDifferentDirections() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator.calculateReplacement(
                        3, "子"
                );

        assertNotNull(result);
        assertTrue(result.hasReplacement);

        assertFalse("方案A应逆飞", result.methodAForward);
        assertTrue("方案B应顺飞", result.methodBForward);

        int[] starsA =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodAForward
                );

        int[] starsB =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodBForward
                );

        assertValidNineStars(starsA);
        assertValidNineStars(starsB);

        assertFalse(
                "方案A和方案B方向不同，九宫数组也应不同",
                java.util.Arrays.equals(starsA, starsB)
        );
    }

    /**
     * 测试另一组相反方向组合。
     *
     * 八白原运星、子山：
     * 方案A顺飞，方案B逆飞。
     */
    @Test
    public void testReplacementMethodsOppositeDirections() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator.calculateReplacement(
                        8, "子"
                );

        assertNotNull(result);
        assertTrue(result.hasReplacement);

        assertTrue("方案A应顺飞", result.methodAForward);
        assertFalse("方案B应逆飞", result.methodBForward);

        int[] starsA =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodAForward
                );

        int[] starsB =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodBForward
                );

        assertValidNineStars(starsA);
        assertValidNineStars(starsB);

        assertFalse(
                "两种相反方向必须生成不同九宫盘",
                java.util.Arrays.equals(starsA, starsB)
        );
    }

    /**
     * 测试A、B方向相同时，生成的九宫盘相同。
     *
     * 四绿原运星、子山：
     * 方案A和方案B均顺飞。
     */
    @Test
    public void testReplacementMethodsSameDirection() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator.calculateReplacement(
                        4, "子"
                );

        assertNotNull(result);
        assertTrue(result.hasReplacement);

        assertTrue("方案A应顺飞", result.methodAForward);
        assertTrue("方案B应顺飞", result.methodBForward);

        int[] starsA =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodAForward
                );

        int[] starsB =
                XuanKongCalculator.flyFromCenter(
                        result.replacementStar,
                        result.methodBForward
                );

        assertValidNineStars(starsA);
        assertValidNineStars(starsB);

        assertArrayEquals(
                "A、B同为顺飞时，九宫数组应完全相同",
                starsA,
                starsB
        );
    }

    /**
     * 测试九宫完整性验证器能够识别非法数据。
     */
    @Test
    public void testNineStarChartValidation() {

        assertTrue(
                "正常九宫盘应通过验证",
                XuanKongCalculator.isValidNineStarChart(
                        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}
                )
        );

        assertFalse(
                "重复星数应验证失败",
                XuanKongCalculator.isValidNineStarChart(
                        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 8}
                )
        );

        assertFalse(
                "缺少星数应验证失败",
                XuanKongCalculator.isValidNineStarChart(
                        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 0}
                )
        );

        assertFalse(
                "超出范围的星数应验证失败",
                XuanKongCalculator.isValidNineStarChart(
                        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 10}
                )
        );

        assertFalse(
                "空数组应验证失败",
                XuanKongCalculator.isValidNineStarChart(null)
        );

        assertFalse(
                "长度不为9的数组应验证失败",
                XuanKongCalculator.isValidNineStarChart(
                        new int[]{1, 2, 3, 4, 5, 6, 7, 8}
                )
        );
    }

    /**
     * 测试非法山向输入。
     */
    @Test
    public void testInvalidMountainInput() {
        assertEquals(-1, XuanKongCalculator.getMountainIndex("错误"));
        assertEquals(-1, XuanKongCalculator.getMountainIndex(""));
        assertEquals(-1, XuanKongCalculator.getMountainIndex(null));
    }

    /**
     * 测试元运范围之外的年份。
     */
    @Test
    public void testYearOutsidePeriodRange() {
        assertNull(XuanKongCalculator.getPeriod(1863));
        assertNull(XuanKongCalculator.getPeriod(2044));
    }
}
