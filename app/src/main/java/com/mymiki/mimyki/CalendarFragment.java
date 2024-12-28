package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private ListView eventListView;
    private FloatingActionButton addEventButton;
    private DatabaseHelper dbHelper;
    private String selectedDate;

    private int user_id = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        eventListView = view.findViewById(R.id.eventListView);
        addEventButton = view.findViewById(R.id.addEventButton);
        dbHelper = new DatabaseHelper(getContext());
        user_id = getUserIdFromSharedPreferences();

        if (user_id == -1) {
            Toast.makeText(getContext(), "Không xác định được người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return view;
        }

        selectedDate = getCurrentDate();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadEventsForSelectedDate();
        });

        addEventButton.setOnClickListener(v -> openEventDialog(null));

        return view;
    }

    private void loadEventsForSelectedDate() {
        Cursor cursor = dbHelper.getEventsByDate(selectedDate, user_id);
        if (cursor != null && cursor.getCount() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            final List<Integer> eventIds = new ArrayList<>();

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String eventName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME));
                @SuppressLint("Range") int eventId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID));
                eventIds.add(eventId);
                adapter.add(eventName);
            }

            cursor.close();
            eventListView.setAdapter(adapter);

            eventListView.setOnItemClickListener((parent, view, position, id) -> {
                int eventId = eventIds.get(position);
                openEventDialog(eventId);
            });

            eventListView.setVisibility(View.VISIBLE);
            TextView noEventsTextView = getView().findViewById(R.id.noEventsTextView);
            if (noEventsTextView != null) {
                noEventsTextView.setVisibility(View.GONE);
            }
        } else {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            emptyAdapter.add("Không có sự kiện cho ngày này");
            eventListView.setAdapter(emptyAdapter);

            eventListView.setVisibility(View.GONE);
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

                        dbHelper.addEvent(eventName, description, datetime, location, false, 1, cateId, user_id); // Thêm userId thực tế
                    } else {
                        dbHelper.updateEvent(eventId, eventName, description, datetime, location, false, 1);
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

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}

