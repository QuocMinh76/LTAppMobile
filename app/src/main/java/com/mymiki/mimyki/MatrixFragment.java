package com.mymiki.mimyki;

import static com.mymiki.mimyki.DatabaseHelper.COLUMN_IS_PREMIUM;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MatrixFragment extends Fragment {

    FloatingActionButton fabAddTask;
    ListView listQuadrant1, listQuadrant2, listQuadrant3, listQuadrant4;
    ArrayList<String> quadrant1Tasks, quadrant2Tasks, quadrant3Tasks, quadrant4Tasks;
    ArrayAdapter<String> quadrant1Adapter, quadrant2Adapter, quadrant3Adapter, quadrant4Adapter;
    private int default_cate_id = -1;
    private int user_id = -1;

    private DatabaseHelper dbHelper;
    private DatabaseHelper databaseHelper;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        dbHelper = new DatabaseHelper(getContext());
        user_id = getUserIdFromSharedPreferences(); // Đảm bảo lấy user_id
        databaseHelper = new DatabaseHelper(getContext());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting the location
                getCurrentLocation(cityName -> {
                    // Handle the location result
                });
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(OnSuccessListener<String> listener) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String cityName = addresses.get(0).getLocality();
                            listener.onSuccess(cityName);
                        } else {
                            listener.onSuccess(null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onSuccess(null);
                    }
                } else {
                    listener.onSuccess(null);
                }
            });
        } else {
            requestLocationPermission();
        }
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

        default_cate_id = dbHelper.getDefaultCategoryId(user_id);

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
        EditText edtTaskDescription = dialogView.findViewById(R.id.edt_task_description);
        EditText edtTaskLocation = dialogView.findViewById(R.id.edt_task_location);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        Button btnSelectDateTime = dialogView.findViewById(R.id.btn_select_datetime);

        getCurrentLocation(cityName -> {
            if (cityName != null) {
                edtTaskLocation.setText(cityName);
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.matrix_strings, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(spinnerAdapter);

        Cursor categoryCursor = dbHelper.getCategoriesByUserId(user_id);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        List<Integer> categoryIds = new ArrayList<>();
        while (categoryCursor.moveToNext()) {
            String categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
            int categoryId = categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
            categoryAdapter.add(categoryName);
            categoryIds.add(categoryId);
        }
        categoryCursor.close();
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

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
                    String taskDescription = edtTaskDescription.getText().toString().trim();
                    String taskLocation = edtTaskLocation.getText().toString().trim();
                    int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
                    int selectedCategoryId = categoryIds.get(selectedCategoryPosition);
                    int priority = spinnerPriority.getSelectedItemPosition();
                    if (!taskContent.isEmpty() && !selectedDateTime[0].isEmpty()) {
                        addTaskToQuadrant(taskContent, taskDescription, taskLocation, selectedCategoryId, priority + 1, selectedDateTime[0]);
                        Toast.makeText(getContext(), "Đã thêm công việc", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Vui lòng nhập nội dung công việc và chọn ngày giờ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .create()
                .show();
    }
    private void addTaskToQuadrant(String taskContent, String taskDescription, String taskLocation, int category, int priority, String dateTime) {
        // Thêm vào SQLite
        dbHelper.addEvent(taskContent, taskDescription, dateTime, taskLocation, false, priority, category, user_id); //Để tạm cate_id = 1
        // Lấy thời gian thông báo từ SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        int offsetMinutes = sharedPref.getInt("notification_offset", 1);

        // Lên lịch thông báo
        NotificationScheduler.scheduleNotification(getContext(), taskContent, dateTime, offsetMinutes);
        // Thêm vào danh sách hiển thị
        switch (priority) {
            case 1:
                quadrant1Tasks.add(taskContent);
                quadrant1Adapter.notifyDataSetChanged();
                break;
            case 2:
                quadrant2Tasks.add(taskContent);
                quadrant2Adapter.notifyDataSetChanged();
                break;
            case 3:
                quadrant3Tasks.add(taskContent);
                quadrant3Adapter.notifyDataSetChanged();
                break;
            case 4:
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
        if (listView == listQuadrant1) return 1;
        if (listView == listQuadrant2) return 2;
        if (listView == listQuadrant3) return 3;
        if (listView == listQuadrant4) return 4;
        return -1;
    }

    private void showEditTaskDialog(ArrayList<String> taskList, ArrayAdapter<String> adapter, int position) {
        String currentTask = taskList.get(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.matrix_dialog_edit_task, null);

        EditText edtTaskContent = dialogView.findViewById(R.id.edt_task_content);
        EditText edtTaskDescription = dialogView.findViewById(R.id.edt_task_description);
        EditText edtTaskLocation = dialogView.findViewById(R.id.edt_task_location);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);
        Button btnSelectDateTime = dialogView.findViewById(R.id.btn_select_datetime);
        Button btnOpenLocation = dialogView.findViewById(R.id.btn_open_location);

        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Set current values
        edtTaskContent.setText(currentTask);
        edtTaskDescription.setText(dbHelper.getTaskDescription(currentTask, user_id));
        edtTaskLocation.setText(dbHelper.getTaskLocation(currentTask, user_id));
        btnSelectDateTime.setText(dbHelper.getTaskDateTime(currentTask, user_id));

        // Load categories into spinner
        Cursor categoryCursor = dbHelper.getCategoriesByUserId(user_id);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        List<Integer> categoryIds = new ArrayList<>();
        while (categoryCursor.moveToNext()) {
            String categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
            int categoryId = categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
            categoryAdapter.add(categoryName);
            categoryIds.add(categoryId);
        }
        categoryCursor.close();
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setSelection(categoryIds.indexOf(dbHelper.getTaskCategory(currentTask, user_id)));

        // Load priorities into spinner
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.matrix_strings, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        int taskCurrentPriority = dbHelper.getTaskPriority(currentTask, user_id);
        spinnerPriority.setSelection(taskCurrentPriority - 1);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnSave.setOnClickListener(v -> {
            String updatedTask = edtTaskContent.getText().toString().trim();
            String updatedDescription = edtTaskDescription.getText().toString().trim();
            String updatedLocation = edtTaskLocation.getText().toString().trim();
            int updatedCategory = categoryIds.get(spinnerCategory.getSelectedItemPosition());
            int updatedPriority = spinnerPriority.getSelectedItemPosition();
            String updatedDateTime = btnSelectDateTime.getText().toString().trim();

            if (!updatedTask.isEmpty()) {
                taskList.set(position, updatedTask);
                adapter.notifyDataSetChanged();
                dbHelper.updateEventContent(currentTask, updatedTask, updatedDescription, updatedLocation, updatedCategory, updatedPriority + 1, updatedDateTime, user_id);
                dialog.dismiss();
                Toast.makeText(getContext(), "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            NotificationScheduler.cancelNotification(getContext(), currentTask);
            taskList.remove(position);
            adapter.notifyDataSetChanged();
            dbHelper.deleteEvent(currentTask, user_id);
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã xóa công việc", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOpenLocation.setOnClickListener(v -> openLocationInGoogleMaps(currentTask, user_id));

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
                    case 1:
                        quadrant1Tasks.add(taskContent);
                        break;
                    case 2:
                        quadrant2Tasks.add(taskContent);
                        break;
                    case 3:
                        quadrant3Tasks.add(taskContent);
                        break;
                    case 4:
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

    private void openLocationInGoogleMaps(String taskName, int userId) {
        // Retrieve the location from the database
        String location = databaseHelper.getTaskLocation(taskName, userId);

        if (location != null && !location.isEmpty()) {
            // Create an Intent to open Google Maps
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if there is an app to handle the Intent
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Handle the case where Google Maps is not installed
                Toast.makeText(getContext(), "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where the location is not valid
            Toast.makeText(getContext(), "Location is not available", Toast.LENGTH_SHORT).show();
        }
    }

}
