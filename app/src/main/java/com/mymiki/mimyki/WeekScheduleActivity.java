package com.mymiki.mimyki;

import static com.mymiki.mimyki.ScheduleUtils.daysInWeekArray;
import static com.mymiki.mimyki.ScheduleUtils.monthYearFromDate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class WeekScheduleActivity extends AppCompatActivity implements ScheduleAdapter.OnItemListener{
    private TextView monthYearText;
    private RecyclerView scheduleRecycleView;
    private ListView eventListView;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_week_schedule);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        initWidgets();
        setWeekView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeekView()
    {
        monthYearText.setText(monthYearFromDate(ScheduleUtils.selectDate));
        ArrayList<LocalDate> days = daysInWeekArray(ScheduleUtils.selectDate);
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(days, (ScheduleAdapter.OnItemListener) this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),7);
        scheduleRecycleView.setLayoutManager(layoutManager);
        scheduleRecycleView.setAdapter(scheduleAdapter);
        setEventAdapter();
    }

    private void initWidgets()
    {
        scheduleRecycleView = findViewById(R.id.scheduleRecycleView);
        monthYearText = findViewById(R.id.monthYear);
        eventListView = findViewById(R.id.eventListView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void previousWeekAction(View view)
    {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.minusWeeks(1);
        setWeekView();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void nextWeekAction(View view)
    {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.plusWeeks(1);
        setWeekView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onItemClick(int position, LocalDate date) {
        ScheduleUtils.selectDate = date;
        setWeekView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEventAdapter();
    }

    private void setEventAdapter() {
        ArrayList<ScheduleEvent> dailyEvents = ScheduleEvent.eventsForDate(ScheduleUtils.selectDate);
        ScheduleEventAdapter scheduleEventAdapter = new ScheduleEventAdapter(getApplicationContext(), dailyEvents);
        eventListView.setAdapter(scheduleEventAdapter);
    }

    public void newEventAction(View view) {
        startActivity(new Intent(this, ScheduleEventActivity.class));
    }
}