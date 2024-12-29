package com.mymiki.mimyki;

import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_ID;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_NAME;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_DATETIME;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_DONE;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_ID;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_NAME;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {

    private static final int REQUEST_CODE_EDIT_TASK = 1;
    private int user_id = -1;
    private DatabaseHelper dbHelper;
    LinearLayout mainContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user_id = getUserIdFromSharedPreferences();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);

        // FloatingActionButton for adding tasks
        FloatingActionButton fabAddTask = rootView.findViewById(R.id.fab_add_task);

        DrawerLayout drawerLayout = rootView.findViewById(R.id.drawer_layout);
        ImageButton btnSidePanel = rootView.findViewById(R.id.btn_side_panel);

        btnSidePanel.setOnClickListener(v -> {
                drawerLayout.openDrawer(GravityCompat.START);
        });

        // Main container in activity_main.xml
        mainContainer = rootView.findViewById(R.id.main_container);

        FloatingActionButton addCategoryButton = rootView.findViewById(R.id.fab_add_category);
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());



        // Set a click listener for the FAB
        fabAddTask.setOnClickListener(v -> showAddTaskDialog(null));

        displayCategoriesAndEvents(mainContainer);

        refreshView();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == Activity.RESULT_OK) {
            // The task was updated, so refresh the task list
            refreshView();  // Your method to refresh the view
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm phân loại mới");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set padding for the layout to add space around the content
        int padding = (int) (20 * getResources().getDisplayMetrics().density); // 20dp padding
        layout.setPadding(padding, padding, padding, padding);

        // Add an EditText to enter the category name
        final EditText categoryNameInput = new EditText(getContext());
        categoryNameInput.setHint("Tên phân loại công việc");

        // Set padding for the EditText
        categoryNameInput.setPadding(padding, padding, padding, padding); // Adjust the padding for the EditText

        layout.addView(categoryNameInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String categoryName = categoryNameInput.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                dbHelper.addCategory(categoryName, user_id); // user_id is the current user's ID
                Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
                refreshView();
            } else {
                Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showAddTaskDialog(@Nullable Integer taskId) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event_add_task, null);

        // Initialize input fields
        EditText taskNameInput = dialogView.findViewById(R.id.eventNameInput_t);
        EditText taskDescriptionInput = dialogView.findViewById(R.id.descriptionInput_t);
        Button dateTimeButton = dialogView.findViewById(R.id.dateTimeInput_t);
        EditText taskLocationInput = dialogView.findViewById(R.id.locationInput_t);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner_t);

        // Populate the category spinner
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

        final String[] formattedDateTime = {""};

        // Set up Date & Time picker
        dateTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // If editing, initialize with existing datetime
            if (taskId != null) {
                Cursor taskCursor = dbHelper.getEventById(taskId, user_id);
                if (taskCursor != null && taskCursor.moveToFirst()) {
                    String datetimeStr = taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATETIME));
                    String[] datetimeParts = datetimeStr.split(" ");
                    if (datetimeParts.length == 2) {
                        String[] dateParts = datetimeParts[0].split("-");
                        String[] timeParts = datetimeParts[1].split(":");
                        if (dateParts.length == 3 && timeParts.length == 2) {
                            int year = Integer.parseInt(dateParts[0]);
                            int month = Integer.parseInt(dateParts[1]) - 1; // Months are 0-based
                            int day = Integer.parseInt(dateParts[2]);
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            calendar.set(year, month, day, hour, minute);
                        }
                    }
                    taskCursor.close();
                }
            }

            // Show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // After selecting date, show TimePickerDialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                (timeView, hourOfDay, minute) -> {
                                    formattedDateTime[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                                            year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                                    dateTimeButton.setText(formattedDateTime[0]);
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

        // If editing an existing task, populate the fields with current data
        if (taskId != null) {
            Cursor taskCursor = dbHelper.getEventById(taskId, user_id);
            if (taskCursor != null && taskCursor.moveToFirst()) {
                taskNameInput.setText(taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME)));
                taskDescriptionInput.setText(taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
                taskLocationInput.setText(taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION)));
                String datetime = taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATETIME));
                dateTimeButton.setText(datetime);
                String categoryName = taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATE_ID));
                int categoryPosition = categoryAdapter.getPosition(categoryName);
                if (categoryPosition >= 0) {
                    categorySpinner.setSelection(categoryPosition);
                }
                taskCursor.close();
            }
        } else {
            // If adding a new task, set the current datetime as default
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dateTimeButton.setText(currentDateTime);
        }

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setPositiveButton(taskId == null ? "Thêm" : "Sửa", (dialog, which) -> {
                    String taskName = taskNameInput.getText().toString().trim();
                    String taskDescription = taskDescriptionInput.getText().toString().trim();
                    String taskDateTime = dateTimeButton.getText().toString().trim();
                    String taskLocation = taskLocationInput.getText().toString().trim();
                    String categoryName = categorySpinner.getSelectedItem().toString();
                    int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
                    int selectedCategoryId = categoryIds.get(selectedCategoryPosition);

                    // Validate required fields
                    if (taskName.isEmpty()) {
                        Toast.makeText(getContext(), "Tên công việc là bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (taskId == null) {
                        // Adding a new task
                        dbHelper.addEvent(taskName, taskDescription, taskDateTime, taskLocation, false, 1, selectedCategoryId, user_id);

                        String taskContent = taskNameInput.getText().toString().trim();
                        // Lấy thời gian thông báo từ SharedPreferences
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                        int offsetMinutes = sharedPref.getInt("notification_offset", 1);

                        // Lên lịch thông báo
                        NotificationScheduler.scheduleNotification(getContext(), taskContent, formattedDateTime[0], offsetMinutes);
                        Toast.makeText(getContext(), "Công việc đã được thêm", Toast.LENGTH_SHORT).show();
                    } else {
                        // Updating an existing task
                        dbHelper.updateEvent(taskId, taskName, taskDescription, taskDateTime, taskLocation, false, selectedCategoryId, 1);
                        Toast.makeText(getContext(), "Công việc đã được cập nhật", Toast.LENGTH_SHORT).show();
                    }

                    // Refresh the task list or UI
                    refreshView();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa", (dialog, which) -> {
                    if (taskId != null) {
                        dbHelper.deleteEvent(taskId);
                        Toast.makeText(getContext(), "Công việc đã được xóa", Toast.LENGTH_SHORT).show();
                        refreshView();
                    }
                });

        // Show the dialog
        builder.create().show();
    }

    private void refreshView() {
        // Clear the current view (container)
        mainContainer.removeAllViews();

        // Fetch categories for the current user from the database
        Cursor categoriesCursor = dbHelper.getCategoriesByUserId(user_id);

        if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
            do {
                // Get category name from the cursor
                String categoryName = categoriesCursor.getString(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                int categoryId = categoriesCursor.getInt(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));

                // Create a task group layout for each category
                View categoryView = getLayoutInflater().inflate(R.layout.list_item_task_group, mainContainer, false);

                // Set the category title
                TextView categoryTitle = categoryView.findViewById(R.id.task_group_title);
                categoryTitle.setText(categoryName);

                // Find the container to add events for this category
                LinearLayout eventContainer = categoryView.findViewById(R.id.category_layout);

                eventContainer.setTag(categoryId); // Assign the category ID to the container
                Cursor eventsCursor = dbHelper.getEventsByCategoryId(categoryId);

                // Inside the loop where you're adding categories
                categoryView.setOnLongClickListener(v -> {
                    String currentCategoryName = ((TextView) v.findViewById(R.id.task_group_title)).getText().toString();
                    showCategoryDialog(categoryId, currentCategoryName);
                    return true;
                });

                // Set drop listener for category
                eventContainer.setOnDragListener((v, event) -> {
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            return true;

                        case DragEvent.ACTION_DROP:
                            // Get the task ID from ClipData
                            ClipData.Item item = event.getClipData().getItemAt(0);
                            int draggedTaskId = Integer.parseInt(item.getText().toString());

                            // No need to remove the dragged view, as refreshView will reload the task's category
                            // We just need to update the task's category in the database
                            int newCategoryId = (int) v.getTag();  // Ensure the category's ID is stored in the view's tag

                            // Check if the event container (category) is actually ready to accept the task
                            if (v instanceof LinearLayout && ((LinearLayout) v).getChildCount() == 0) {
                                // This category is empty, so we can safely drop the task here
                                dbHelper.updateTaskCategory(draggedTaskId, newCategoryId);
                                refreshView();
                                return true;
                            } else {
                                // Handle the case where the category isn't empty (optional)
                                dbHelper.updateTaskCategory(draggedTaskId, newCategoryId);
                                refreshView();
                                return true;
                            }

                        case DragEvent.ACTION_DRAG_ENDED:
                            return true;

                        default:
                            return false;
                    }
                });

                if (eventsCursor != null && eventsCursor.moveToFirst()) {
                    do {
                        // Create a view for each event (task)
                        View eventView = getLayoutInflater().inflate(R.layout.list_item_task, eventContainer, false);

                        // Set the event details from the cursor
                        CheckBox eventDone = eventView.findViewById(R.id.list_item_task_checkbox_done);
                        boolean isDone = eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_DONE)) == 1;
                        eventDone.setChecked(isDone);

                        // Bind the event's ID to the delete and edit buttons
                        int eventId = eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));

                        TextView eventDescription = eventView.findViewById(R.id.list_item_task_description);
                        eventDescription.setText(eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)));

                        TextView eventDate = eventView.findViewById(R.id.list_item_task_date);
                        eventDate.setText(eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(COLUMN_DATETIME)));

                        // Swipe layout initialization
                        SwipeLayout swipeLayout = eventView.findViewById(R.id.swipe_layout);
                        Button editButton = eventView.findViewById(R.id.task_edit);
                        Button deleteButton = eventView.findViewById(R.id.task_delete);

                        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                            @Override
                            public void onOpen(SwipeLayout layout) {
                                // Swipe layout opened, disable click actions
                                swipeLayout.setTag(true);  // Mark as open
                            }

                            @Override
                            public void onClose(SwipeLayout layout) {
                                // Swipe layout closed, enable click actions
                                swipeLayout.setTag(false);  // Mark as closed
                            }

                            @Override
                            public void onStartOpen(SwipeLayout layout) {}

                            @Override
                            public void onStartClose(SwipeLayout layout) {}

                            @Override
                            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

                            @Override
                            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
                        });


                        editButton.setOnClickListener(v -> showAddTaskDialog(eventId));

                        deleteButton.setOnClickListener(v -> {
                            NotificationScheduler.cancelNotification(getContext(), eventDescription.toString().trim());
                            deleteTask(eventId);
                        });

                        eventView.setOnClickListener(v -> {
                            // Open TaskDetailsActivity with the task ID
                            if (swipeLayout.getTag() == null || !(Boolean) swipeLayout.getTag()) {
                                Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
                                intent.putExtra("TASK_ID", eventId);
                                intent.putExtra("USER_ID", user_id);
                                startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
                            }
                        });

                        eventView.setOnLongClickListener(v -> {
                            if (swipeLayout.getTag() == null || !(Boolean) swipeLayout.getTag()) {
                                ClipData clipData = ClipData.newPlainText("taskId", String.valueOf(eventId));
                                View.DragShadowBuilder dragShadow = new View.DragShadowBuilder(v);
                                eventView.startDrag(clipData, dragShadow, null, 0); // You can pass flags as 0
                                Log.d("DragDrop", "Drag started for Task ID: " + eventId);
                            }
                            return true;
                        });

                        // Add the event view to the event container
                        eventContainer.addView(eventView);

                        // Set listener for task checkbox
                        eventDone.setTag(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_ID))); // Set task ID in the tag

                        eventDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            // Get the task ID from the tag of the checkbox
                            int taskId = (int) buttonView.getTag(); // Retrieve task ID from the tag

                            // Update the task's completion status in the database
                            dbHelper.updateTaskDoneStatus(taskId, isChecked);

                            // Force layout refresh
                            mainContainer.requestLayout();
                        });

                    } while (eventsCursor.moveToNext());

                    eventsCursor.close();
                }

                // Add the category view to the main container
                mainContainer.addView(categoryView);
            } while (categoriesCursor.moveToNext());

            categoriesCursor.close();
        }
    }

    private void showCategoryDialog(final int categoryId, String currentCategoryName) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.category_edit_layout, null);
        final EditText categoryNameInput = dialogView.findViewById(R.id.category_name_input);
        final Button editButton = dialogView.findViewById(R.id.btn_edit);
        final Button deleteButton = dialogView.findViewById(R.id.btn_delete);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        // Set the current category name in the input field
        categoryNameInput.setText(currentCategoryName);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Check if there are any events in the category
        boolean hasEvents = dbHelper.hasEventsInCategory(categoryId);

        // Disable the Delete button if there are events in the category
        if (hasEvents) {
            deleteButton.setEnabled(false);
            deleteButton.setTextColor(Color.GRAY); // Optionally disable with color
        } else {
            deleteButton.setEnabled(true);
        }

        // Edit button click listener
        editButton.setOnClickListener(v -> {
            String newCategoryName = categoryNameInput.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                dbHelper.updateCategory(categoryId, newCategoryName);
                refreshView(); // Refresh the category list after renaming
                dialog.dismiss();
            } else {
                // Show a warning or error message if the name is empty
                Toast.makeText(getContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button click listener
        deleteButton.setOnClickListener(v -> {
            if (!hasEvents) {
                dbHelper.deleteCategory(categoryId);
                refreshView(); // Refresh the category list after deletion
                dialog.dismiss();
            } else {
                // Show a warning if there are events in the category
                Toast.makeText(getContext(), "Cannot delete category with events", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button click listener
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void displayCategoriesAndEvents(LinearLayout container) {
        Cursor categoryCursor = dbHelper.getCategoriesByUserId(user_id);

        if (categoryCursor != null && categoryCursor.moveToFirst()) {
            do {
                // Extract category data
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                int categoryId = categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));

                // Inflate task group layout
                View taskGroupView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_task_group, container, false);
                TextView taskGroupTitle = taskGroupView.findViewById(R.id.task_group_title);
                LinearLayout taskContainer = taskGroupView.findViewById(R.id.task_container);

                // Set task group title
                taskGroupTitle.setText(categoryName);

                // Load and display tasks for this category
                Cursor taskCursor = dbHelper.getEventsByCategoryId(categoryId);
                if (taskCursor != null && taskCursor.moveToFirst()) {
                    do {
                        // Extract task data
                        String taskDescription = taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME));
                        String taskDate = taskCursor.getString(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATETIME)); // Example column
                        boolean isTaskDone = taskCursor.getInt(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DONE)) == 1;
                        int taskId = taskCursor.getInt(taskCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_ID));

                        // Inflate task layout
                        View taskView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_task, taskContainer, false);
                        CheckBox taskCheckbox = taskView.findViewById(R.id.list_item_task_checkbox_done);
                        TextView taskDescriptionView = taskView.findViewById(R.id.list_item_task_description);
                        TextView taskDateView = taskView.findViewById(R.id.list_item_task_date);

                        // Set task details
                        taskDescriptionView.setText(taskDescription);
                        taskDateView.setText(taskDate);
                        taskCheckbox.setChecked(isTaskDone);

                        // Add the task view to the task container
                        taskContainer.addView(taskView);

                        // Optional: Set listeners for task interactions
                        taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            // Update the task's completion status in the database using the stored task ID
                            dbHelper.updateTaskDoneStatus(taskId, isChecked);
                        });

                    } while (taskCursor.moveToNext());
                    taskCursor.close();
                }

                // Add the task group view to the main container
                container.addView(taskGroupView);

            } while (categoryCursor.moveToNext());
            categoryCursor.close();
        }
    }

    public void deleteTask(int taskId) {
        // Delete the task from the database using the taskId
        dbHelper.deleteEvent(taskId);

        // Refresh the view to reflect the removal
        refreshView();
    }

    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}