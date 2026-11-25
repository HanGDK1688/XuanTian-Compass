package com.xuantian.compass;

/**
 * 住宅资料数据模型
 *
 * 用于记录住宅基本信息、坐向和首次入住年份。
 */
public class ResidenceData {

    public int id;
    public String name;

    // 建造年份
    public int buildingYear;

    // 首次入住年份，用于计算住宅元运
    public int moveInYear;

    public String floor;
    public double area;

    // 住宅朝向角度，-1表示尚未设置
    public double facingAngle = -1;

    // 住宅朝向对应的二十四山名称
    public String facingMountain = "";

    public ResidenceData() {
    }

    public ResidenceData(
            int id,
            String name,
            int buildingYear,
            String floor,
            double area) {

        this.id = id;
        this.name = name;
        this.buildingYear = buildingYear;
        this.moveInYear = buildingYear;
        this.floor = floor;
        this.area = area;
    }
}
