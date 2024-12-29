package com.mymiki.mimyki;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CoverFragment extends Fragment {

    String username;
    String name;
    boolean isPremium;


    private DatabaseHelper dbHelper;
    private int currentUserId = -1; // Giả định ID người dùng hiện tại là 1

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbHelper = new DatabaseHelper(context);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cover, container, false);

        currentUserId = getUserIdFromSharedPreferences();
        loadUserData(currentUserId);
        Button btnBuyPremium = view.findViewById(R.id.btn_buy_premium);
        btnBuyPremium.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PurchaseActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData(int userId) {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USER,
                null,
                DatabaseHelper.COLUMN_USER_ID_TABLE + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            isPremium = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_PREMIUM)) == 1;
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}
