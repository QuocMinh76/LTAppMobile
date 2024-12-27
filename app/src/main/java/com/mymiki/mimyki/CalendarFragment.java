package com.mymiki.mimyki;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        eventListView = view.findViewById(R.id.eventListView);
        addEventButton = view.findViewById(R.id.addEventButton);
        dbHelper = new DatabaseHelper(getContext());

        // Lấy ngày được chọn
        selectedDate = getCurrentDate();
        // Xử lý sự kiện chọn ngày
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadEventsForSelectedDate();
        });
        // Nút thêm sự kiện
        addEventButton.setOnClickListener(v -> openEventDialog(null));
        return view;
    }

    private void loadEventsForSelectedDate() {
        Cursor cursor = dbHelper.getEventsByDate(selectedDate); // Lấy sự kiện theo ngày từ cơ sở dữ liệu
        if (cursor != null && cursor.getCount() > 0) { // Kiểm tra nếu có sự kiện
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            final List<Integer> eventIds = new ArrayList<>(); // Danh sách lưu trữ eventId

            // Lấy tất cả sự kiện cho ngày đã chọn
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String eventName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME));
                @SuppressLint("Range") int eventId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID));
                eventIds.add(eventId); // Lưu eventId tương ứng với mỗi tên sự kiện
                adapter.add(eventName); // Thêm tên sự kiện vào adapter
            }

            cursor.close(); // Đảm bảo đóng cursor khi xong
            eventListView.setAdapter(adapter); // Gắn adapter cho ListView

            // Xử lý khi nhấn vào sự kiện
            eventListView.setOnItemClickListener((parent, view, position, id) -> {
                // Lấy eventId từ danh sách đã lưu
                int eventId = eventIds.get(position);
                openEventDialog(eventId); // Mở dialog sửa sự kiện
            });

            // Hiển thị lại ListView nếu có sự kiện
            eventListView.setVisibility(View.VISIBLE);
            TextView noEventsTextView = getView().findViewById(R.id.noEventsTextView);
            if (noEventsTextView != null) {
                noEventsTextView.setVisibility(View.GONE); // Ẩn thông báo "Không có sự kiện"
            }
        } else {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            emptyAdapter.add("Không có sự kiện cho ngày này");
            eventListView.setAdapter(emptyAdapter); // Hiển thị thông báo nếu không có sự kiện

            // Ẩn ListView và hiển thị TextView thông báo
            eventListView.setVisibility(View.GONE); // Ẩn ListView
            TextView noEventsTextView = getView().findViewById(R.id.noEventsTextView);
            noEventsTextView.setVisibility(View.VISIBLE); // Hiển thị thông báo
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

        // Lấy danh sách category
        Cursor categoryCursor = dbHelper.getAllCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        while (categoryCursor.moveToNext()) {
            categoryAdapter.add(categoryCursor.getString(categoryCursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME)));
        }
        categoryCursor.close();
        categorySpinner.setAdapter(categoryAdapter);

        // Set up TimePickerDialog
        timeButton.setOnClickListener(v -> {
            int hour = 12; // Default hour
            int minute = 0; // Default minute

            // If eventId is provided, pre-fill the time
            if (eventId != null) {
                Cursor eventCursor = dbHelper.getEventById(eventId);
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

            // Open TimePickerDialog
            new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute1);
                timeButton.setText(time); // Set the selected time on the button
            }, hour, minute, true).show();
        });

        // Nếu eventId là null, set giờ hiện tại cho nút
        if (eventId == null) {
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            timeButton.setText(currentTime); // Set the current time if no eventId
        } else {
            // Nếu là chỉnh sửa, dữ liệu sự kiện sẽ được load từ cơ sở dữ liệu
            Cursor eventCursor = dbHelper.getEventById(eventId);
            if (eventCursor != null && eventCursor.moveToFirst()) {
                eventNameInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME)));
                descriptionInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
                locationInput.setText(eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION)));
                String[] datetimeParts = eventCursor.getString(eventCursor.getColumnIndex(DatabaseHelper.COLUMN_DATETIME)).split(" ");
                if (datetimeParts.length == 2) {
                    timeButton.setText(datetimeParts[1]); // Set time from event
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

                    // Kết hợp ngày đã chọn với giờ nhập vào
                    String datetime = selectedDate + " " + time;

                    if (eventId == null) {
                        // Tìm cateId từ categoryName
                        Cursor cursor = dbHelper.getAllCategories();
                        int cateId = 1; // Giá trị mặc định nếu không tìm thấy
                        while (cursor.moveToNext()) {
                            if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME)).equals(categoryName)) {
                                cateId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_ID));
                                break;
                            }
                        }
                        cursor.close();

                        dbHelper.addEvent(eventName, description, datetime, location, "Khác", cateId, 1); // Thêm userId thực tế
                    } else {
                        dbHelper.updateEvent(eventId, eventName, description, datetime, location, "Khác");
                    }
                    loadEventsForSelectedDate(); // Cập nhật lại danh sách sự kiện
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa", (dialog, which) -> {
                    if (eventId != null) {
                        dbHelper.deleteEvent(eventId); // Xóa sự kiện khỏi cơ sở dữ liệu
                        loadEventsForSelectedDate(); // Làm mới danh sách sự kiện sau khi xóa
                    }
                });
        builder.create().show();
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }
}
