package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 10; // Tăng phiên bản khi thay đổi cấu trúc DB
    private static final String DATABASE_NAME = "todo.db";

    // Bảng events
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_EVENT_ID = "id";
    public static final String COLUMN_EVENT_NAME = "event";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_EVENT_DONE = "is_done";
    public static final String COLUMN_PRIORITY_TAG = "priority_tag";
    public static final String COLUMN_CATE_ID = "cate_id";
    public static final String COLUMN_USER_ID = "user_id";

    // Bảng category
    public static final String TABLE_CATEGORY = "category";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_USER_ID = "user_id";

    // Bảng priority
    public static final String TABLE_PRIORITY = "priority";
    public static final String COLUMN_PRIORITY_ID = "id";
    public static final String COLUMN_PRIORITY_NAME = "name";

    // Bảng user
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID_TABLE = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_PREMIUM = "is_premium";
    public static final String COLUMN_NOTIFICATION_OFFSET = "notification_offset";

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
                + COLUMN_IS_PREMIUM + " INTEGER DEFAULT 0, "
                + COLUMN_NOTIFICATION_OFFSET + " INTEGER DEFAULT 1)";
        db.execSQL(CREATE_USER_TABLE);

        // Tạo bảng category
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " ("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_NAME + " TEXT, "
                + COLUMN_CATEGORY_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID_TABLE + "))";
        db.execSQL(CREATE_CATEGORY_TABLE);

        // Tạo bảng priority
        String CREATE_PRIORITY_TABLE = "CREATE TABLE " + TABLE_PRIORITY + " ("
                + COLUMN_PRIORITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PRIORITY_NAME + " TEXT)";
        db.execSQL(CREATE_PRIORITY_TABLE);

        // Insert default data into the Priority table
        String insertOtherPriority = "INSERT INTO " + TABLE_PRIORITY + " (" + COLUMN_PRIORITY_NAME + ") VALUES ('Khác')";
        String insertLowPriority = "INSERT INTO " + TABLE_PRIORITY + " (" + COLUMN_PRIORITY_NAME + ") VALUES ('Bình thường')";
        String insertMediumPriority = "INSERT INTO " + TABLE_PRIORITY + " (" + COLUMN_PRIORITY_NAME + ") VALUES ('Quan trọng')";
        String insertHighPriority = "INSERT INTO " + TABLE_PRIORITY + " (" + COLUMN_PRIORITY_NAME + ") VALUES ('Khẩn cấp')";

        db.execSQL(insertHighPriority);
        db.execSQL(insertMediumPriority);
        db.execSQL(insertLowPriority);
        db.execSQL(insertOtherPriority);

        // Tạo bảng events
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EVENT_NAME + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_EVENT_DONE + " INTEGER DEFAULT 0, "
                + COLUMN_PRIORITY_TAG + " INTERGER, "
                + COLUMN_CATE_ID + " INTEGER, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_PRIORITY_TAG + ") REFERENCES " + TABLE_PRIORITY + "(" + COLUMN_PRIORITY_ID + "), "
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
        db.execSQL("DROP TABLE IF EXISTS priority");
        db.close();
    }

    //tao
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu có thay đổi
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIORITY);
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
    public long addUser(String name, String username, String password, boolean isPremium) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Băm mật khẩu trước khi lưu
        String hashedPassword = hashPassword(password);

        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_IS_PREMIUM, isPremium ? 1 : 0);
        // Insert user and return the user ID
        long userId = db.insert(TABLE_USER, null, values);
        db.close();
        return userId;
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
    public void addEvent(String event, String description, String datetime, String location, boolean isDone, int priorityTag, int cateId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, event);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_EVENT_DONE, isDone);
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
    public void updateEvent(int eventId, String event, String description, String datetime, String location, boolean isDone, int categoryId, int priorityTag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, event);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_EVENT_DONE, isDone);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_CATE_ID, categoryId);
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

    // Create: Add a new priority
    public void addPriority(String priorityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIORITY_NAME, priorityName);

        db.insert(TABLE_PRIORITY, null, values);
        db.close();
    }

    // Read: Get all priorities
    public Cursor getAllPriorities() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRIORITY, null, null, null, null, null, null);
    }

    // Update: Update a priority
    public int updatePriority(int priorityId, String newPriorityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIORITY_NAME, newPriorityName);

        int rowsAffected = db.update(TABLE_PRIORITY, values, COLUMN_PRIORITY_ID + " = ?",
                new String[]{String.valueOf(priorityId)});
        db.close();
        return rowsAffected; // Number of rows affected
    }

    // Delete: Delete a priority
    public int deletePriority(int priorityId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_PRIORITY, COLUMN_PRIORITY_ID + " = ?",
                new String[]{String.valueOf(priorityId)});
        db.close();
        return rowsDeleted; // Number of rows deleted
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

    public Cursor getEventById(int eventId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM events WHERE id = ? AND user_id = ?", new String[]{String.valueOf(eventId), String.valueOf(userId)});
    }

    public Cursor getEventsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_EVENTS, // Tên bảng
                null, // Lấy tất cả các cột
                COLUMN_USER_ID + " = ?", // Điều kiện WHERE
                new String[]{String.valueOf(userId)}, // Giá trị của điều kiện WHERE
                null, // GROUP BY
                null, // HAVING
                null // ORDER BY
        );
    }

    public Cursor getEventsByDate(String date, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM events WHERE date(datetime) = ? AND user_id = ?", new String[]{date, String.valueOf(userId)});
    }

    public Cursor getAllCategories(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM category WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    // Lấy danh sách category theo user_id
    public Cursor getCategoriesByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CATEGORY_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.query(TABLE_CATEGORY, null, selection, selectionArgs, null, null, null);
    }

    // Lấy danh sách sự kiện theo category_id
    public Cursor getEventsByCategoryId(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CATE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(categoryId)};
        return db.query(TABLE_EVENTS, null, selection, selectionArgs, null, null, null);
    }

    public void updateTaskDoneStatus(int taskId, boolean isDone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_DONE, isDone ? 1 : 0);
        db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void updateEventPriority(String eventName, int newPriority, int user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRIORITY_TAG, newPriority);
        db.update(TABLE_EVENTS, values, COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{eventName, String.valueOf(user_id)});
    }

    public void updateEventContent(String oldTask, String newTask, String description, String location, int category, int priority, String dateTime, int userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, newTask);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_CATE_ID, category);
        values.put(COLUMN_PRIORITY_TAG, priority);
        values.put(COLUMN_DATETIME, dateTime);

        String selection = COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {oldTask, String.valueOf(userId)};

        getWritableDatabase().update(TABLE_EVENTS, values, selection, selectionArgs);
    }

    public void deleteEvent(String content, int userId) {
        getWritableDatabase().delete(TABLE_EVENTS, COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?", new String[]{content, String.valueOf(userId)});
    }

    public void updateUserNotificationOffset(int userId, int offsetMinutes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("notification_offset", offsetMinutes);
        db.update(TABLE_USER, values, COLUMN_USER_ID_TABLE + " = ?", new String[]{String.valueOf(userId)});
    }

    public String getPriorityNameById(int priorityId) {
        String priorityName = null; // Variable to store the result
        SQLiteDatabase db = this.getReadableDatabase(); // Open the database in read mode
        Cursor cursor = null;

        try {
            // Query to select the priority name for the given ID
            cursor = db.query(
                    TABLE_PRIORITY,               // Table name
                    new String[]{COLUMN_PRIORITY_NAME}, // Columns to retrieve
                    COLUMN_PRIORITY_ID + "=?",     // WHERE clause
                    new String[]{String.valueOf(priorityId)}, // WHERE clause arguments
                    null,                          // GROUP BY
                    null,                          // HAVING
                    null                           // ORDER BY
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the priority name from the first row of the result
                priorityName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor to avoid memory leaks
            }
            db.close(); // Close the database
        }

        return priorityName; // Return the retrieved priority name
    }

    public int getDefaultCategoryId(int userId) {
        int categoryId = -1; // Default value to indicate no category found
        SQLiteDatabase db = this.getReadableDatabase(); // Open the database in read mode
        Cursor cursor = null;

        try {
            // Query to get the first category of the user
            cursor = db.query(
                    "category",                       // Table name
                    new String[]{"id"},               // Columns to retrieve
                    "user = ?",                       // WHERE clause
                    new String[]{String.valueOf(userId)}, // WHERE clause arguments
                    null,                             // GROUP BY
                    null,                             // HAVING
                    "id ASC",                         // ORDER BY (first category created)
                    "1"                               // LIMIT (only 1 row)
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the category ID from the first row of the result
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor to avoid memory leaks
            }
            db.close(); // Close the database
        }

        return categoryId; // Return the retrieved category ID
    }

    public int getPriorityIdByEventId(int eventId) {
        int priorityId = -1; // Default value to indicate no priority found
        SQLiteDatabase db = this.getReadableDatabase(); // Open the database in read mode
        Cursor cursor = null;

        try {
            // Query to retrieve the priority ID of the event
            cursor = db.query(
                    TABLE_EVENTS,                     // Table name
                    new String[]{COLUMN_PRIORITY_TAG}, // Column to retrieve
                    COLUMN_EVENT_ID + " = ?",         // WHERE clause
                    new String[]{String.valueOf(eventId)}, // WHERE clause argument
                    null,                             // GROUP BY
                    null,                             // HAVING
                    null                              // ORDER BY
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the priority ID from the first row of the result
                priorityId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_TAG));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor to avoid memory leaks
            }
            db.close(); // Close the database
        }

        return priorityId; // Return the retrieved priority ID
    }

    public void updateTaskCategory(int taskId, int newCategoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATE_ID, newCategoryId);
        db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public boolean hasEventsInCategory(int categoryId) {
        // Query to check if there are events in the category
        Cursor cursor = getEventsByCategoryId(categoryId);
        boolean hasEvents = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return hasEvents;
    }

    public static void cancelNotification(Context context, String eventName) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, eventName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent); // Hủy thông báo
        }
    }

    public String getEventTimeById(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EVENTS,
                new String[]{COLUMN_DATETIME},
                COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String eventTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME));
            cursor.close();
            return eventTime;
        }
        return null;
    }

    public String getTaskDescription(String taskName, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_DESCRIPTION},
                COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{taskName, String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            cursor.close();
            return description;
        }
        return null;
    }

    public String getTaskLocation(String taskName, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_LOCATION},
                COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{taskName, String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
            cursor.close();
            return location;
        }
        return null;
    }

    public int getTaskCategory(String taskName, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_CATE_ID},
                COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{taskName, String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int category = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATE_ID));
            cursor.close();
            return category;
        }
        return -1;
    }

    public int getTaskPriority(String taskName, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_PRIORITY_TAG},
                COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{taskName, String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_TAG));
            cursor.close();
            return priority;
        }
        return -1;
    }

    public String getTaskDateTime(String taskName, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_EVENTS, new String[]{COLUMN_DATETIME},
                COLUMN_EVENT_NAME + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{taskName, String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME));
            cursor.close();
            return dateTime;
        }
        return null;
    }

    // Method to get the total number of events for a user
    public int getTotalEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int totalEvents = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalEvents = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return totalEvents;
    }

    // Method to get the total number of finished events for a user
    public int getTotalFinishedEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_EVENT_DONE + " = 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int totalFinishedEvents = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalFinishedEvents = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return totalFinishedEvents;
    }

    // Method to get the total number of unfinished events for a user
    public int getTotalUnfinishedEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_EVENT_DONE + " = 0";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int totalUnfinishedEvents = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalUnfinishedEvents = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return totalUnfinishedEvents;
    }

    public String getUserNameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the query to get the user's name by ID
        String query = "SELECT " + COLUMN_USER_NAME + " FROM " + TABLE_USER +
                " WHERE " + COLUMN_USER_ID_TABLE + " = ?";

        // Prepare the cursor to execute the query
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String name = null;

        if (cursor != null && cursor.moveToFirst()) {
            // Get the user's name from the cursor
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
        }

        // Close the cursor and database
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return name;
    }
}


