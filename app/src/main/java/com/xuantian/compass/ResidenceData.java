package com.xuantian.compass;

/**
 * 住宅资料数据模型
 *
 * 用于记录每一套住宅的基本信息。
 */
public class ResidenceData {

    // 数据库记录编号
    public int id;

    // 住宅名称
    public String name;

    // 建造年份
    public int buildingYear;

    // 所在楼层
    public String floor;

    // 住宅面积（平方米）
    public double area;

    // 空构造方法
    public ResidenceData() {
    }

    // 完整构造方法
    public ResidenceData(
            int id,
            String name,
            int buildingYear,
            String floor,
            double area) {

        this.id = id;
        this.name = name;
        this.buildingYear = buildingYear;
        this.floor = floor;
        this.area = area;
    }
}
