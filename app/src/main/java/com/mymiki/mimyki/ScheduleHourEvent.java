package com.mymiki.mimyki;

import java.time.LocalTime;
import java.util.ArrayList;

public class ScheduleHourEvent {
    LocalTime time;
    ArrayList<ScheduleEvent> events;

    public ScheduleHourEvent(LocalTime time, ArrayList<ScheduleEvent> events) {
        this.time = time;
        this.events = events;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public ArrayList<ScheduleEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ScheduleEvent> events) {
        this.events = events;
    }
}
