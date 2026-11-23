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

* 支持多套住宅的新增、读取、修改和删除。
  */
  public class ResidenceStorage extends SQLiteOpenHelper {
  
  private static final String DATABASE_NAME =
  "xuantian_residences.db";
  
  private static final int DATABASE_VERSION = 1;
  
  private static final String TABLE_NAME =
  "residences";
  
  private static final String COL_ID =
  "id";
  
  private static final String COL_NAME =
  "name";
  
  private static final String COL_YEAR =
  "building_year";
  
  private static final String COL_FLOOR =
  "floor";
  
  private static final String COL_AREA =
  "area";
  
  public ResidenceStorage(Context context) {
  super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  /**
  
  * 创建住宅资料表
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
    
    String sql =
    "CREATE TABLE " + TABLE_NAME + " (" +
    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COL_NAME + " TEXT NOT NULL, " +
    COL_YEAR + " INTEGER NOT NULL, " +
    COL_FLOOR + " TEXT NOT NULL, " +
    COL_AREA + " REAL NOT NULL" +
    ")";
    
    db.execSQL(sql);
    }
  
  /**
  
  * 数据库升级
    */
    @Override
    public void onUpgrade(
    SQLiteDatabase db,
    int oldVersion,
    int newVersion) {
    
    // 暂时保留原有数据结构。
    // 后续增加字段时再编写升级方案。
    }
  
  /**
  
  * 新增住宅
  
  * 
  
  * 返回新住宅的数据库编号。
  
  * 如果保存失败，返回 -1。
    */
    public long insert(ResidenceData data) {
    
    SQLiteDatabase db = getWritableDatabase();
    
    ContentValues values = new ContentValues();
    
    values.put(COL_NAME, data.name);
    values.put(COL_YEAR, data.buildingYear);
    values.put(COL_FLOOR, data.floor);
    values.put(COL_AREA, data.area);
    
    return db.insert(TABLE_NAME, null, values);
    }
  
  /**
  
  * 修改住宅资料
  
  * 
  
  * 返回成功修改的记录数量。
    */
    public int update(ResidenceData data) {
    
    SQLiteDatabase db = getWritableDatabase();
    
    ContentValues values = new ContentValues();
    
    values.put(COL_NAME, data.name);
    values.put(COL_YEAR, data.buildingYear);
    values.put(COL_FLOOR, data.floor);
    values.put(COL_AREA, data.area);
    
    return db.update(
    TABLE_NAME,
    values,
    COL_ID + "=?",
    new String[]{String.valueOf(data.id)}
    );
    }
  
  /**
  
  * 删除指定住宅
    */
    public int delete(int id) {
    
    SQLiteDatabase db = getWritableDatabase();
    
    return db.delete(
    TABLE_NAME,
    COL_ID + "=?",
    new String[]{String.valueOf(id)}
    );
    }
  
  /**
  
  * 读取全部住宅资料
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
    COL_ID + " DESC"
    );
    
    try {
    while (cursor.moveToNext()) {
    
         ResidenceData data = new ResidenceData();

     data.id = cursor.getInt(
             cursor.getColumnIndexOrThrow(COL_ID));

     data.name = cursor.getString(
             cursor.getColumnIndexOrThrow(COL_NAME));

     data.buildingYear = cursor.getInt(
             cursor.getColumnIndexOrThrow(COL_YEAR));

     data.floor = cursor.getString(
             cursor.getColumnIndexOrThrow(COL_FLOOR));

     data.area = cursor.getDouble(
             cursor.getColumnIndexOrThrow(COL_AREA));

     list.add(data);
 }
    
    } finally {
    cursor.close();
    }
    
    return list;
    }
  
  /**
  
  * 根据编号读取一套住宅
    */
    public ResidenceData getById(int id) {
    
    SQLiteDatabase db = getReadableDatabase();
    
    Cursor cursor = db.query(
    TABLE_NAME,
    null,
    COL_ID + "=?",
    new String[]{String.valueOf(id)},
    null,
    null,
    null
    );
    
    try {
    if (cursor.moveToFirst()) {
    
         ResidenceData data = new ResidenceData();

     data.id = cursor.getInt(
             cursor.getColumnIndexOrThrow(COL_ID));

     data.name = cursor.getString(
             cursor.getColumnIndexOrThrow(COL_NAME));

     data.buildingYear = cursor.getInt(
             cursor.getColumnIndexOrThrow(COL_YEAR));

     data.floor = cursor.getString(
             cursor.getColumnIndexOrThrow(COL_FLOOR));

     data.area = cursor.getDouble(
             cursor.getColumnIndexOrThrow(COL_AREA));

     return data;
 }
    
    } finally {
    cursor.close();
    }
    
    return null;
    }
  }
