package com.mymiki.mimyki;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatrixFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatrixFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MatrixFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MatrixFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MatrixFragment newInstance(String param1, String param2) {
        MatrixFragment fragment = new MatrixFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    FloatingActionButton fabAddTask;
    ListView listQuadrant1, listQuadrant2, listQuadrant3, listQuadrant4;
    ArrayList<String> quadrant1Tasks, quadrant2Tasks, quadrant3Tasks, quadrant4Tasks;
    ArrayAdapter<String> quadrant1Adapter, quadrant2Adapter, quadrant3Adapter, quadrant4Adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_matrix, container, false);

        fabAddTask = rootView.findViewById(R.id.fab_add_task);
        listQuadrant1 = rootView.findViewById(R.id.list_quadrant1);
        listQuadrant2 = rootView.findViewById(R.id.list_quadrant2);
        listQuadrant3 = rootView.findViewById(R.id.list_quadrant3);
        listQuadrant4 = rootView.findViewById(R.id.list_quadrant4);

        quadrant1Tasks = new ArrayList<>();
        quadrant2Tasks = new ArrayList<>();
        quadrant3Tasks = new ArrayList<>();
        quadrant4Tasks = new ArrayList<>();

        quadrant1Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant1Tasks);
        quadrant2Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant2Tasks);
        quadrant3Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant3Tasks);
        quadrant4Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant4Tasks);

        listQuadrant1.setAdapter(quadrant1Adapter);
        listQuadrant2.setAdapter(quadrant2Adapter);
        listQuadrant3.setAdapter(quadrant3Adapter);
        listQuadrant4.setAdapter(quadrant4Adapter);


        // Thiết lập Drag and Drop cho các ListView
        setupDragAndDrop(listQuadrant1, quadrant1Tasks, quadrant1Adapter);
        setupDragAndDrop(listQuadrant2, quadrant2Tasks, quadrant2Adapter);
        setupDragAndDrop(listQuadrant3, quadrant3Tasks, quadrant3Adapter);
        setupDragAndDrop(listQuadrant4, quadrant4Tasks, quadrant4Adapter);

        listQuadrant1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditTaskDialog(quadrant1Tasks, quadrant1Adapter, position);
            }
        });

        listQuadrant2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditTaskDialog(quadrant2Tasks, quadrant2Adapter, position);
            }
        });

        listQuadrant3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditTaskDialog(quadrant3Tasks, quadrant3Adapter, position);
            }
        });

        listQuadrant4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditTaskDialog(quadrant4Tasks, quadrant4Adapter, position);
            }
        });


        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        return rootView;
    }

    private void showAddTaskDialog() {
        // Tạo View cho dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.matrix_dialog_add_task, null);

        // Lấy tham chiếu đến EditText
        EditText edtTaskContent = dialogView.findViewById(R.id.edt_task_content);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);

        // Cài đặt adapter cho Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.matrix_strings, // Danh sách trạng thái công việc trong strings.xml
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(spinnerAdapter);

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm công việc mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String taskContent = edtTaskContent.getText().toString().trim();
                        int priority = spinnerPriority.getSelectedItemPosition();

                        if (!taskContent.isEmpty()) {
                            // Thêm công việc vào danh sách dựa trên mức độ quan trọng
                            switch (priority) {
                                case 0: // Khẩn cấp
                                    quadrant1Tasks.add(taskContent);
                                    quadrant1Adapter.notifyDataSetChanged();
                                    break;
                                case 1: // Quan trọng
                                    quadrant2Tasks.add(taskContent);
                                    quadrant2Adapter.notifyDataSetChanged();
                                    break;
                                case 2: // Bình thường
                                    quadrant3Tasks.add(taskContent);
                                    quadrant3Adapter.notifyDataSetChanged();
                                    break;
                                case 3: // Khác
                                    quadrant4Tasks.add(taskContent);
                                    quadrant4Adapter.notifyDataSetChanged();
                                    break;
                            }
                            Toast.makeText(getContext(), "Đã thêm công việc", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Vui lòng nhập nội dung công việc", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null);

        builder.create().show();
    }

    private void setupDragAndDrop(ListView listView, ArrayList<String> taskList, ArrayAdapter<String> adapter) {
        // Bắt đầu Drag khi nhấn giữ item
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String task = taskList.get(position);

                // Lưu nội dung công việc dưới dạng clip data
                ClipData data = ClipData.newPlainText("task", task);

                // Tạo Drag Shadow
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                // Bắt đầu Drag
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, task, 0);
                }

                // Xóa công việc khỏi danh sách hiện tại
                taskList.remove(position);
                adapter.notifyDataSetChanged();

                return true;
            }
        });

        // Xử lý Drop vào danh sách khác
        listView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Thay đổi giao diện khi item kéo vào danh sách
                        listView.setBackgroundColor(Color.TRANSPARENT); // Đổi nền sang màu xám nhạt
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        // Khôi phục giao diện khi item rời danh sách
                        listView.setBackgroundColor(Color.TRANSPARENT); // Trả lại màu trong suốt
                        return true;
                    case DragEvent.ACTION_DROP:
                        // Lấy nội dung được kéo
                        String droppedTask = event.getClipData().getItemAt(0).getText().toString();

                        // Thêm vào danh sách hiện tại
                        taskList.add(droppedTask);
                        adapter.notifyDataSetChanged();

                        // Khôi phục giao diện danh sách sau khi thả
                        listView.setBackgroundColor(Color.TRANSPARENT);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Đảm bảo giao diện trở về trạng thái mặc định nếu kéo bị hủy
                        listView.setBackgroundColor(Color.TRANSPARENT);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void showEditTaskDialog(ArrayList<String> taskList, ArrayAdapter<String> adapter, int position) {
        // Lấy nội dung công việc hiện tại
        String currentTask = taskList.get(position);

        // Tạo View cho dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.matrix_dialog_edit_task, null);

        // Tham chiếu các thành phần trong layout
        EditText edtTaskContent = dialogView.findViewById(R.id.edt_task_content);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Hiển thị nội dung công việc hiện tại
        edtTaskContent.setText(currentTask);

        // Tạo AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false) // Người dùng phải nhấn nút Hủy mới đóng được
                .create();

        // Xử lý nút Lưu
        btnSave.setOnClickListener(v -> {
            String updatedTask = edtTaskContent.getText().toString().trim();
            if (!updatedTask.isEmpty()) {
                taskList.set(position, updatedTask); // Cập nhật nội dung công việc
                adapter.notifyDataSetChanged(); // Cập nhật giao diện
                dialog.dismiss(); // Đóng dialog
                Toast.makeText(getContext(), "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút Xóa
        btnDelete.setOnClickListener(v -> {
            taskList.remove(position); // Xóa công việc khỏi danh sách
            adapter.notifyDataSetChanged(); // Cập nhật giao diện
            dialog.dismiss(); // Đóng dialog
            Toast.makeText(getContext(), "Đã xóa công việc", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show(); // Hiển thị dialog
    }

}