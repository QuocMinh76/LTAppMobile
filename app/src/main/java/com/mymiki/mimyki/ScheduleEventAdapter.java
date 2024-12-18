package com.mymiki.mimyki;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

public class ScheduleEventAdapter extends ArrayAdapter<ScheduleEvent> {
    public ScheduleEventAdapter(@NonNull Context context, List<ScheduleEvent> events) {
        super(context, 0, events);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ScheduleEvent event = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_event_cell, parent, false);
        TextView eventCell = convertView.findViewById(R.id.eventCell);

        String eventTitle = event.getName() + " " + ScheduleUtils.formatteTime(event.getTime());
        eventCell.setText(eventTitle);
        return convertView;
    }
}
