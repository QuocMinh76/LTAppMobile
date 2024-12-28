package com.mymiki.mimyki;

import static com.mymiki.mimyki.DatabaseHelper.COLUMN_IS_PREMIUM;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import java.util.Calendar;
import java.util.Locale;

public class MatrixFragment extends Fragment {

    FloatingActionButton fabAddTask;
    ListView listQuadrant1, listQuadrant2, listQuadrant3, listQuadrant4;
    ArrayList<String> quadrant1Tasks, quadrant2Tasks, quadrant3Tasks, quadrant4Tasks;
    ArrayAdapter<String> quadrant1Adapter, quadrant2Adapter, quadrant3Adapter, quadrant4Adapter;

    private int user_id = -1;

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
        user_id = getUserIdFromSharedPreferences(); // Đảm bảo lấy user_id

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        user_id = getUserIdFromSharedPreferences();

        // Kiểm tra quyền Premium
        if (!isUserPremium()) {
            setupFunctionality();
            showCoverFragment();
        } else {
            setupFunctionality();
        }

        return rootView;
    }

    private boolean isUserPremium() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USER,
                null,
                DatabaseHelper.COLUMN_USER_ID_TABLE + " = ?",
                new String[]{String.valueOf(user_id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            boolean isPremium = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_PREMIUM)) == 1;
            cursor.close();
            return isPremium;
        }
        return false;
    }

    private void showCoverFragment() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new CoverFragment())
                .commit();
    }

    private void setupFunctionality() {
        loadEvents(); // Tải dữ liệu từ SQLite

        setupDragAndDrop(listQuadrant1, quadrant1Tasks, quadrant1Adapter);
        setupDragAndDrop(listQuadrant2, quadrant2Tasks, quadrant2Adapter);
        setupDragAndDrop(listQuadrant3, quadrant3Tasks, quadrant3Adapter);
        setupDragAndDrop(listQuadrant4, quadrant4Tasks, quadrant4Adapter);

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        listQuadrant1.setOnItemClickListener((parent, view, position, id) -> showEditTaskDialog(quadrant1Tasks, quadrant1Adapter, position));
        listQuadrant2.setOnItemClickListener((parent, view, position, id) -> showEditTaskDialog(quadrant2Tasks, quadrant2Adapter, position));
        listQuadrant3.setOnItemClickListener((parent, view, position, id) -> showEditTaskDialog(quadrant3Tasks, quadrant3Adapter, position));
        listQuadrant4.setOnItemClickListener((parent, view, position, id) -> showEditTaskDialog(quadrant4Tasks, quadrant4Adapter, position));
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.matrix_dialog_add_task, null);

        EditText edtTaskContent = dialogView.findViewById(R.id.edt_task_content);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);
        Button btnSelectDateTime = dialogView.findViewById(R.id.btn_select_datetime);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.matrix_strings, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(spinnerAdapter);

        final String[] selectedDateTime = {""};

        btnSelectDateTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                (timeView, hourOfDay, minute) -> {
                                    selectedDateTime[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                                            year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                                    btnSelectDateTime.setText(selectedDateTime[0]);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm công việc mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String taskContent = edtTaskContent.getText().toString().trim();
                    int priority = spinnerPriority.getSelectedItemPosition();
                    if (!taskContent.isEmpty() && !selectedDateTime[0].isEmpty()) {
                        addTaskToQuadrant(taskContent, priority, selectedDateTime[0]);
                        Toast.makeText(getContext(), "Đã thêm công việc", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Vui lòng nhập nội dung công việc và chọn ngày giờ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .create()
                .show();
    }

    private void addTaskToQuadrant(String taskContent, int priority, String dateTime) {
        // Thêm vào SQLite
        dbHelper.addEvent(taskContent, "", dateTime, "", false, priority, 1, user_id); //Để tạm cate_id = 1

        // Thêm vào danh sách hiển thị
        switch (priority) {
            case 0:
                quadrant1Tasks.add(taskContent);
                quadrant1Adapter.notifyDataSetChanged();
                break;
            case 1:
                quadrant2Tasks.add(taskContent);
                quadrant2Adapter.notifyDataSetChanged();
                break;
            case 2:
                quadrant3Tasks.add(taskContent);
                quadrant3Adapter.notifyDataSetChanged();
                break;
            case 3:
                quadrant4Tasks.add(taskContent);
                quadrant4Adapter.notifyDataSetChanged();
                break;
        }
    }

    private void setupDragAndDrop(ListView listView, ArrayList<String> taskList, ArrayAdapter<String> adapter) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String task = taskList.get(position);
            ClipData data = ClipData.newPlainText("task", task);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, task, 0);
            } else {
                view.startDrag(data, shadowBuilder, task, 0);
            }

            taskList.remove(position);
            adapter.notifyDataSetChanged();
            return true;
        });

        listView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(Color.LTGRAY);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return true;
                case DragEvent.ACTION_DROP:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    String droppedTask = event.getClipData().getItemAt(0).getText().toString();
                    taskList.add(droppedTask);
                    adapter.notifyDataSetChanged();

                    // Update the priority in the database
                    int newPriority = getQuadrantPriority(listView);
                    dbHelper.updateEventPriority(droppedTask, newPriority, user_id);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    return event.getResult();
                default:
                    return false;
            }
        });
    }

    private int getQuadrantPriority(View listView) {
        if (listView == listQuadrant1) return 0;
        if (listView == listQuadrant2) return 1;
        if (listView == listQuadrant3) return 2;
        if (listView == listQuadrant4) return 3;
        return -1;
    }

    private void showEditTaskDialog(ArrayList<String> taskList, ArrayAdapter<String> adapter, int position) {
        String currentTask = taskList.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.matrix_dialog_edit_task, null);

        EditText edtTaskContent = dialogView.findViewById(R.id.edt_task_content);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        edtTaskContent.setText(currentTask);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnSave.setOnClickListener(v -> {
            String updatedTask = edtTaskContent.getText().toString().trim();
            if (!updatedTask.isEmpty()) {
                taskList.set(position, updatedTask);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
                Toast.makeText(getContext(), "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            taskList.remove(position);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã xóa công việc", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }

    private void loadEvents() {
        Cursor cursor = dbHelper.getEventsByUser(user_id); // Tải công việc theo user_id
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String taskContent = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME));
                int priorityTag = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY_TAG));

                switch (priorityTag) {
                    case 0:
                        quadrant1Tasks.add(taskContent);
                        break;
                    case 1:
                        quadrant2Tasks.add(taskContent);
                        break;
                    case 2:
                        quadrant3Tasks.add(taskContent);
                        break;
                    case 3:
                        quadrant4Tasks.add(taskContent);
                        break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        quadrant1Adapter.notifyDataSetChanged();
        quadrant2Adapter.notifyDataSetChanged();
        quadrant3Adapter.notifyDataSetChanged();
        quadrant4Adapter.notifyDataSetChanged();
    }

}
