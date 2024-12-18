package com.mymiki.mimyki;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatrixFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatrixFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FloatingActionButton fabAddTask;
    ListView listQuadrant1, listQuadrant2, listQuadrant3, listQuadrant4;
    ArrayList<String> quadrant1Tasks, quadrant2Tasks, quadrant3Tasks, quadrant4Tasks;
    ArrayAdapter<String> quadrant1Adapter, quadrant2Adapter, quadrant3Adapter, quadrant4Adapter;

    public MatrixFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MatrixFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MatrixFragment newInstance(String param1, String param2) {
        MatrixFragment fragment = new MatrixFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_matrix, container, false);

        fabAddTask = rootView.findViewById(R.id.fab_add_task);
        listQuadrant1 = rootView.findViewById(R.id.list_quadrant1);
        listQuadrant2 = rootView.findViewById(R.id.list_quadrant2);
        listQuadrant3 = rootView.findViewById(R.id.list_quadrant3);
        listQuadrant4 = rootView.findViewById(R.id.list_quadrant4);

        quadrant1Tasks = new ArrayList<>();
        quadrant2Tasks = new ArrayList<>();
        quadrant3Tasks = new ArrayList<>();
        quadrant4Tasks = new ArrayList<>();

        quadrant1Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant1Tasks);
        quadrant2Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant2Tasks);
        quadrant3Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant3Tasks);
        quadrant4Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, quadrant4Tasks);

        listQuadrant1.setAdapter(quadrant1Adapter);
        listQuadrant2.setAdapter(quadrant2Adapter);
        listQuadrant3.setAdapter(quadrant3Adapter);
        listQuadrant4.setAdapter(quadrant4Adapter);

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        return rootView;
    }

    private void showAddTaskDialog() {
        CharSequence[] items = {"Khẩn Cấp", "Quan Trọng", "Bình Thường", "Khác"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Chọn trạng thái công việc")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String task = "Công việc mới";

                        switch (which) {
                            case 0: // Khẩn Cấp
                                quadrant1Tasks.add(task);
                                quadrant1Adapter.notifyDataSetChanged();
                                break;
                            case 1: // Quan Trọng
                                quadrant2Tasks.add(task);
                                quadrant2Adapter.notifyDataSetChanged();
                                break;
                            case 2: // Bình Thường
                                quadrant3Tasks.add(task);
                                quadrant3Adapter.notifyDataSetChanged();
                                break;
                            case 3: // Khác
                                quadrant4Tasks.add(task);
                                quadrant4Adapter.notifyDataSetChanged();
                                break;
                        }
                        Toast.makeText(getContext(), "Đã thêm công việc", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }
}