package com.mymiki.mimyki;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScheduleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView daysOfMonth;
    private final ScheduleAdapter.OnItemListener onItemListener;
    public final View parentSchedule;
    public ScheduleViewHolder(@NonNull View itemView, ScheduleAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        daysOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        this.parentSchedule = itemView.findViewById(R.id.parentSchedule);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemListener.onItemClick(getAdapterPosition(), (String) daysOfMonth.getText());
    }
}

