package com.mymiki.mimyki;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PurchaseActivity extends AppCompatActivity {

    private EditText etCardNumber, etExpiryDate, etCVC;
    private Button btnConfirmPurchase;
    private DatabaseHelper dbHelper;
    private int userId = -1; //default = -1: Không tìm thấy
    private EditText etName, etUsername;
    private boolean isPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCVC = findViewById(R.id.etCVC);
        btnConfirmPurchase = findViewById(R.id.btnConfirmPurchase);
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);

        dbHelper = new DatabaseHelper(this);

        userId = getUserIdFromSharedPreferences(); //Lấy id bằng SharedPreferences
        if (userId != -1) {
            loadUserData(userId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }

        // Get userId from SharedPreferences
        SharedPreferences sharedPref = this.getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", MODE_PRIVATE);
        userId = sharedPref.getInt("user_id", -1);

        btnConfirmPurchase.setOnClickListener(v -> {
            String cardNumber = etCardNumber.getText().toString().trim();
            String expiryDate = etExpiryDate.getText().toString().trim();
            String cvc = etCVC.getText().toString().trim();

            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvc.isEmpty()) {
                Toast.makeText(PurchaseActivity.this, "Vui lòng nhập đầy đủ thông tin thanh toán!", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            dbHelper.updateUser(userId, name, username, true);
            Toast.makeText(PurchaseActivity.this, "Bạn đã mua Premium thành công!", Toast.LENGTH_SHORT).show();

            // Return to AccountFragment
            Intent intent = new Intent(PurchaseActivity.this, MainActivity.class);
            intent.putExtra("fragment", "AccountFragment");
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData(int userId) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USER,
                null,
                DatabaseHelper.COLUMN_USER_ID_TABLE + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            isPremium = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_PREMIUM)) == 1;
            etName.setText(name);
            etUsername.setText(username);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}