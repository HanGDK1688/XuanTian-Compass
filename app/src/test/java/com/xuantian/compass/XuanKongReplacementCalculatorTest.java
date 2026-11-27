package com.xuantian.compass;

import org.junit.Test;

import static org.junit.Assert.*;

public class XuanKongReplacementCalculatorTest {

    /**
     * 测试二十四山索引。
     */
    @Test
    public void testTwentyFourMountainIndexes() {

        String[] mountains = {
                "子", "癸", "丑",
                "艮", "寅", "甲",
                "卯", "乙", "辰",
                "巽", "巳", "丙",
                "午", "丁", "未",
                "坤", "申", "庚",
                "酉", "辛", "戌",
                "乾", "亥", "壬"
        };

        for (int i = 0; i < mountains.length; i++) {
            assertEquals(
                    "二十四山索引错误：" + mountains[i],
                    i,
                    XuanKongReplacementCalculator
                            .getMountainIndex(mountains[i])
            );
        }

        assertEquals(
                -1,
                XuanKongReplacementCalculator
                        .getMountainIndex("错误")
        );
    }

    /**
     * 测试乙山109.80度。
     */
    @Test
    public void testYiMountain109_80Degrees() {

        XuanKongReplacementCalculator.AngleResult result =
                XuanKongReplacementCalculator
                        .analyzeBearing(109.80);

        assertNotNull(result);

        assertEquals("乙", result.mountain);
        assertEquals("辰", result.nextMountain);

        assertEquals(105.0, result.centerAngle, 0.001);
        assertEquals(4.80, result.deviation, 0.001);

        assertEquals("替卦", result.classification);
        assertEquals("兼辰", result.direction);

        assertTrue(result.isReplacementGua);
        assertFalse(result.isDownGua);
        assertFalse(result.isBoundary);
    }

    /**
     * 测试辛向289.80度。
     */
    @Test
    public void testXinMountain289_80Degrees() {

        XuanKongReplacementCalculator.AngleResult result =
                XuanKongReplacementCalculator
                        .analyzeBearing(289.80);

        assertNotNull(result);

        assertEquals("辛", result.mountain);
        assertEquals("戌", result.nextMountain);

        assertEquals(285.0, result.centerAngle, 0.001);
        assertEquals(4.80, result.deviation, 0.001);

        assertEquals("替卦", result.classification);
        assertEquals("兼戌", result.direction);

        assertTrue(result.isReplacementGua);
        assertFalse(result.isDownGua);
        assertFalse(result.isBoundary);
    }

    /**
     * 测试下卦范围。
     */
    @Test
    public void testDownGuaRange() {

        XuanKongReplacementCalculator.AngleResult result1 =
                XuanKongReplacementCalculator
                        .analyzeBearing(105.0);

        assertEquals("下卦", result1.classification);
        assertTrue(result1.isDownGua);

        XuanKongReplacementCalculator.AngleResult result2 =
                XuanKongReplacementCalculator
                        .analyzeBearing(109.5);

        assertEquals("下卦", result2.classification);
        assertTrue(result2.isDownGua);
    }

    /**
     * 测试替卦范围。
     */
    @Test
    public void testReplacementGuaRange() {

        XuanKongReplacementCalculator.AngleResult result =
                XuanKongReplacementCalculator
                        .analyzeBearing(109.6);

        assertEquals("乙", result.mountain);
        assertEquals("替卦", result.classification);

        assertTrue(result.isReplacementGua);
    }

    /**
     * 测试山向边界。
     */
    @Test
    public void testBoundaryRange() {

        XuanKongReplacementCalculator.AngleResult result1 =
                XuanKongReplacementCalculator
                        .analyzeBearing(112.5);

        assertNotNull(result1);
        assertTrue(result1.isBoundary);
        assertEquals("边界", result1.classification);

        XuanKongReplacementCalculator.AngleResult result2 =
                XuanKongReplacementCalculator
                        .analyzeBearing(97.5);

        assertNotNull(result2);
        assertTrue(result2.isBoundary);
        assertEquals("边界", result2.classification);
    }

