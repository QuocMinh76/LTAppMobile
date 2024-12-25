package com.mymiki.mimyki;

import static java.security.AccessController.getContext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "schedule.db";

    // Tên bảng và cột
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date"; // Lưu ngày (yyyy-MM-dd)
    public static final String COLUMN_EVENT = "event";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_EVENT + " TEXT)";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // Thêm sự kiện
    public void addEvent(String date, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_EVENT, event);
        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    // Lấy sự kiện theo ngày
    public Cursor getEventsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, new String[]{COLUMN_EVENT},
                COLUMN_DATE + "=?", new String[]{date},
                null, null, null);
    }

    public void updateEvent(String date, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT, event);
        db.update(TABLE_EVENTS, values, COLUMN_DATE + " = ?", new String[]{date});
        db.close();
    }

    public void deleteEvent(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_DATE + " = ?", new String[]{date});
        db.close();
    }

    // Lấy tất cả sự kiện
    public Cursor getAllEvents() {

        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, null, null, null, null, null);

    }

}

