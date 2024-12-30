package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class WeekFragment extends Fragment {

    private TextView weekTitle;
    private Button btnPreviousWeek, btnNextWeek, btnSelectDate;
    private LinearLayout weekDaysLayout;
    private LocalDate currentWeekStart;
    private ListView eventListView;
    private FloatingActionButton addEventButton;
    private DatabaseHelper dbHelper;
    private String selectedDate;
    private int user_id = -1;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_week, container, false);

        // Khởi tạo các view
        weekTitle = rootView.findViewById(R.id.week_title);
        btnPreviousWeek = rootView.findViewById(R.id.btn_previous_week);
        btnNextWeek = rootView.findViewById(R.id.btn_next_week);
        btnSelectDate = rootView.findViewById(R.id.btn_select_date);
        weekDaysLayout = rootView.findViewById(R.id.week_days_layout);
        eventListView = rootView.findViewById(R.id.eventListView);
        addEventButton = rootView.findViewById(R.id.addEventButton);
        dbHelper = new DatabaseHelper(getContext());
        user_id = getUserIdFromSharedPreferences();

        if (user_id == -1) {
            Toast.makeText(getContext(), "Không xác định được người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        btnPreviousWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekTitle();
            updateWeekDays();
            loadEventsForSelectedDate();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekTitle();
            updateWeekDays();
            loadEventsForSelectedDate();
        });

        LocalDate currentDate = LocalDate.now();
        currentWeekStart = currentDate.with(ChronoField.DAY_OF_WEEK, 1); // Đặt lại ngày bắt đầu tuần
        updateWeekTitle();
        updateWeekDays();

        addEventButton.setOnClickListener(v -> openEventDialog(null));
        return rootView;
    }

    @SuppressLint("NewApi")
    private void showDatePickerDialog() {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue() - 1;
        int dayOfMonth = currentDate.getDayOfMonth();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth);
                    currentWeekStart = selectedDate.with(ChronoField.DAY_OF_WEEK, 1); // Đặt lại ngày bắt đầu tuần
                    updateWeekTitle();
                    updateWeekDays();
                    this.selectedDate = selectedDate.toString();
                    loadEventsForSelectedDate();
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    @SuppressLint("NewApi")
    private void updateWeekTitle() {
        // Lấy số tuần trong tháng
        int dayOfMonth = currentWeekStart.getDayOfMonth();
        int weekOfMonth = (dayOfMonth - 1) / 7 + 1;

        String monthName = currentWeekStart.getMonth().name();
        int year = currentWeekStart.getYear();

        weekTitle.setText("Tuần: " + weekOfMonth + " | " + monthName + " " + year);
    }

    @SuppressLint("NewApi")
    private void updateWeekDays() {
        weekDaysLayout.removeAllViews();
        final Button[] previouslySelectedButton = {null};
        for (int i = 0; i < 7; i++) {
            @SuppressLint({"NewApi", "LocalSuppress"}) LocalDate day = currentWeekStart.plusDays(i);
            Button dayButton = new Button(getContext());

            // Lấy thứ trong tuần
            String dayOfWeek = day.getDayOfWeek().name().substring(0, 3);

            //GradientDrawable chung
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setStroke(2, Color.BLACK); // Viền màu đen
            drawable.setCornerRadius(16); // Bo tròn góc

            // Hiển thị thứ và ngày trong tháng
            String buttonText = dayOfWeek + " " + day.getDayOfMonth();
            dayButton.setText(buttonText);
            dayButton.setText(buttonText);
            dayButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            dayButton.setBackground(drawable);

            if (hasEventOnDate(day)) {
                dayButton.setPaintFlags(dayButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                dayButton.setTextColor(Color.parseColor("#fc0377"));
            }
            // Nếu đây là ngày đã chọn
            if (day.toString().equals(selectedDate)) {
                dayButton.setBackgroundColor(Color.parseColor("#E6E6FA")); // Lavender
                previouslySelectedButton[0] = dayButton; // Save the selected button
            }

            dayButton.setOnClickListener(v -> {
                selectedDate = day.toString(); // Lưu lại ngày đã chọn
                loadEventsForSelectedDate(); // Tải sự kiện cho ngày đã chọn

                if (previouslySelectedButton[0] != null) {
                    previouslySelectedButton[0].setBackgroundColor(Color.WHITE);
                    previouslySelectedButton[0].setBackground(drawable);
                }

                dayButton.setBackgroundColor(Color.parseColor("#E6E6FA"));
                previouslySelectedButton[0] = dayButton;
            });
            weekDaysLayout.addView(dayButton);
        }
    }

    // Kiểm tra xem ngày có sự kiện hay không
    private boolean hasEventOnDate(LocalDate date) {
        Cursor cursor = dbHelper.getEventsByDate(date.toString(), user_id);
        return cursor != null && cursor.getCount() > 0;
    }

    private void loadEventsForSelectedDate() {
        if (selectedDate == null) return;

        // Truy vấn dữ liệu sự kiện từ cơ sở dữ liệu
        Cursor cursor = dbHelper.getEventsByDate(selectedDate, user_id);
        if (cursor != null && cursor.getCount() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            final List<Integer> eventIds = new ArrayList<>();

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String eventName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME));
                @SuppressLint("Range") int eventId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID));
                @SuppressLint("Range") String eventDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATETIME)); // Giả sử cột ngày của sự kiện có tên COLUMN_EVENT_DATE
                eventIds.add(eventId);

                String eventDisplayText = "• " + eventName + " - " + eventDate;
                adapter.add(eventDisplayText);
            }

            cursor.close();
            eventListView.setAdapter(adapter); // Cập nhật adapter cho ListView

            // Gọi notifyDataSetChanged để làm mới danh sách
            adapter.notifyDataSetChanged();

            eventListView.setVisibility(View.VISIBLE); // Đảm bảo listView được hiển thị
            TextView noEventsTextView = getView().findViewById(R.id.noEventsTextView);
            if (noEventsTextView != null) {
                noEventsTextView.setVisibility(View.GONE);
            }
        } else {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            emptyAdapter.add("Không có sự kiện cho ngày này");
            eventListView.setAdapter(emptyAdapter);
            eventListView.setVisibility(View.GONE); // Ẩn ListView nếu không có sự kiện
            TextView noEventsTextView = getView().findViewById(R.id.noEventsTextView);
            noEventsTextView.setVisibility(View.VISIBLE);
        }
    }
    @SuppressLint("Range")
    private void openEventDialog(@Nullable Integer eventId) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event, null);
        EditText eventNameInput = dialogView.findViewById(R.id.eventNameInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        Button timeButton = dialogView.findViewById(R.id.timeInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

        Cursor categoryCursor = dbHelper.getAllCategories(user_id);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        while (categoryCursor.moveToNext()) {
            categoryAdapter.add(categoryCursor.getString(categoryCursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME)));
        }
        categoryCursor.close();
        categorySpinner.setAdapter(categoryAdapter);

        timeButton.setOnClickListener(v -> {
            int hour = 12;
            int minute = 0;

            if (eventId != null) {
                Cursor eventCursor = dbHelper.getEventById(eventId, user_id);
                if (eventCursor != null && eventCursor.moveToFirst()) {
                    String[] datetimeParts = eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_DATETIME)).split(" ");
                    if (datetimeParts.length == 2) {
                        String[] timeParts = datetimeParts[1].split(":");
                        if (timeParts.length == 2) {
                            hour = Integer.parseInt(timeParts[0]);
                            minute = Integer.parseInt(timeParts[1]);
                        }
                    }
                    eventCursor.close();
                }
            }

            new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute1);
                timeButton.setText(time);
            }, hour, minute, true).show();
        });

        if (eventId == null) {
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            timeButton.setText(currentTime);
        } else {
            Cursor eventCursor = dbHelper.getEventById(eventId, user_id);
            if (eventCursor != null && eventCursor.moveToFirst()) {
                eventNameInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME)));
                descriptionInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
                locationInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION)));
                String[] datetimeParts = eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_DATETIME)).split(" ");
                if (datetimeParts.length == 2) {
                    timeButton.setText(datetimeParts[1]);
                }
                eventCursor.close();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setPositiveButton(eventId == null ? "Thêm" : "Sửa", (dialog, which) -> {
                    String eventName = eventNameInput.getText().toString();
                    String description = descriptionInput.getText().toString();
                    String time = timeButton.getText().toString();
                    String location = locationInput.getText().toString();
                    String categoryName = categorySpinner.getSelectedItem().toString();

                    String datetime = selectedDate + " " + time;

                    // Nếu không có eventId, tức là đang thêm sự kiện mới
                    if (eventId == null) {
                        Cursor cursor = dbHelper.getAllCategories(user_id);
                        int cateId = 1;
                        while (cursor.moveToNext()) {
                            if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME)).equals(categoryName)) {
                                cateId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_ID));
                                break;
                            }
                        }
                        cursor.close();

                        dbHelper.addEvent(eventName, description, datetime, location, false, 1, cateId, user_id); // Thêm sự kiện mới
                    } else { // Cập nhật sự kiện
                        int taskCurrentPriority = dbHelper.getTaskPriority(eventName, user_id);
                        int taskCurrentCategory = dbHelper.getTaskCategory(eventName, user_id);

                        dbHelper.updateEvent(eventId, eventName, description, datetime, location, false, taskCurrentCategory, taskCurrentPriority);
                    }
                    loadEventsForSelectedDate();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa", (dialog, which) -> {
                    if (eventId != null) {
                        dbHelper.deleteEvent(eventId);
                        loadEventsForSelectedDate();
                    }
                });
        builder.create().show();
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}




