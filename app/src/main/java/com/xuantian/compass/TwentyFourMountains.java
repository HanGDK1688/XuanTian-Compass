package com.xuantian.compass;

/**

* 玄天罗盘 V1.4

* 二十四山方位计算工具

* 

* 方位角规则：

* 0° = 正北子山中心

* 角度顺时针增加

* 每山占15°

* 每个方位以中心角度前后7.5°为界
  */
  public class TwentyFourMountains {
  
  /**
  
  * 二十四山排列顺序
  * 
  * 从正北子山开始，顺时针排列。
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
  
  /**
  
  * 每一山所占角度
    */
    private static final double ANGLE_PER_MOUNTAIN = 15.0;
  
  /**
  
  * 方位边界偏移量
    */
    private static final double HALF_ANGLE = 7.5;
  
  /**
  
  * 将方位角转换为0至360度范围
    */
    public static double normalizeAngle(double angle) {
    
    angle = angle % 360.0;
    
    if (angle < 0) {
    angle += 360.0;
    }
    
    return angle;
    }
  
  /**
  
  * 根据方位角获取二十四山名称
  
  * 
  
  * @param angle 方位角，单位为度
  
  * @return 二十四山名称
    */
    public static String getMountainName(double angle) {
    
    int index = getMountainIndex(angle);
    
    return MOUNTAINS[index];
    }
  
  /**
  
  * 根据方位角获取二十四山序号
  
  * 
  
  * 序号从0开始：
  
  * 0 = 子
  
  * 1 = 癸
  
  * ...
  
  * 23 = 壬
    */
    public static int getMountainIndex(double angle) {
    
    double normalized = normalizeAngle(angle);
    
    int index = (int) Math.floor(
    (normalized + HALF_ANGLE) / ANGLE_PER_MOUNTAIN
    );
    
    return index % MOUNTAINS.length;
    }
  
  /**
  
  * 获取二十四山完整列表
    */
    public static String[] getAllMountains() {
    
    return MOUNTAINS.clone();
    }
  
  /**
  
  * 获取指定二十四山的中心角度
    */
    public static double getMountainCenterAngle(int index) {
    
    if (index < 0 || index >= MOUNTAINS.length) {
    throw new IllegalArgumentException("二十四山序号必须在0至23之间");
    }
    
    return index * ANGLE_PER_MOUNTAIN;
    }
  
  /**
  
  * 获取当前方位所属二十四山的中心角度
    */
    public static double getMountainCenterAngle(double angle) {
    
    return getMountainCenterAngle(getMountainIndex(angle));
    }
  
  /**
  
  * 获取二十四山数量
    */
    public static int getMountainCount() {
    
    return MOUNTAINS.length;
    }
  }
