package com.mymiki.mimyki;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int taskId, user_id;
    private EditText taskNameEditText, taskDescriptionEditText,
            taskDateEditText, taskPriorityEditText, taskLocationEditText;
    private Spinner categorySpinner;
    private CheckBox taskDoneCheckBox;
    int priorityTag;
    String priorityName;
    List<Integer> categoryIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        dbHelper = new DatabaseHelper(this);

        // Initialize the views
        taskNameEditText = findViewById(R.id.task_name_edit);
        taskDescriptionEditText = findViewById(R.id.task_description_edit);
        taskDateEditText = findViewById(R.id.task_date_edit);
        categorySpinner = findViewById(R.id.category_spinner);
        taskDoneCheckBox = findViewById(R.id.task_done_checkbox);
        taskPriorityEditText = findViewById(R.id.priority_edit);
        taskLocationEditText = findViewById(R.id.task_location_edit);

        // Get the task ID passed via Intent
        taskId = getIntent().getIntExtra("TASK_ID", -1);
        user_id = getIntent().getIntExtra("USER_ID", -1);

        dbHelper.getEventById(taskId);

        if (taskId != -1) {
            loadTaskDetails(taskId);
        }

        // Save button listener
        findViewById(R.id.save_task_button).setOnClickListener(v -> saveTaskDetails());

        // Cancel button listener
        findViewById(R.id.cancel_task_button).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btn_open_location_d).setOnClickListener(v -> openLocationInGoogleMaps(String.valueOf(taskNameEditText.getText()), user_id));
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }

    private void loadTaskDetails(int taskId) {
        // Query the database to fetch the task details
        Cursor cursor = dbHelper.getEventById(taskId);

        if (cursor != null && cursor.moveToFirst()) {
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME));
            String taskDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String taskDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATETIME));
            String taskLocation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            boolean isTaskDone = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DONE)) == 1;
            priorityTag = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY_TAG));
            int taskCategoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATE_ID)); // Fetch the task's category ID

            // Set the values in the UI
            taskNameEditText.setText(taskName);
            taskDescriptionEditText.setText(taskDescription);
            taskDateEditText.setText(taskDate);
            taskLocationEditText.setText(taskLocation);
            taskDoneCheckBox.setChecked(isTaskDone);

            priorityName = dbHelper.getPriorityNameById(priorityTag);
            taskPriorityEditText.setText(priorityName);

            Cursor categoryCursor = dbHelper.getCategoriesByUserId(user_id);
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            categoryIds = new ArrayList<>();
            int selectedCategoryIndex = -1; // To keep track of the index of the task's category
            int currentIndex = 0; // Counter to track the current index in the loop

            while (categoryCursor.moveToNext()) {
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                int categoryId = categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                categoryAdapter.add(categoryName);
                categoryIds.add(categoryId);

                if (categoryId == taskCategoryId) {
                    selectedCategoryIndex = currentIndex; // Found the task's category
                }
                currentIndex++;
            }
            categoryCursor.close();

            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);

            // Set the selected item in the spinner to the task's category
            if (selectedCategoryIndex != -1) {
                categorySpinner.setSelection(selectedCategoryIndex);
            }

            cursor.close();
        }
    }


    private void saveTaskDetails() {
        String taskName = taskNameEditText.getText().toString();
        String taskDescription = taskDescriptionEditText.getText().toString();
        String taskDate = taskDateEditText.getText().toString();
        String taskLocation = taskLocationEditText.getText().toString();
        boolean isTaskDone = taskDoneCheckBox.isChecked();
        int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
        int selectedCategoryId = categoryIds.get(selectedCategoryPosition);

        // Update the task in the database
        dbHelper.updateEvent(taskId, taskName, taskDescription, taskDate, taskLocation, isTaskDone, selectedCategoryId, priorityTag);

        setResult(Activity.RESULT_OK);

        // Return to the previous screen
        finish();
    }

    private void openLocationInGoogleMaps(String taskName, int userId) {
        // Retrieve the location from the database
        String location = dbHelper.getTaskLocation(taskName, userId);

        if (location != null && !location.isEmpty()) {
            // Create an Intent to open Google Maps
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if there is an app to handle the Intent
            if (mapIntent.resolveActivity(this.getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Handle the case where Google Maps is not installed
                Toast.makeText(this, "Chưa tải Google Maps!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where the location is not valid
            Toast.makeText(this, "Vị trí cung cấp không khả dụng!", Toast.LENGTH_SHORT).show();
        }
    }
}