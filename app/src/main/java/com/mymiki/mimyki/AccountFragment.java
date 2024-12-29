package com.mymiki.mimyki;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AccountFragment extends Fragment {

    private EditText etName, etUsername;
    private Switch swPremium;
    private TextView tvPremiumStatus;
    private Button btnUpdate, btnDelete, btnLogout, btnBuyPremium;
    private DatabaseHelper dbHelper;
    private int userId = -1; //default = -1: Không tìm thấy
    private boolean isPremium;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        etName = view.findViewById(R.id.etName);
        etUsername = view.findViewById(R.id.etUsername);
        swPremium = view.findViewById(R.id.swPremium);
        tvPremiumStatus = view.findViewById(R.id.tvPremiumStatus);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnBuyPremium = view.findViewById(R.id.btnBuyPremium);

        dbHelper = new DatabaseHelper(requireContext());

        userId = getUserIdFromSharedPreferences(); //Lấy id bằng SharedPreferences

        // Lấy userId từ Activity (giả sử được truyền qua Intent hoặc SharedPreferences)
//        Bundle arguments = getArguments(); // Kết quả debug: argument đang bị rỗng nên không gán user_id được
//        if (arguments != null) {
//            userId = arguments.getInt("USER_ID", -1);
//        }
//
        if (userId != -1) {
            loadUserData(userId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }

        Button btnEditNotificationTime = view.findViewById(R.id.btnEditNotificationTime);
        btnEditNotificationTime.setOnClickListener(v -> showEditNotificationTimeDialog());


        // Cập nhật thông tin người dùng
        btnUpdate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            if (name.isEmpty() || username.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.updateUser(userId, name, username, isPremium);
            Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
        });

        // Xóa tài khoản
        btnDelete.setOnClickListener(v -> {
            dbHelper.deleteUser(userId);
            Toast.makeText(getContext(), "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
            // Đưa người dùng về trang đăng nhập
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        // Mua Premium
        btnBuyPremium.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PurchaseActivity.class);
            startActivity(intent);
        });

        swPremium.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPremium = isChecked; // Update state
            updatePremiumUI(); // Reflect changes in UI
        });
    }

    // Tải dữ liệu người dùng từ SQLite
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
            swPremium.setChecked(isPremium);
            updatePremiumUI();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    // Cập nhật giao diện Premium
    private void updatePremiumUI() {
        if (isPremium) {
            tvPremiumStatus.setVisibility(View.VISIBLE);
            btnBuyPremium.setVisibility(View.GONE);
        } else {
            tvPremiumStatus.setVisibility(View.GONE);
            btnBuyPremium.setVisibility(View.VISIBLE);
        }
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }


    private void showEditNotificationTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chỉnh sửa thời gian thông báo");

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số phút");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            int offsetMinutes = Integer.parseInt(input.getText().toString());
            dbHelper.updateUserNotificationOffset(userId, offsetMinutes); // Lưu vào DB
            SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
            sharedPref.edit().putInt("notification_offset", offsetMinutes).apply();
            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
