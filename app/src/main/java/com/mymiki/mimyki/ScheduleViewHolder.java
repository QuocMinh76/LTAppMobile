package com.mymiki.mimyki;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class ScheduleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private final ArrayList<LocalDate> days;
    public final TextView daysOfMonth;
    private final ScheduleAdapter.OnItemListener onItemListener;
    public final View parentSchedule;
    public ScheduleViewHolder(@NonNull View itemView, ScheduleAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days)
    {
        super(itemView);
        daysOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        this.parentSchedule = itemView.findViewById(R.id.parentSchedule);
        itemView.setOnClickListener(this);
        this.days = days;
    }

    @Override
    public void onClick(View view) {
        onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
    }
}

