package com.xuantian.compass;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZhongzhouReplacementRulesTest {

    /**
     * 测试中州派十三山替星表。
     */
    @Test
    public void testThirteenMountainReplacementTable() {

        assertStar(1, "甲", "申");

        assertStar(2, "壬", "卯", "乙");

        assertStar(7, "丑", "艮", "丙");

        assertStar(6, "巽", "辰", "巳");

        assertStar(9, "庚", "寅");
    }

    private void assertStar(
            int expected,
            String... mountains) {

        for (String mountain : mountains) {

            assertEquals(
                    "替星错误：" + mountain,
                    expected,
                    ZhongzhouReplacementRules
                            .getReplacementStar(mountain)
            );
        }
    }

    /**
     * 测试不属于十三山的山。
     */
    @Test
    public void testMountainsWithoutReplacementStar() {

        String[] mountains = {
                "子", "癸",
                "午", "丁",
                "未", "坤",
                "辛", "酉",
                "戌", "乾", "亥"
        };

        for (String mountain : mountains) {

            assertEquals(
                    -1,
                    ZhongzhouReplacementRules
                            .getReplacementStar(mountain)
            );
        }
    }

    /**
     * 测试二十四山宫位。
     */
    @Test
    public void testMountainPalace() {

        assertEquals(1,
                ZhongzhouReplacementRules.getMountainPalace("戌"));

        assertEquals(2,
                ZhongzhouReplacementRules.getMountainPalace("庚"));

        assertEquals(3,
                ZhongzhouReplacementRules.getMountainPalace("艮"));

        assertEquals(4,
                ZhongzhouReplacementRules.getMountainPalace("丙"));

        assertEquals(5,
                ZhongzhouReplacementRules.getMountainPalace("壬"));

        assertEquals(6,
                ZhongzhouReplacementRules.getMountainPalace("坤"));

        assertEquals(7,
                ZhongzhouReplacementRules.getMountainPalace("甲"));

        assertEquals(8,
                ZhongzhouReplacementRules.getMountainPalace("巽"));

        assertEquals(-1,
                ZhongzhouReplacementRules.getMountainPalace("错误"));
    }

    /**
     * 测试三元龙索引。
     */
    @Test
    public void testDragonIndex() {

        assertEquals(0,
                ZhongzhouReplacementRules.getDragonIndex("丑"));

        assertEquals(1,
                ZhongzhouReplacementRules.getDragonIndex("艮"));

        assertEquals(2,
                ZhongzhouReplacementRules.getDragonIndex("寅"));

        assertEquals(0,
                ZhongzhouReplacementRules.getDragonIndex("甲"));

        assertEquals(1,
                ZhongzhouReplacementRules.getDragonIndex("卯"));

        assertEquals(2,
                ZhongzhouReplacementRules.getDragonIndex("乙"));
    }

    /**
     * 艮兼寅：
     * 同宫、同阳，不替。
     */
    @Test
    public void testGenMountainWithYin() {

        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair(
                        "艮",
                        "寅",
                        true
                );

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status
                        .SAME_PALACE_SAME_YINYANG,
                result.status
        );

        assertEquals(3, result.originalPalace);

        assertEquals(3, result.adjacentPalace);

        assertTrue(result.originalYang);

        assertTrue(result.adjacentYang);

        assertFalse(result.needsReplacement);
    }

    /**
     * 丑兼艮：
     * 同宫、阴阳不同，替卦。
     */
    @Test
    public void testChouMountainWithGen() {

        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair(
                        "丑",
                        "艮",
                        true
                );

        assertNotNull(result);

        assertEquals(
                ZhongzhouReplacementRules.Status
                        .SAME_PALACE_DIFFERENT_YINYANG,
                result.status
        );

        assertFalse(result.originalYang);

        assertTrue(result.adjacentYang);

        assertTrue(result.needsReplacement);
    }

    /**
     * 壬兼亥：
     * 不同卦宫，出卦。
     */
    @Test
    public void testRenMountainWithHai() {

        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair(
                        "壬",
                        "亥",
                        true
                );

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
     * 未进入兼向范围：
     * 按下卦处理。
     */
    @Test
    public void testDownGua() {

        ZhongzhouReplacementRules.Result result =
                ZhongzhouReplacementRules.analyzePair(
                        "艮",
                        "寅",
                        false
                );

        assertEquals(
                ZhongzhouReplacementRules.Status.DOWN_GUA,
                result.status
        );

        assertFalse(result.needsReplacement);
    }

    /**
     * 测试无效输入。
     */
    @Test
    public void testInvalidInput() {

        ZhongzhouReplacementRules.Result result1 =
                ZhongzhouReplacementRules.analyzePair(
                        null,
                        "寅",
                        true
                );

        assertEquals(
                ZhongzhouReplacementRules.Status.INVALID_INPUT,
                result1.status
        );

        ZhongzhouReplacementRules.Result result2 =
                ZhongzhouReplacementRules.analyzePair(
                        "错误",
                        "寅",
                        true
                );

        assertEquals(
                ZhongzhouReplacementRules.Status.INVALID_INPUT,
                result2.status
        );
    }

    /**
     * 测试阴阳映射。
     */
    @Test
    public void testYinYang() {

        assertFalse(
                ZhongzhouReplacementRules.isYang("丑")
        );

        assertTrue(
                ZhongzhouReplacementRules.isYang("艮")
        );

        assertTrue(
                ZhongzhouReplacementRules.isYang("寅")
        );

        assertTrue(
                ZhongzhouReplacementRules.isYang("甲")
        );

        assertFalse(
                ZhongzhouReplacementRules.isYang("卯")
        );

        assertFalse(
                ZhongzhouReplacementRules.isYang("乙")
        );
    }

    /**
     * 测试空值和空白字符。
     */
    @Test
    public void testEmptyAndWhitespaceInput() {

        assertEquals(
                -1,
                ZhongzhouReplacementRules
                        .getReplacementStar(null)
        );

        assertEquals(
                -1,
                ZhongzhouReplacementRules
                        .getMountainPalace(null)
        );

        assertEquals(
                -1,
                ZhongzhouReplacementRules
                        .getReplacementStar(" ")
        );

        assertEquals(
                3,
                ZhongzhouReplacementRules
                        .getMountainPalace(" 艮 ")
        );
    }
}
