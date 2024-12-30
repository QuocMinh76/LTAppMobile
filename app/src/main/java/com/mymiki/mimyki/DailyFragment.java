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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import android.widget.HorizontalScrollView;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Map;

public class DailyFragment extends Fragment {

    private TextView weekTitle;
    private Button btnPreviousWeek, btnNextWeek, btnSelectDate;
    private LinearLayout weekDaysLayout;
    private LocalDate currentWeekStart;
    private RecyclerView eventRecyclerView;
    private FloatingActionButton addEventButton;
    private DatabaseHelper dbHelper;
    private String selectedDate;
    private ListView hourListView;
    private int user_id = -1;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_daily, container, false);

        // Khởi tạo các view
        weekTitle = rootView.findViewById(R.id.week_title);
        btnPreviousWeek = rootView.findViewById(R.id.btn_previous_week);
        btnNextWeek = rootView.findViewById(R.id.btn_next_week);
        btnSelectDate = rootView.findViewById(R.id.btn_select_date); // Nút chọn ngày
        weekDaysLayout = rootView.findViewById(R.id.week_days_layout);
        eventRecyclerView = rootView.findViewById(R.id.eventRecyclerView);
        addEventButton = rootView.findViewById(R.id.addEventButton);
        dbHelper = new DatabaseHelper(getContext());
        user_id = getUserIdFromSharedPreferences();

        // Kiểm tra người dùng đã đăng nhập chưa
        if (user_id == -1) {
            Toast.makeText(getContext(), "Không xác định được người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        // Thiết lập sự kiện cho nút chọn ngày
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Các sự kiện cho nút tuần trước, tuần sau
        btnPreviousWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekTitle();
            updateWeekDays();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekTitle();
            updateWeekDays();
        });

        // Lấy ngày hiện tại và thiết lập tuần hiện tại
        LocalDate currentDate = LocalDate.now();
        currentWeekStart = currentDate.with(ChronoField.DAY_OF_WEEK, 1); // Đặt lại ngày bắt đầu tuần
        updateWeekTitle();
        updateWeekDays();

        // Tạo danh sách giờ
        ArrayList<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            @SuppressLint("DefaultLocale") String hour = String.format("%02d:00", i); // Ví dụ: 00:00, 01:00, ..., 23:00
            hours.add(hour);
        }

        eventRecyclerView = rootView.findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addEventButton.setOnClickListener(v -> openEventDialog(null)); // Mở dialog thêm sự kiện

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
                    loadEventsForSelectedDate(); // Tải các sự kiện cho ngày đã chọn
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
                dayButton.setPaintFlags(dayButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // Gạch chân
                dayButton.setTextColor(Color.parseColor("#fc0377")); // Màu tím cho ngày có sự kiện
            }
            // Nếu đây là ngày đã chọn
            if (day.toString().equals(selectedDate)) {
                dayButton.setBackgroundColor(Color.parseColor("#E6E6FA")); // Lavender
                previouslySelectedButton[0] = dayButton;
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

    public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
        private List<String> eventList;

        public EventAdapter(List<String> eventList) {
            this.eventList = eventList;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_horizontal, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            String eventDetails = eventList.get(position);
            String[] details = eventDetails.split(" - ");
            holder.eventTime.setText(details[0]);
            holder.eventName.setText(details[1]);
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        public class EventViewHolder extends RecyclerView.ViewHolder {
            TextView eventTime, eventName;

            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                eventTime = itemView.findViewById(R.id.event_time);
                eventName = itemView.findViewById(R.id.event_name);
            }
        }
    }
    @SuppressLint("NewApi")
    private void loadEventsForSelectedDate() {
        if (selectedDate == null) return;

        Cursor cursor = dbHelper.getEventsByDate(selectedDate, user_id);
        List<List<String>> eventsAtHours = new ArrayList<>();

        // Tạo danh sách sự kiện cho từng giờ
        for (int i = 0; i < 24; i++) {
            List<String> eventsForHour = new ArrayList<>();
            String hour = String.format("%02d:00", i);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range")
                    String eventDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATETIME));
                    String eventHour = eventDate.split(" ")[1].substring(0, 2) + ":00"; // Lấy giờ

                    if (eventHour.equals(hour)) {
                        @SuppressLint("Range")
                        String eventName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME));
                        String eventTime = eventDate.split(" ")[1].substring(0, 5); // Lấy "HH:MM"
                        eventsForHour.add("• " + eventTime + " - " + eventName);
                    }
                } while (cursor.moveToNext());
            }

            // Sắp xếp các sự kiện theo thứ tự tăng dần
            eventsForHour.sort(String::compareTo);
            eventsAtHours.add(eventsForHour.isEmpty() ? List.of(" ") : eventsForHour);
        }

        if (cursor != null) {
            cursor.close();
        }
        // Cập nhật RecyclerView
        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter(eventsAtHours);
        eventRecyclerView.setAdapter(adapter);
    }

    public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {
        private final List<List<String>> eventsAtHours;

        public EventRecyclerViewAdapter(List<List<String>> eventsAtHours) {
            this.eventsAtHours = eventsAtHours;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_horizontal, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            List<String> events = eventsAtHours.get(position);
            String hour = String.format("%02d:00", position);

            // Hiển thị giờ
            holder.hourTextView.setText(hour);

            // Hiển thị danh sách sự kiện
            StringBuilder eventsString = new StringBuilder();
            for (String event : events) {
                eventsString.append(event).append("\n");
            }
            holder.eventsTextView.setText(eventsString.toString().trim());
        }

        @Override
        public int getItemCount() {
            return eventsAtHours.size();
        }

        public class EventViewHolder extends RecyclerView.ViewHolder {
            TextView hourTextView;
            TextView eventsTextView;

            public EventViewHolder(@NonNull View itemView) {
                super(itemView);
                hourTextView = itemView.findViewById(R.id.event_time);
                eventsTextView = itemView.findViewById(R.id.event_name);
            }
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
                    } else {
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





