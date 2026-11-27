package com.xuantian.compass;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZhongzhouReplacementRulesTest {

    /**
     * 中州派十三山替星表。
     */
    @Test
    public void testThirteenMountainReplacementTable() {
        assertStar(1, "甲", "申");
        assertStar(2, "壬", "卯", "乙");
        assertStar(7, "丑", "艮", "丙");
        assertStar(6, "巽", "辰", "巳");
        assertStar(9, "庚", "寅");
    }

    private void assertStar(int expected, String... mountains) {
        for (String mountain : mountains) {
            assertEquals(
                    "替星错误：" + mountain,
                    expected,
                    ZhongzhouReplacementRules.getReplacementStar(mountain)
            );
        }
    }

    /**
     * 不属于十三山替星表的十一山。
     */
    @Test
    public void testMountainsWithoutReplacementStar() {
        String[] mountains = {
                "子", "癸", "午", "丁", "未",
                "坤", "辛", "酉", "戌", "乾", "亥"
        };

        for (String mountain : mountains) {
            assertEquals(
                    -1,
                    ZhongzhouReplacementRules.getReplacementStar(mountain)
            );
        }
    }

    /**
     * 二十四山宫位。
     */
    @Test
    public void testMountainPalace() {
        assertEquals(1, ZhongzhouReplacementRules.getMountainPalace("戌"));
        assertEquals(2, ZhongzhouReplacementRules.getMountainPalace("庚"));
        assertEquals(3, ZhongzhouReplacementRules.getMountainPalace("艮"));
        assertEquals(4, ZhongzhouReplacementRules.getMountainPalace("丙"));
        assertEquals(5, ZhongzhouReplacementRules.getMountainPalace("壬"));
        assertEquals(6, ZhongzhouReplacementRules.getMountainPalace("坤"));
        assertEquals(7, ZhongzhouReplacementRules.getMountainPalace("甲"));
        assertEquals(8, ZhongzhouReplacementRules.getMountainPalace("巽"));
        assertEquals(-1, ZhongzhouReplacementRules.getMountainPalace("错误"));
    }

    /**
     * 三元龙索引。
     */
    @Test
    public void testDragonIndex() {
        assertEquals(0, ZhongzhouReplacementRules.getDragonIndex("丑"));
        assertEquals(1, ZhongzhouReplacementRules.getDragonIndex("艮"));
        assertEquals(2, ZhongzhouReplacementRules.getDragonIndex("寅"));

        assertEquals(0, ZhongzhouReplacementRules.getDragonIndex("甲"));
        assertEquals(1, ZhongzhouReplacementRules.getDragonIndex("卯"));
        assertEquals(2, ZhongzhouReplacementRules.getDragonIndex("乙"));
    }

    /**
     * 艮兼寅：同宫同阳，不替。
     */
    @Test
    public void testGenMountainWithYin() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair("艮", "寅", true);

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.SAME_PALACE_SAME_YINYANG,
                result.status
        );

        assertEquals(3, result.originalPalace);
        assertEquals(3, result.adjacentPalace);

        assertTrue(result.originalYang);
        assertTrue(result.adjacentYang);
        assertFalse(result.needsReplacement);
    }

    /**
     * 丑兼艮：同宫阴阳不同，进入替卦判断。
     */
    @Test
    public void testChouMountainWithGen() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair("丑", "艮", true);

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.SAME_PALACE_DIFFERENT_YINYANG,
                result.status
        );

        assertFalse(result.originalYang);
        assertTrue(result.adjacentYang);
        assertTrue(result.needsReplacement);
    }

    /**
     * 壬兼亥：不同宫，出卦。
     */
    @Test
    public void testRenMountainWithHai() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair("壬", "亥", true);

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.OUT_OF_PALACE,
                result.status
        );

        assertTrue(result.needsReplacement);
        assertEquals(5, result.originalPalace);
        assertEquals(1, result.adjacentPalace);
    }

    /**
     * 未进入兼向范围，按下卦处理。
     */
    @Test
    public void testDownGua() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair("艮", "寅", false);

        assertEquals(
                ZhongzhouReplacementRules.Status.DOWN_GUA,
                result.status
        );

        assertFalse(result.needsReplacement);
    }

    /**
     * 无效输入。
     */
    @Test
    public void testInvalidInput() {
        ZhongzhouReplacementRules.Result result1 =
                ZhongzhouReplacementRules.analyzePair(null, "寅", true);

        assertEquals(
                ZhongzhouReplacementRules.Status.INVALID_INPUT,
                result1.status
        );

        ZhongzhouReplacementRules.Result result2 =
                ZhongzhouReplacementRules.analyzePair("错误", "寅", true);

        assertEquals(
                ZhongzhouReplacementRules.Status.INVALID_INPUT,
                result2.status
        );
    }

    /**
     * 阴阳映射。
     */
    @Test
    public void testYinYang() {
        assertFalse(ZhongzhouReplacementRules.isYang("丑"));
        assertTrue(ZhongzhouReplacementRules.isYang("艮"));
        assertTrue(ZhongzhouReplacementRules.isYang("寅"));

        assertTrue(ZhongzhouReplacementRules.isYang("甲"));
        assertFalse(ZhongzhouReplacementRules.isYang("卯"));
        assertFalse(ZhongzhouReplacementRules.isYang("乙"));
    }

    /**
     * 空值和空白字符。
     */
    @Test
    public void testEmptyAndWhitespaceInput() {
        assertEquals(
                -1,
                ZhongzhouReplacementRules.getReplacementStar(null)
        );

        assertEquals(
                -1,
                ZhongzhouReplacementRules.getMountainPalace(null)
        );

        assertEquals(
                -1,
                ZhongzhouReplacementRules.getReplacementStar(" ")
        );

        assertEquals(
                3,
                ZhongzhouReplacementRules.getMountainPalace(" 艮 ")
        );
    }

    /**
     * 核心案例：
     * 八运壬山丙向兼亥巳。
     *
     * 山星四绿入巽宫，
     * 同元龙取辰，
     * 辰替六白。
     *
     * 辰为阴，方法A逆飞。
     */
    @Test
    public void testEightPeriodRenMountainReplacement() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.calculateReplacement(
                        4, "壬", "亥", true
                );

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.OUT_OF_PALACE,
                result.status
        );

        assertEquals(4, result.originalStar);
        assertEquals("辰", result.replacementMountain);
        assertEquals(8, result.replacementPalace);
        assertEquals(6, result.replacementStar);

        assertTrue(result.hasReplacement);

        assertFalse(result.methodAForward);
        assertEquals("阴逆飞", result.methodA);
    }

    /**
     * 向星三碧入震宫，
     * 同元龙取甲，
     * 甲替一白。
     *
     * 方法A阳顺飞。
     */
    @Test
    public void testFacingStarThreeReplacement() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.calculateReplacement(
                        3, "丙", "巳", true
                );

        assertNotNull(result);

        assertEquals("甲", result.replacementMountain);
        assertEquals(7, result.replacementPalace);
        assertEquals(1, result.replacementStar);

        assertTrue(result.hasReplacement);
        assertTrue(result.methodAForward);
        assertEquals("阳顺飞", result.methodA);
    }

    /**
     * 五黄特殊处理：
     * 不产生普通替星。
     */
    @Test
    public void testFiveYellowSpecial() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.calculateReplacement(
                        5, "壬", "亥", true
                );

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.FIVE_YELLOW_SPECIAL,
                result.status
        );

        assertFalse(result.hasReplacement);
        assertEquals(-1, result.replacementStar);
        assertNull(result.replacementMountain);

        // 壬为阳，按原山元龙阴阳顺飞。
        assertTrue(result.methodAForward);
        assertEquals("阳顺飞", result.methodA);
    }

    /**
     * 替卦山不在十三山表内。
     *
     * 一白入坎宫，同元龙取癸；
     * 癸不属于十三山替星表。
     */
    @Test
    public void testNoReplacementStar() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.calculateReplacement(
                        1, "癸", "壬", true
                );

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status.NO_REPLACEMENT_STAR,
                result.status
        );

        assertEquals("癸", result.replacementMountain);
        assertEquals(5, result.replacementPalace);
        assertEquals(-1, result.replacementStar);

        assertFalse(result.hasReplacement);
    }

    /**
     * 原运星输入超出范围。
     */
    @Test
    public void testInvalidOriginalStar() {
        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.calculateReplacement(
                        10, "壬", "亥", true
                );

        assertEquals(
                ZhongzhouReplacementRules.Status.INVALID_INPUT,
                result.status
        );
    }

    /**
     * 检查替星存在性。
     */
    @Test
    public void testHasReplacementStar() {
        assertTrue(ZhongzhouReplacementRules.hasReplacementStar("甲"));
        assertTrue(ZhongzhouReplacementRules.hasReplacementStar("辰"));

        assertFalse(ZhongzhouReplacementRules.hasReplacementStar("子"));
        assertFalse(ZhongzhouReplacementRules.hasReplacementStar("癸"));
    }
}
