package com.example.shipcalculation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private final OnDayDeleteListener deleteListener;

    public interface OnDayDeleteListener {
        void onDeleteDay(String date);
    }

    public LogAdapter(LinkedHashMap<String, List<LogEntry>> groupedData, OnDayDeleteListener listener) {
        this.deleteListener = listener;
        buildList(groupedData);
    }

    private void buildList(LinkedHashMap<String, List<LogEntry>> groupedData) {
        items.clear();
        for (String date : groupedData.keySet()) {
            items.add(date);
            items.addAll(groupedData.get(date));
        }
    }

    public void updateData(LinkedHashMap<String, List<LogEntry>> newData) {
        buildList(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_DATE : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        if (type == TYPE_DATE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.date_header, parent, false);
            return new DateHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.log_item, parent, false);
            return new LogHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
        if (h instanceof DateHolder) {
            String date = (String) items.get(pos);
            ((DateHolder) h).bind(date, deleteListener);
        } else {
            LogEntry e = (LogEntry) items.get(pos);
            ((LogHolder) h).bind(e);
        }
    }

    static class DateHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        Button btnDeleteDay;

        DateHolder(View v) {
            super(v);
            txtDate = v.findViewById(R.id.txtDateHeader);
            btnDeleteDay = v.findViewById(R.id.btnDeleteDay);
        }

        void bind(String date, OnDayDeleteListener listener) {
            txtDate.setText("📅 " + date);
            btnDeleteDay.setOnClickListener(v -> listener.onDeleteDay(date));
        }
    }

    static class LogHolder extends RecyclerView.ViewHolder {
        TextView t1, t2, t3, t4, t5;

        LogHolder(View v) {
            super(v);
            t1 = v.findViewById(R.id.colTime);
            t2 = v.findViewById(R.id.colLat);
            t3 = v.findViewById(R.id.colLon);
            t4 = v.findViewById(R.id.colCog);
            t5 = v.findViewById(R.id.colSog);
        }

        void bind(LogEntry e) {
            t1.setText(e.time);
            t2.setText(e.lat);
            t3.setText(e.lon);
            t4.setText(e.cog);
            t5.setText(e.sog);
        }
    }
}
