package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 9; // Tăng phiên bản khi thay đổi cấu trúc DB
    private static final String DATABASE_NAME = "todo.db";

    // Bảng events
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_EVENT_ID = "id";
    public static final String COLUMN_EVENT_NAME = "event";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_PRIORITY_TAG = "priority_tag";
    public static final String COLUMN_CATE_ID = "cate_id";
    public static final String COLUMN_USER_ID = "user_id";

    // Bảng category
    public static final String TABLE_CATEGORY = "category";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_USER_ID = "user_id";

    // Bảng user
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID_TABLE = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_PREMIUM = "is_premium";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng user
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COLUMN_USER_ID_TABLE + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_IS_PREMIUM + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USER_TABLE);

        // Tạo bảng category
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " ("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_NAME + " TEXT, "
                + COLUMN_CATEGORY_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID_TABLE + "))";
        db.execSQL(CREATE_CATEGORY_TABLE);

        // Tạo bảng events
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EVENT_NAME + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_PRIORITY_TAG + " TEXT DEFAULT 'Khác', "
                + COLUMN_CATE_ID + " INTEGER, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_CATE_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COLUMN_CATEGORY_ID + "), "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID_TABLE + "))";
        db.execSQL(CREATE_EVENTS_TABLE);

    }

//    //xoa database
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS user");
        db.close();
    }

    //tao
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu có thay đổi
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    //xác thực khi đăng nhập (pass data và pass nhập vào)
    public int authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn mật khẩu đã băm từ cơ sở dữ liệu
        String query = "SELECT " + COLUMN_USER_ID_TABLE + ", " + COLUMN_PASSWORD + " FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(0); // Lấy user_id
            String storedHashedPassword = cursor.getString(1); // Mật khẩu đã băm
            cursor.close();

            // Băm mật khẩu người dùng nhập vào
            String hashedPassword = hashPassword(password);

            // So sánh mật khẩu đã băm
            if (storedHashedPassword.equals(hashedPassword)) {
                return userId; // Trả về user_id nếu đăng nhập thành công
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return -1; // Trả về -1 nếu không tìm thấy người dùng hoặc mật khẩu không khớp
    }

    // Hàm xử lý đăng nhập
    public int login(String username, String password) {
        return authenticateUser(username, password); // Trả về user_id hoặc -1
    }

    // Thêm người dùng
    public Boolean addUser(String name, String username, String password, boolean isPremium) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Băm mật khẩu trước khi lưu
        String hashedPassword = hashPassword(password);

        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_IS_PREMIUM, isPremium ? 1 : 0);
        db.insert(TABLE_USER, null, values);
        db.close();
        return null;
    }

    // Lấy danh sách tất cả người dùng
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER, null, null, null, null, null, null);
    }

    // Sửa thông tin người dùng
    public void updateUser(int userId, String name, String username, boolean isPremium) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_IS_PREMIUM, isPremium ? 1 : 0);
        db.update(TABLE_USER, values, COLUMN_USER_ID_TABLE + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    // Xóa người dùng
    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, COLUMN_USER_ID_TABLE + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    // Thêm category
    public void addCategory(String name, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_USER_ID, userId);
        db.insert(TABLE_CATEGORY, null, values);
        db.close();
    }

    // Lấy danh sách tất cả category
    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORY, null, null, null, null, null, null);
    }

    // Sửa category
    public void updateCategory(int categoryId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        db.update(TABLE_CATEGORY, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
    }

    // Xóa category
    public void deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORY, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
    }

    // Thêm sự kiện
    public void addEvent(String event, String description, String datetime, String location, String priorityTag, int cateId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, event);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_PRIORITY_TAG, priorityTag);
        values.put(COLUMN_CATE_ID, cateId);
        values.put(COLUMN_USER_ID, userId);
        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    // Lấy danh sách tất cả sự kiện
    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, null, null, null, null, null);
    }

    // Sửa sự kiện
    public void updateEvent(int eventId, String event, String description, String datetime, String location, String priorityTag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, event);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_PRIORITY_TAG, priorityTag);
        db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
    }

    // Xóa sự kiện
    public void deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
    }

    public Cursor getEventsByDate(String selectedDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_EVENTS, // Tên bảng sự kiện
                null,
                "DATE(" + COLUMN_DATETIME + ") = ?",
                new String[]{selectedDate},
                null, null, null
        );
    }

    public Cursor getCategoriesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_CATEGORY, // Tên bảng
                null, // Lấy tất cả các cột
                COLUMN_USER_ID + " = ?", // Điều kiện WHERE
                new String[]{String.valueOf(userId)}, // Giá trị của điều kiện WHERE
                null, // GROUP BY
                null, // HAVING
                null // ORDER BY
        );
    }

    public Cursor getEventById(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_EVENTS, // Tên bảng
                null, // Lấy tất cả các cột
                COLUMN_EVENT_ID + " = ?", // Điều kiện WHERE
                new String[]{String.valueOf(eventId)}, // Giá trị của điều kiện WHERE
                null, // GROUP BY
                null, // HAVING
                null // ORDER BY
        );
    }

    public int getCategoryIdByName(String categoryName, int currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CATEGORY, // Tên bảng
                new String[]{COLUMN_CATEGORY_ID}, // Chỉ lấy cột categoryId
                COLUMN_CATEGORY_NAME + " = ?", // Điều kiện WHERE
                new String[]{categoryName}, // Giá trị của điều kiện WHERE
                null, // GROUP BY
                null, // HAVING
                null // ORDER BY
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
            cursor.close();
            return categoryId;
        }

        // Trả về giá trị mặc định nếu không tìm thấy
        return -1;
    }

}


