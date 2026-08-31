package com.example.shipcalculation;

public class LogEntry {
    public String date, time, lat, lon, cog, sog;

    public LogEntry(String date, String time, String lat, String lon, String cog, String sog) {
        this.date = date;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
        this.cog = cog;
        this.sog = sog;
    }
}
