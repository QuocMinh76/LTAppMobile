package com.mymiki.mimyki;

import static com.mymiki.mimyki.ScheduleUtils.daysInMonthArray;
import static com.mymiki.mimyki.ScheduleUtils.monthYearFromDate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

public class ScheduleTaskActivity extends AppCompatActivity implements ScheduleAdapter.OnItemListener
{
    private TextView monthYearText;
    private RecyclerView scheduleRecycleView;
    private TemporalAccessor date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_task);
        initWidgets();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ScheduleUtils.selectDate = LocalDate.now();
            setMonthView();
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.schedule), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(ScheduleUtils.selectDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(ScheduleUtils.selectDate);
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),7);
        scheduleRecycleView.setLayoutManager(layoutManager);
        scheduleRecycleView.setAdapter(scheduleAdapter);
    }

    private void initWidgets()
    {
        scheduleRecycleView = findViewById(R.id.scheduleRecycleView);
        monthYearText = findViewById(R.id.monthYear);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void previousMontAction(View view)
    {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.minusMonths(1);
        setMonthView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void nextMontAction(View view)
    {
        ScheduleUtils.selectDate = ScheduleUtils.selectDate.plusMonths(1);
        setMonthView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemClick(int position, String dayText) {
        if (dayText != null && !dayText.equals("")) {
            String message = "Selected Date: " + dayText + " " + monthYearFromDate(ScheduleUtils.selectDate);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            String message = "No date selected.";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void weeklyAction(View view)
    {
        startActivity(new Intent(this, WeekScheduleActivity.class));
    }
}