    /**
     * 测试二十四山替星表。
     */
    @Test
    public void testReplacementStarTable() {

        assertReplacementStar(1,
                "子", "甲", "癸", "申");

        assertReplacementStar(2,
                "坤", "壬", "乙", "卯", "未");

        assertReplacementStar(6,
                "乾", "戌", "巳", "辰", "巽", "亥");

        assertReplacementStar(7,
                "艮", "丑", "丙", "辛", "酉");

        assertReplacementStar(9,
                "寅", "午", "庚", "丁");

        assertEquals(
                -1,
                XuanKongReplacementCalculator
                        .getReplacementStar("错误")
        );
    }

    private void assertReplacementStar(
            int expectedStar,
            String... mountains) {

        for (String mountain : mountains) {

            assertEquals(
                    "替星表错误：" + mountain,
                    expectedStar,
                    XuanKongReplacementCalculator
                            .getReplacementStar(mountain)
            );
        }
    }

    /**
     * 测试九星原始宫位。
     */
    @Test
    public void testStarOriginalPalace() {

        int[] expected = {
                -1,
                5, 6, 7, 8, 0, 1, 2, 3, 4
        };

        for (int star = 1; star <= 9; star++) {

            assertEquals(
                    expected[star],
                    XuanKongReplacementCalculator
                            .getStarOriginalPalace(star)
            );
        }
    }

    /**
     * 测试乙山替卦计算。
     */
    @Test
    public void testYiMountainReplacementCalculation() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(6, "乙");

        assertNotNull(result);

        assertEquals("乙", result.originalMountain);

        assertEquals(6, result.originalStar);
        assertEquals("亥", result.replacementMountain);
        assertEquals(6, result.replacementStar);

        assertEquals(1, result.originalPalace);
        assertEquals(1, result.replacementPalace);

        assertEquals(2, result.dragonIndex);

        assertTrue(result.methodAForward);
        assertTrue(result.methodBForward);

        assertEquals("顺飞", result.methodA);
        assertEquals("顺飞", result.methodB);

