package com.xuantian.compass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 住宅资料本地数据库
 *
 * 数据库升级到版本2：
 * 增加住宅朝向角度和二十四山字段。
 *
 * 通过ALTER TABLE升级，保留原有住宅资料。
 */
public class ResidenceStorage extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "xuantian_residences.db";

    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "residences";

    public ResidenceStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "name TEXT NOT NULL,"
                        + "building_year INTEGER NOT NULL,"
                        + "floor TEXT NOT NULL,"
                        + "area REAL NOT NULL,"
                        + "facing_angle REAL NOT NULL DEFAULT -1,"
                        + "facing_mountain TEXT NOT NULL DEFAULT ''"
                        + ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_NAME
                            + " ADD COLUMN facing_angle REAL"
                            + " NOT NULL DEFAULT -1"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_NAME
                            + " ADD COLUMN facing_mountain TEXT"
                            + " NOT NULL DEFAULT ''"
            );
        }
    }

    /**
     * 新增住宅资料
     *
     * @return 新记录ID，失败返回-1
     */
    public long insert(ResidenceData data) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = toContentValues(data);

        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * 修改住宅资料
     *
     * @return 成功修改的记录数量
     */
    public int update(ResidenceData data) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = toContentValues(data);

        return db.update(
                TABLE_NAME,
                values,
                "id = ?",
                new String[]{String.valueOf(data.id)}
        );
    }

    /**
     * 删除住宅资料
     *
     * @return 删除的记录数量
     */
    public int delete(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_NAME,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    /**
     * 获取全部住宅档案
     */
    public List<ResidenceData> getAll() {

        List<ResidenceData> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );

        try {
            while (cursor.moveToNext()) {
                list.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    /**
     * 根据ID读取住宅档案
     */
    public ResidenceData getById(int id) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * 将住宅资料转换为数据库字段
     */
    private ContentValues toContentValues(ResidenceData data) {

        ContentValues values = new ContentValues();

        values.put("name", data.name);
        values.put("building_year", data.buildingYear);
        values.put("floor", data.floor);
        values.put("area", data.area);
        values.put("facing_angle", data.facingAngle);
        values.put("facing_mountain", data.facingMountain);

        return values;
    }

    /**
     * 将数据库记录转换为住宅资料
     */
    private ResidenceData fromCursor(Cursor cursor) {

        ResidenceData data = new ResidenceData();

        data.id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id"));

        data.name = cursor.getString(
                cursor.getColumnIndexOrThrow("name"));

        data.buildingYear = cursor.getInt(
                cursor.getColumnIndexOrThrow("building_year"));

        data.floor = cursor.getString(
                cursor.getColumnIndexOrThrow("floor"));

        data.area = cursor.getDouble(
                cursor.getColumnIndexOrThrow("area"));

        data.facingAngle = cursor.getDouble(
                cursor.getColumnIndexOrThrow("facing_angle"));

        data.facingMountain = cursor.getString(
                cursor.getColumnIndexOrThrow("facing_mountain"));

        return data;
    }
}
