package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private CalendarView calendarView;
    private TextView eventDetails;
    private DatabaseHelper databaseHelper;

    private String mParam1;
    private String mParam2;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("Range")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        eventDetails = view.findViewById(R.id.eventDetails);
        EditText inputEvent = view.findViewById(R.id.inputEvent);
        Button btnAddEvent = view.findViewById(R.id.btnAddEvent);
        Button btnUpdateEvent = view.findViewById(R.id.btnUpdateEvent);
        Button btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnViewAllEvents = view.findViewById(R.id.btnViewAllEvents); // Thêm nút xem tất cả sự kiện

        databaseHelper = new DatabaseHelper(getContext());

        final String[] selectedDate = {null};

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate[0] = year + "-" + (month + 1) + "-" + dayOfMonth;

            // Hiển thị sự kiện của ngày đã chọn
            Cursor cursor = databaseHelper.getEventsByDate(selectedDate[0]);
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder events = new StringBuilder();
                do {
                    events.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT))).append("\n");
                } while (cursor.moveToNext());
                eventDetails.setText(events.toString());
            } else {
                eventDetails.setText("Không có sự kiện nào.");
            }
            if (cursor != null) {
                cursor.close();
            }
        });

        btnAddEvent.setOnClickListener(v -> {
            if (selectedDate[0] != null && !inputEvent.getText().toString().isEmpty()) {
                databaseHelper.addEvent(selectedDate[0], inputEvent.getText().toString());
                Toast.makeText(getContext(), "Thêm sự kiện thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Hãy chọn ngày và nhập sự kiện!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateEvent.setOnClickListener(v -> {
            if (selectedDate[0] != null && !inputEvent.getText().toString().isEmpty()) {
                databaseHelper.updateEvent(selectedDate[0], inputEvent.getText().toString());
                Toast.makeText(getContext(), "Cập nhật sự kiện thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Hãy chọn ngày và nhập sự kiện!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteEvent.setOnClickListener(v -> {
            if (selectedDate[0] != null) {
                databaseHelper.deleteEvent(selectedDate[0]);
                Toast.makeText(getContext(), "Xóa sự kiện thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Hãy chọn ngày để xóa sự kiện!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút xem tất cả sự kiện
        btnViewAllEvents.setOnClickListener(v -> {
            Cursor cursor = databaseHelper.getAllEvents();
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder allEvents = new StringBuilder();
                do {
                    String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                    String event = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT));
                    allEvents.append("Date: ").append(date).append(" - Event: ").append(event).append("\n");
                } while (cursor.moveToNext());

                // Hiển thị danh sách sự kiện trong TextView hoặc Toast
                eventDetails.setText(allEvents.toString());
            } else {
                Toast.makeText(getContext(), "Không có sự kiện nào trong cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
            }
            if (cursor != null) {
                cursor.close();
            }
        });

        return view;
    }
}