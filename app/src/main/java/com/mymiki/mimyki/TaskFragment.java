package com.mymiki.mimyki;

import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_ID;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_CATEGORY_NAME;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_DATETIME;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_DONE;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_ID;
import static com.mymiki.mimyki.DatabaseHelper.COLUMN_EVENT_NAME;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskFragment extends Fragment {

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
        //Button addTask = findViewById(R.id.btn_add);

        // Main container in activity_main.xml
        mainContainer = rootView.findViewById(R.id.main_container);

        ImageButton addCategoryButton = rootView.findViewById(R.id.imageButton_add_category);
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        // Set a click listener for the FAB
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        displayCategoriesAndEvents(mainContainer);

        return rootView;
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

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm công việc");

        // Create a layout for the dialog
        LinearLayout layout = createDialogLayout();

        // Event Name (Required)
        final EditText taskNameInput = createEditText("Tên công việc (Bắt buộc)");
        layout.addView(taskNameInput);

        // Event Description (Optional)
        final EditText taskDescriptionInput = createEditText("Mô tả");
        layout.addView(taskDescriptionInput);

        // Date & Time Selector (Optional, default to today's date and time)
        final EditText taskDateTimeInput = createEditText("Ngày và Giờ");
        taskDateTimeInput.setText(getCurrentDateTime());
        setupDateTimeInput(taskDateTimeInput);
        layout.addView(taskDateTimeInput);

        // Event Location (Optional)
        final EditText taskLocationInput = createEditText("Địa điểm");
        layout.addView(taskLocationInput);

        // Category Spinner
        final Spinner categorySpinner = createCategorySpinner();
        layout.addView(categorySpinner);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            handleAddButtonClick(taskNameInput, taskDescriptionInput, taskDateTimeInput, taskLocationInput, categorySpinner);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Helper method to create dialog layout
    private LinearLayout createDialogLayout() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    // Helper method to create an EditText with a hint
    private EditText createEditText(String hint) {
        EditText editText = new EditText(getContext());
        editText.setHint(hint);
        editText.setPadding(20, 50, 20, 50); // Padding inside the EditText
        return editText;
    }

    // Helper method to get the current date and time
    private String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime());
    }

    // Helper method to set up Date & Time input
    private void setupDateTimeInput(EditText taskDateTimeInput) {
        taskDateTimeInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                    String formattedDateTime = String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d",
                            year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                    taskDateTimeInput.setText(formattedDateTime); // Set the formatted date-time
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    // Helper method to create the category spinner and populate it with categories
    private Spinner createCategorySpinner() {
        final Spinner categorySpinner = new Spinner(getContext());
        List<String> categoryNames = new ArrayList<>();
        final List<Integer> categoryIds = new ArrayList<>();
        Cursor categoriesCursor = dbHelper.getCategoriesByUserId(user_id);
        if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
            do {
                String categoryName = categoriesCursor.getString(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                int categoryId = categoriesCursor.getInt(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
                categoryNames.add(categoryName);
                categoryIds.add(categoryId); // Add category ID to the list
            } while (categoriesCursor.moveToNext());
            categoriesCursor.close();
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(0); // Set default selection to the first category
        return categorySpinner;
    }

    // Handle the "Add" button click and insert the new task
    private void handleAddButtonClick(EditText taskNameInput, EditText taskDescriptionInput, EditText taskDateTimeInput,
                                      EditText taskLocationInput, Spinner categorySpinner) {
        String taskName = taskNameInput.getText().toString().trim();
        String taskDescription = taskDescriptionInput.getText().toString().trim();
        String taskDateTime = taskDateTimeInput.getText().toString().trim();
        String taskLocation = taskLocationInput.getText().toString().trim();
        String priority = "Khác"; // Hardcoded priority value

        // Get selected category ID from the spinner
        int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
        List<Integer> categoryIds = getCategoryIds(); // Get category IDs list
        int selectedCategoryId = categoryIds.get(selectedCategoryPosition);

        if (taskName.isEmpty()) {
            Toast.makeText(getContext(), "Task name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add event to the database
        dbHelper.addEvent(taskName, taskDescription, taskDateTime, taskLocation, false, priority, selectedCategoryId, user_id);

        // Retrieve the event ID (after insertion) using a query
        int eventId = dbHelper.getLastInsertedEventId();

        if (eventId != -1) {
            Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
            addTaskToLayout(taskName, eventId, false); // Pass event ID here
        } else {
            Toast.makeText(getContext(), "Error adding task", Toast.LENGTH_SHORT).show();
        }

        refreshView(); // Refresh the view after adding a task
    }

    // Helper method to get category IDs
    private List<Integer> getCategoryIds() {
        List<Integer> categoryIds = new ArrayList<>();
        Cursor categoriesCursor = dbHelper.getCategoriesByUserId(user_id);
        if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
            do {
                categoryIds.add(categoriesCursor.getInt(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
            } while (categoriesCursor.moveToNext());
            categoriesCursor.close();
        }
        return categoryIds;
    }


    private void addTaskToLayout(String taskName, int eventId, boolean isChecked) {
        // Get the main container for tasks
        LinearLayout mainContainer = getView().findViewById(R.id.main_container);

        // Create a layout for the task with checkbox and label
        LinearLayout taskLayout = createTaskLayout();

        // Create and configure the checkbox for the task
        CheckBox taskCheckBox = createTaskCheckBox(taskName, isChecked);

        // Set up a listener to handle the checkbox status change
        taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            // Update the task status in the database using the event ID
            dbHelper.updateTaskDoneStatus(eventId, isChecked1);
        });

        // Add the checkbox to the task layout
        taskLayout.addView(taskCheckBox);

        // Add the task layout to the main container
        mainContainer.addView(taskLayout);
    }

    // Helper method to create the main task layout
    private LinearLayout createTaskLayout() {
        LinearLayout taskLayout = new LinearLayout(getContext());
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        taskLayout.setPadding(0, 10, 0, 10);
        return taskLayout;
    }

    // Helper method to create and configure the checkbox
    private CheckBox createTaskCheckBox(String taskName, boolean isChecked) {
        CheckBox taskCheckBox = new CheckBox(getContext());
        taskCheckBox.setChecked(isChecked);
        taskCheckBox.setText(taskName);
        return taskCheckBox;
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

                // Create a task group layout for each category
                View categoryView = getLayoutInflater().inflate(R.layout.list_item_task_group, mainContainer, false);

                // Set the category title
                TextView categoryTitle = categoryView.findViewById(R.id.task_group_title);
                categoryTitle.setText(categoryName);

                // Find the container to add events for this category
                LinearLayout eventContainer = categoryView.findViewById(R.id.task_container);

                // Fetch events for this category using the category ID
                int categoryId = categoriesCursor.getInt(categoriesCursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
                Cursor eventsCursor = dbHelper.getEventsByCategoryId(categoryId);

                if (eventsCursor != null && eventsCursor.moveToFirst()) {
                    do {
                        // Create a view for each event (task)
                        View eventView = getLayoutInflater().inflate(R.layout.list_item_task, eventContainer, false);

                        // Set the event details from the cursor
                        CheckBox eventDone = eventView.findViewById(R.id.list_item_task_checkbox_done);
                        boolean isDone = eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_DONE)) == 1;
                        eventDone.setChecked(isDone);

                        TextView eventDescription = eventView.findViewById(R.id.list_item_task_description);
                        eventDescription.setText(eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)));

                        TextView eventDate = eventView.findViewById(R.id.list_item_task_date);
                        eventDate.setText(eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(COLUMN_DATETIME)));

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


    private void loadCategories() {
        Cursor cursor = dbHelper.getCategoriesByUserId(user_id);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                // Display the category (e.g., add it to the UI)
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void loadEventsByCategory(int categoryId) {
        Cursor cursor = dbHelper.getEventsByCategoryId(categoryId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int eventId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));
                String eventName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME));
                // Display the event under the category
            } while (cursor.moveToNext());
            cursor.close();
        }
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


    private int getUserIdFromSharedPreferences() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        return sharedPref.getInt("user_id", -1);
    }
}