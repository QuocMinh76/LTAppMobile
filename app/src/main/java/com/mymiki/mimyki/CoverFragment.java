package com.mymiki.mimyki;

import android.content.Context;
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

    private DatabaseHelper dbHelper;
    private int currentUserId = 1; // Giả định ID người dùng hiện tại là 1

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dbHelper = new DatabaseHelper(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cover, container, false);

        Button btnBuyPremium = view.findViewById(R.id.btn_buy_premium);
        btnBuyPremium.setOnClickListener(v -> {
            // Cập nhật thông tin Premium trong SQLite
            dbHelper.updateUser(currentUserId, "Tên người dùng", "username", true);
            Toast.makeText(getContext(), "Nâng cấp Premium thành công!", Toast.LENGTH_SHORT).show();

            // Loại bỏ CoverFragment
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });

        return view;
    }
}
