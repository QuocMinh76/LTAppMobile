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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        eventDetails = view.findViewById(R.id.eventDetails);
        EditText inputEventName = view.findViewById(R.id.inputEventName);
        EditText inputEventDescription = view.findViewById(R.id.inputEventDescription);
        EditText inputStartTime = view.findViewById(R.id.inputStartTime);
        EditText inputEndTime = view.findViewById(R.id.inputEndTime);
        Button btnAddEvent = view.findViewById(R.id.btnAddEvent);
        Button btnUpdateEvent = view.findViewById(R.id.btnUpdateEvent);
        Button btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        Button btnViewAllEvents = view.findViewById(R.id.btnViewAllEvents);

        databaseHelper = new DatabaseHelper(getContext());
//        databaseHelper.clearDatabase(); //xoa database

        // Add Users
        databaseHelper.addUser("Alice", "alice123", "pass123", false);  // User Alice, not an admin
        databaseHelper.addUser("Bob", "bob456", "pass456", true);       // User Bob, is an admin

        // Add Categories
        databaseHelper.addCategory("Work", 1);      // Category Work for User 1 (Alice)
        databaseHelper.addCategory("Personal", 1);  // Category Personal for User 1 (Alice)
        databaseHelper.addCategory("Meetings", 2);  // Category Meetings for User 2 (Bob)

        // Add Events
        databaseHelper.addEvent("Team Meeting", "Discuss Q4 goals", "2024-12-27 10:00", "Office Room A", "High", 3, 2);
        databaseHelper.addEvent("Doctor Appointment", "Annual checkup", "2024-12-28 14:30", "City Hospital", "Medium", 2, 1);
        databaseHelper.addEvent("Grocery Shopping", "Weekly groceries", "2024-12-29 16:00", "Supermarket", "Low", 2, 1);

        final String[] selectedDate = {null};

//        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
//            selectedDate[0] = year + "-" + (month + 1) + "-" + dayOfMonth;
//
//            // Hiển thị sự kiện của ngày đã chọn
//            Cursor cursor = databaseHelper.getEventsByDate(selectedDate[0]);
//            if (cursor != null && cursor.moveToFirst()) {
//                StringBuilder events = new StringBuilder();
//                do {
//                    String eventName = cursor.getString(cursor.getColumnIndex("EventName"));
//                    String eventDescription = cursor.getString(cursor.getColumnIndex("EventDescription"));
//                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
//                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
//                    events.append("Name: ").append(eventName).append("\n")
//                            .append("Description: ").append(eventDescription).append("\n")
//                            .append("Start: ").append(startTime).append(" - End: ").append(endTime).append("\n\n");
//                } while (cursor.moveToNext());
//                eventDetails.setText(events.toString());
//            } else {
//                eventDetails.setText("No events for the selected date.");
//            }
//            if (cursor != null) {
//                cursor.close();
//            }
//        });
        return view;
    }
}
