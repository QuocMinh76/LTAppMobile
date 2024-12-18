package com.mymiki.mimyki;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);

        // FloatingActionButton for adding tasks
        FloatingActionButton fabAddTask = rootView.findViewById(R.id.fab_add_task);
        //Button addTask = findViewById(R.id.btn_add);

        // Main container in activity_main.xml
        LinearLayout mainContainer = rootView.findViewById(R.id.main_container);

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
            LayoutInflater inflater1 = LayoutInflater.from(getContext());
            View groupView = inflater1.inflate(R.layout.list_item_task_group, mainContainer, false);

            // Set group title
            TextView groupTitleView = groupView.findViewById(R.id.task_group_title);
            groupTitleView.setText(groupTitle);

            // Get the task container within the group
            LinearLayout taskContainer = groupView.findViewById(R.id.task_container);

            // Dynamically add tasks to the group
            for (String task : tasks) {
                // Inflate task_item.xml
                View taskView = inflater1.inflate(R.layout.list_item_task, taskContainer, false);

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
                Toast.makeText(getContext(), "Testing", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}