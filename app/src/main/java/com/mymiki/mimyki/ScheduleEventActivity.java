package com.mymiki.mimyki;

import android.media.metrics.Event;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleEventActivity extends AppCompatActivity
{
    private EditText eventNameT;
    private TextView eventDate, eventTime;
    private LocalTime time;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_event);
        initWidgets();
        time = LocalTime.now();
        eventDate.setText("Date: " + ScheduleUtils.formatteDate(ScheduleUtils.selectDate));
        eventTime.setText("Time: " + ScheduleUtils.formatteTime(time));
    }

    private void initWidgets() {
        eventNameT = findViewById(R.id.eventNameT);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void saveEventAction(View view)
    {
        String eventName = eventNameT.getText().toString();
        ScheduleEvent newEvent = new ScheduleEvent(eventName, ScheduleUtils.selectDate, time);
        ScheduleEvent.eventsList.add(newEvent);
        finish();
    }
}