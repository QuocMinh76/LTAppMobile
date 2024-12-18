package com.mymiki.mimyki;

import static com.mymiki.mimyki.ScheduleUtils.selectDate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailyScheduleActivity extends AppCompatActivity {
    private TextView monthDayText;
    private TextView dayOfWeekT;
    private ListView hourListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_schedule);
        initWidgets();
    }

    private void initWidgets() {
        monthDayText = findViewById(R.id.monthDay);
        dayOfWeekT = findViewById(R.id.dayOfWeekT);
        hourListView = findViewById(R.id.hourListView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        setDayView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setDayView() {
        monthDayText.setText(ScheduleUtils.monthDayFromDate(selectDate));
        String dayOfWeek = selectDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        dayOfWeekT.setText(dayOfWeek);
        setHourAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setHourAdapter() {
        ScheduleHourAdapter scheduleHourAdapter = new ScheduleHourAdapter(getApplicationContext(), hourEventList());
        hourListView.setAdapter(scheduleHourAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<ScheduleHourEvent> hourEventList() {
        ArrayList<ScheduleHourEvent> list = new ArrayList<>();
        for(int hour = 0; hour < 24; hour++)
        {
            LocalTime time = LocalTime.of(hour, 0);
            ArrayList<ScheduleEvent> events = ScheduleEvent.eventsForDateAndTime(selectDate, time);
            ScheduleHourEvent scheduleHourEvent = new ScheduleHourEvent(time, events);
            list.add(scheduleHourEvent);
        }
        return list;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void previousDayAction(View view) {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.minusDays(1);
        setDayView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void nextDayAction(View view) {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.plusDays(1);
        setDayView();
    }

    public void newEventAction(View view) {
        startActivity(new Intent(this, ScheduleEventActivity.class));

    }
}