package com.example.shipcalculation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LogListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LogAdapter adapter;
    LinkedHashMap<String, List<LogEntry>> groupedData = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);

        recyclerView = findViewById(R.id.recyclerLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCsv();
        adapter = new LogAdapter(groupedData, this::confirmDeleteDay);
        recyclerView.setAdapter(adapter);
    }

    private void loadCsv() {
        groupedData.clear();
        File file = new File(getExternalFilesDir(null), "gps_log.csv");

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 5) continue;

               // String date = parts[0].substring(0, 10);   // YYYY-MM-DD
                String[] d = parts[0].substring(0, 10).split("-");
                String date = d[2] + "." + d[1] + "." + d[0];  // DD-MM-YYYY

                String time = parts[0].substring(11, 16);  // HH:mm

                LogEntry entry = new LogEntry(
                        date,
                        time,
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4]
                );

                if (!groupedData.containsKey(date)) {
                    groupedData.put(date, new ArrayList<>());
                }
                groupedData.get(date).add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmDeleteDay(String date) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Logs for " + date)
                .setMessage("Bu güne ait tüm kayıtlar silinsin mi?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDay(date))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDay(String date) {
        File file = new File(getExternalFilesDir(null), "gps_log.csv");
        if (!file.exists()) return;

        try {
            List<String> newLines = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(date)) {
                        newLines.add(line);
                    }
                }
            }

            try (FileWriter fw = new FileWriter(file, false)) {
                for (String l : newLines) fw.write(l + "\n");
            }

            Toast.makeText(this, date + " kayıtları silindi", Toast.LENGTH_SHORT).show();

            loadCsv();
            adapter.updateData(groupedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
