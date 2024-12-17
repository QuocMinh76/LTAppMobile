package com.mymiki.mimyki;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    public ScheduleAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener)
    {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.schedule_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (days.size() > 15)
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        else
            layoutParams.height = (int) parent.getHeight();
        return new ScheduleViewHolder(view, onItemListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position)
    {
        final LocalDate date = days.get(position);
        if(date == null)
            holder.daysOfMonth.setText("");
        else
        {
            holder.daysOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            if(date.equals(ScheduleUtils.selectDate))
                holder.parentSchedule.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount()
    {
        return days.size();
    }

    public interface OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}
