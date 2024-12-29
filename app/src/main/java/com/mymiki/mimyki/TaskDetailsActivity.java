package com.mymiki.mimyki;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

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
    private EditText taskNameEditText;
    private EditText taskDescriptionEditText;
    private EditText taskDateEditText;
    private EditText taskPriorityEditText;
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

        // Get the task ID passed via Intent
        taskId = getIntent().getIntExtra("TASK_ID", -1);
        user_id = getIntent().getIntExtra("USER_ID", -1);

        dbHelper.getEventById(taskId);

        if (taskId != -1) {
            loadTaskDetails(taskId);
        }

        // Save button listener
        findViewById(R.id.save_task_button).setOnClickListener(v -> saveTaskDetails());
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
            boolean isTaskDone = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DONE)) == 1;
            priorityTag = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY_TAG));

            // Set the values in the UI
            taskNameEditText.setText(taskName);
            taskDescriptionEditText.setText(taskDescription);
            taskDateEditText.setText(taskDate);
            taskDoneCheckBox.setChecked(isTaskDone);

            priorityName = dbHelper.getPriorityNameById(priorityTag);

            taskPriorityEditText.setText(priorityName);

            Cursor categoryCursor = dbHelper.getCategoriesByUserId(user_id);
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            categoryIds = new ArrayList<>();
            while (categoryCursor.moveToNext()) {
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                int categoryId = categoryCursor.getInt(categoryCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                categoryAdapter.add(categoryName);
                categoryIds.add(categoryId);
            }
            categoryCursor.close();
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);

            cursor.close();
        }
    }

    private void saveTaskDetails() {
        String taskName = taskNameEditText.getText().toString();
        String taskDescription = taskDescriptionEditText.getText().toString();
        String taskDate = taskDateEditText.getText().toString();
        boolean isTaskDone = taskDoneCheckBox.isChecked();
        int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
        int selectedCategoryId = categoryIds.get(selectedCategoryPosition);

        // Update the task in the database
        dbHelper.updateEvent(taskId, taskName, taskDescription, taskDate, "", isTaskDone, selectedCategoryId, priorityTag);

        setResult(Activity.RESULT_OK);

        // Return to the previous screen
        finish();
    }
}