package com.xuantian.compass;

/**
 * 住宅资料数据模型
 *
 * 用于记录每一套住宅的基本信息和坐向信息。
 */
public class ResidenceData {

    public int id;

    public String name;

    public int buildingYear;

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
        this.floor = floor;
        this.area = area;
    }
}
