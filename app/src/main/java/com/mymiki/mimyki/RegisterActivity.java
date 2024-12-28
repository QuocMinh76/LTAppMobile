package com.mymiki.mimyki;


import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_ID;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_USER_ID;
import static com.mymiki.mimyki.DatabaseHelper.TABLE_CATEGORY;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etUsername, etPassword;

    private Button btnRegister, btnLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ View
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister_r);

        btnLogin = findViewById(R.id.btnLogin);

        dbHelper = new DatabaseHelper(this);

        // Xử lý khi nhấn nút "Đăng ký"
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the user and get the userId
            long userId = dbHelper.addUser(name, username, password, false);

            if (userId != -1) {
                // Add default category
                dbHelper.addCategory("Chào mừng!", (int) userId);

                // Get the category ID of the newly added category (assuming a method to get the last inserted ID)
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT " + COLUMN_CATEGORY_ID + " FROM " + TABLE_CATEGORY +
                                " WHERE " + COLUMN_CATEGORY_USER_ID + " = ? ORDER BY " + COLUMN_CATEGORY_ID + " DESC LIMIT 1",
                        new String[]{String.valueOf(userId)});

                int categoryId = -1;
                if (cursor.moveToFirst()) {
                    categoryId = cursor.getInt(0);
                }
                cursor.close();

                // Add default event if categoryId is valid
                if (categoryId != -1) {
                    dbHelper.addEvent(
                            "Hãy bắt đầu....",
                            "...thêm các hoạt động mới cho bản thân!",
                            "2025-01-01 00:00:00",
                            "Nhà của bạn",
                            false,
                            1, // Assuming 'Khác' priority is ID 1
                            categoryId,
                            (int) userId
                    );
                }

                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // Redirect to LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý khi nhấn nút "Đăng nhập"

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
