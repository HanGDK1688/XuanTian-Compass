package com.xuantian.compass;

/**
 * 住宅资料数据模型
 *
 * 用于记录住宅基本信息、首次入住时间和坐向信息。
 */
public class ResidenceData {

    public int id;

    public String name;

    // 住宅建造年份
    public int buildingYear;

    // 首次实际入住年份
    public int moveInYear;

    // 首次实际入住日期，格式：yyyy-MM-dd
    // 例如：2024-02-10
    public String moveInDate = "";

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
        this.moveInDate = "";
        this.floor = floor;
        this.area = area;
    }
}
