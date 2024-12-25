package com.mymiki.mimyki;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleHourAdapter extends ArrayAdapter<ScheduleHourEvent> {
    public ScheduleHourAdapter(@NonNull Context context, List<ScheduleHourEvent> hourEvents) {
        super(context, 0, hourEvents);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ScheduleHourEvent event = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_hour_cell, parent, false);
        setHour(convertView, event.time);
        setEvents(convertView, event.events);
        return convertView;
    }

    private void setEvents(View convertView, ArrayList<ScheduleEvent> events) {
        LinearLayout eventContainer = convertView.findViewById(R.id.eventContainer);

        eventContainer.removeAllViews();

        for (ScheduleEvent event : events) {
            TextView eventView = new TextView(getContext());

            String eventName = event.getName();
            if (eventName.length() > 15) {
                eventName = eventName.substring(0, 10) + "...";
            }
            eventView.setText(eventName);

            eventView.setPadding(24, 16, 24, 16);
            eventView.setTextSize(16);
            eventView.setTextColor(Color.BLACK);
            eventView.setTypeface(null, Typeface.BOLD);
            eventView.setGravity(Gravity.CENTER);

            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#e1c8f7"));
            background.setCornerRadius(50);
            background.setStroke(2, Color.BLACK);
            eventView.setBackground(background);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(16, 8, 16, 8);
            eventView.setLayoutParams(layoutParams);

            eventContainer.addView(eventView);
        }

        if (events.isEmpty()) {
            TextView noEventView = new TextView(getContext());
            noEventView.setText("No Events");
            noEventView.setPadding(16, 8, 16, 8);
            noEventView.setTextSize(14);
            noEventView.setTextColor(Color.GRAY);
            eventContainer.addView(noEventView);
        }
    }

    private void setEvent(TextView textView, ScheduleEvent scheduleEvent) {
        textView.setText(scheduleEvent.getName());
        textView.setVisibility(View.VISIBLE);
    }

    private void hideEvent(TextView tv) {
        tv.setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setHour(View convertView, LocalTime time) {
        TextView timeT = convertView.findViewById(R.id.timeT);
        timeT.setText(ScheduleUtils.formatteShortTime(time));
    }

}