        assertTrue(result.hasReplacement);
    }

    /**
     * 测试辛向替卦计算。
     */
    @Test
    public void testXinMountainReplacementCalculation() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(1, "辛");

        assertNotNull(result);

        assertEquals("辛", result.originalMountain);

        assertEquals(1, result.originalStar);
        assertEquals("癸", result.replacementMountain);
        assertEquals(1, result.replacementStar);

        assertEquals(5, result.originalPalace);
        assertEquals(5, result.replacementPalace);

        assertEquals(2, result.dragonIndex);

        assertFalse(result.methodAForward);
        assertFalse(result.methodBForward);

        assertEquals("逆飞", result.methodA);
        assertEquals("逆飞", result.methodB);

        assertTrue(result.hasReplacement);
    }

    /**
     * 新增测试：
     * 原运星与替星不同。
     *
     * 原运星三碧，原山子。
     * 震宫天元龙对应卯。
     * 卯的替星为二黑。
     *
     * 方案A：逆飞。
     * 方案B：顺飞。
     */
    @Test
    public void testDifferentReplacementStarAndOppositeMethodsA() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(3, "子");

        assertNotNull(result);

        assertEquals(3, result.originalStar);
        assertEquals("卯", result.replacementMountain);
        assertEquals(2, result.replacementStar);

        assertNotEquals(
                result.originalStar,
                result.replacementStar
        );

        assertEquals(7, result.originalPalace);
        assertEquals(6, result.replacementPalace);

        assertEquals(1, result.dragonIndex);

        assertFalse(result.methodAForward);
        assertTrue(result.methodBForward);

        assertEquals("逆飞", result.methodA);
        assertEquals("顺飞", result.methodB);

        assertTrue(result.hasReplacement);
    }

    /**
     * 新增测试：
     * 验证另一种A/B顺逆相反的情况。
     *
     * 原运星八白，原山子。
     * 艮宫天元龙对应艮。
     * 艮的替星为七赤。
     *
     * 方案A：顺飞。
     * 方案B：逆飞。
     */
    @Test
    public void testDifferentReplacementStarAndOppositeMethodsB() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(8, "子");

        assertNotNull(result);

        assertEquals(8, result.originalStar);
        assertEquals("艮", result.replacementMountain);
        assertEquals(7, result.replacementStar);

        assertNotEquals(
                result.originalStar,
                result.replacementStar
        );

        assertEquals(3, result.originalPalace);
        assertEquals(2, result.replacementPalace);

        assertEquals(1, result.dragonIndex);

        assertTrue(result.methodAForward);
        assertFalse(result.methodBForward);

        assertEquals("顺飞", result.methodA);
        assertEquals("逆飞", result.methodB);

        assertTrue(result.hasReplacement);
    }

    /**
     * 新增测试：
     * 验证替星不同，但A、B两种方法都顺飞。
     *
     * 原运星四绿，原山子。
     * 巽宫天元龙对应巽。
     * 巽的替星为六白。
     */
    @Test
    public void testDifferentReplacementStarBothForward() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(4, "子");

        assertNotNull(result);

        assertEquals(4, result.originalStar);
        assertEquals("巽", result.replacementMountain);
        assertEquals(6, result.replacementStar);

        assertNotEquals(
                result.originalStar,
                result.replacementStar
        );

        assertTrue(result.methodAForward);
        assertTrue(result.methodBForward);

        assertEquals("顺飞", result.methodA);
        assertEquals("顺飞", result.methodB);

        assertTrue(result.hasReplacement);
    }

    /**
     * 新增测试：
     * 验证五黄入中没有有效替卦结果。
     */
    @Test
    public void testFiveYellowCenterHasNoReplacement() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(5, "子");

        assertNull(result);
    }

    /**
     * 新增测试：
     * 验证替卦结果的字段完整性。
     */
    @Test
    public void testReplacementResultFields() {

        XuanKongReplacementCalculator.ReplacementResult result =
                XuanKongReplacementCalculator
                        .calculateReplacement(3, "子");

        assertNotNull(result);

        assertNotNull(result.originalMountain);
        assertNotNull(result.replacementMountain);

        assertTrue(result.originalStar >= 1);
        assertTrue(result.originalStar <= 9);

        assertTrue(result.replacementStar >= 1);
        assertTrue(result.replacementStar <= 9);

        assertTrue(result.originalPalace >= 1);
        assertTrue(result.originalPalace <= 8);

        assertTrue(result.replacementPalace >= 1);
        assertTrue(result.replacementPalace <= 8);

        assertTrue(result.dragonIndex >= 0);
        assertTrue(result.dragonIndex <= 2);

        assertNotNull(result.methodA);
        assertNotNull(result.methodB);
        assertNotNull(result.description);

        assertTrue(result.hasReplacement);
    }

    /**
     * 测试无效输入。
     */
    @Test
    public void testInvalidInput() {

        assertNull(
                XuanKongReplacementCalculator
                        .analyzeBearing(Double.NaN)
        );

        assertNull(
                XuanKongReplacementCalculator
                        .analyzeBearing(Double.POSITIVE_INFINITY)
        );

        assertNull(
                XuanKongReplacementCalculator
                        .calculateReplacement(0, "乙")
        );

        assertNull(
                XuanKongReplacementCalculator
                        .calculateReplacement(6, "错误")
        );

        assertNull(
                XuanKongReplacementCalculator
                        .calculateReplacement(6, null)
        );
    }

    /**
     * 测试角度归一化。
     */
    @Test
    public void testNormalizeAngle() {

        assertEquals(
                10.0,
                XuanKongReplacementCalculator
                        .normalizeAngle(370.0),
                0.001
        );

        assertEquals(
                350.0,
                XuanKongReplacementCalculator
                        .normalizeAngle(-10.0),
                0.001
        );

        assertEquals(
                0.0,
                XuanKongReplacementCalculator
                        .normalizeAngle(720.0),
                0.001
        );
    }

    /**
     * 测试角度格式化。
     */
    @Test
    public void testFormatAngleResult() {

        XuanKongReplacementCalculator.AngleResult result =
                XuanKongReplacementCalculator
                        .analyzeBearing(109.80);

        String text =
                XuanKongReplacementCalculator
                        .formatAngleResult(result);

        assertNotNull(text);
        assertTrue(text.contains("乙"));
        assertTrue(text.contains("兼辰"));
        assertTrue(text.contains("替卦"));
    }
}
