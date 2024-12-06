//package com.mymiki.mimyki;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class PriorityTaskActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_priority_task);
//    }
//}

package com.mymiki.mimyki;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PriorityTaskActivity extends AppCompatActivity {

    Button btnAdd;
    ListView listQuadrant1, listQuadrant2, listQuadrant3, listQuadrant4;
    ArrayList<String> quadrant1Tasks, quadrant2Tasks, quadrant3Tasks, quadrant4Tasks;
    ArrayAdapter<String> quadrant1Adapter, quadrant2Adapter, quadrant3Adapter, quadrant4Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_priority_task);

        btnAdd = findViewById(R.id.btn_add);
        listQuadrant1 = findViewById(R.id.list_quadrant1);
        listQuadrant2 = findViewById(R.id.list_quadrant2);
        listQuadrant3 = findViewById(R.id.list_quadrant3);
        listQuadrant4 = findViewById(R.id.list_quadrant4);

        quadrant1Tasks = new ArrayList<>();
        quadrant2Tasks = new ArrayList<>();
        quadrant3Tasks = new ArrayList<>();
        quadrant4Tasks = new ArrayList<>();

        quadrant1Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quadrant1Tasks);
        quadrant2Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quadrant2Tasks);
        quadrant3Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quadrant3Tasks);
        quadrant4Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quadrant4Tasks);

        listQuadrant1.setAdapter(quadrant1Adapter);
        listQuadrant2.setAdapter(quadrant2Adapter);
        listQuadrant3.setAdapter(quadrant3Adapter);
        listQuadrant4.setAdapter(quadrant4Adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    private void showAddTaskDialog() {
        CharSequence[] items = {"Khẩn Cấp", "Quan Trọng", "Bình Thường", "Khác"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn trạng thái công việc")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String task = "Công việc mới";

                        switch (which) {
                            case 0: // Khẩn Cấp
                                quadrant1Tasks.add(task);
                                quadrant1Adapter.notifyDataSetChanged();
                                break;
                            case 1: // Quan Trọng
                                quadrant2Tasks.add(task);
                                quadrant2Adapter.notifyDataSetChanged();
                                break;
                            case 2: // Bình Thường
                                quadrant3Tasks.add(task);
                                quadrant3Adapter.notifyDataSetChanged();
                                break;
                            case 3: // Khác
                                quadrant4Tasks.add(task);
                                quadrant4Adapter.notifyDataSetChanged();
                                break;
                        }
                        Toast.makeText(PriorityTaskActivity.this, "Đã thêm công việc", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }
}

