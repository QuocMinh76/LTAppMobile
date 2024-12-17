package com.mymiki.mimyki;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectionTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_section_task);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.section_task_page), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // FloatingActionButton for adding tasks
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        //Button addTask = findViewById(R.id.btn_add);

        // Main container in activity_main.xml
        LinearLayout mainContainer = findViewById(R.id.main_container);

        // Example data
        Map<String, List<String>> groups = new HashMap<>();
        groups.put("Work", Arrays.asList("Prepare presentation", "Email clients"));
        groups.put("Personal", Arrays.asList("Buy groceries", "Go for a run"));
        groups.put("Chores", Arrays.asList("Clean the house", "Wash the car", "Fix the sink"));

        // Dynamically create groups and tasks
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String groupTitle = entry.getKey();
            List<String> tasks = entry.getValue();

            // Inflate group_item.xml
            LayoutInflater inflater = LayoutInflater.from(this);
            View groupView = inflater.inflate(R.layout.list_item_task_group, mainContainer, false);

            // Set group title
            TextView groupTitleView = groupView.findViewById(R.id.task_group_title);
            groupTitleView.setText(groupTitle);

            // Get the task container within the group
            LinearLayout taskContainer = groupView.findViewById(R.id.task_container);

            // Dynamically add tasks to the group
            for (String task : tasks) {
                // Inflate task_item.xml
                View taskView = inflater.inflate(R.layout.list_item_task, taskContainer, false);

                // Set task details
                TextView taskDescription = taskView.findViewById(R.id.list_item_task_description);
                taskDescription.setText(task);

                TextView taskDate = taskView.findViewById(R.id.list_item_task_date);
                taskDate.setText("Today"); // Example date

                CheckBox taskCheckbox = taskView.findViewById(R.id.list_item_task_checkbox_done);
                taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    // Handle checkbox changes (optional)
                });

                // Add the task view to the task container
                taskContainer.addView(taskView);
            }

            // Add the group view to the main container
            mainContainer.addView(groupView);
        }

        // Set a click listener for the FAB
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch a new Activity to add a task or show a dialog to add a task
                Intent intent = new Intent(SectionTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}