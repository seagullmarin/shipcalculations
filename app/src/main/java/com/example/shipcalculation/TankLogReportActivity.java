package com.example.shipcalculation;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
//import androidx.databinding.library.baseAdapters.BuildConfig;
//import com.example.shipcalculation.BuildConfig; // Sizin paket adınızla başlayan BuildConfig
//import com.example.shipcalculation.BuildConfig;

import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipcalculation.adapters.SoundingLogAdapter;
import com.example.shipcalculation.models.FreshWaterSoundingLog;
import com.example.shipcalculation.viewmodels.FreshWaterViewModel; // ViewModel'ınızın yolu

// iText PDF kütüphanesi için importlar (build.gradle'a eklediğinizden emin olun)
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import com.itextpdf.layout.element.Text;

public class TankLogReportActivity extends AppCompatActivity {

    public static final String EXTRA_TANK_ID = "com.example.shipcalculation.EXTRA_TANK_ID";
    public static final String EXTRA_TANK_NUMBER_OR_NAME = "com.example.shipcalculation.EXTRA_TANK_NUMBER_OR_NAME"; // Daha açıklayıcı bir isim
   // public static final String EXTRA_TANK_NAME = "com.example.shipcalculation.EXTRA_TANK_NAME";
    public static final String EXTRA_TANK_TYPE = "com.example.shipcalculation.EXTRA_TANK_TYPE";

    private TextView textViewTitle;
    private Spinner spinnerLogFilter;
    private RecyclerView recyclerViewLogs;
    private Button buttonGeneratePdf;

    private FreshWaterViewModel freshWaterViewModel;
    private SoundingLogAdapter soundingLogAdapter;
    private int currentTankId = -1;
    private String currentTankName = "TankName";
    private String currentTankType = "";// Yeni değişken
    private String currentTankNumberOrName = "Tank"; // Değişken adını güncelleyelim

    @UnstableApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tank_log_report);

        // Intent'ten gelen tüm ekstraları logla
        Log.d("TankLogReport_DEBUG", "-------------------- INTENT EXTRAS START --------------------");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d("TankLogReport_DEBUG", "Extra: Key='" + key + "', Value='" + (value != null ? value.toString() : "NULL") + "'");
            }
        } else {
            Log.d("TankLogReport_DEBUG", "No extras found in Intent.");
        }
        Log.d("TankLogReport_DEBUG", "-------------------- INTENT EXTRAS END ----------------------");

        textViewTitle = findViewById(R.id.textViewTankLogReportTitle);
        // ... (diğer findViewById çağrıları) ...

        String intentTankNumberOrName = getIntent().getStringExtra(EXTRA_TANK_NUMBER_OR_NAME);
       // String intentTankName = getIntent().getStringExtra(EXTRA_TANK_NAME);
        String intentTankType = getIntent().getStringExtra(EXTRA_TANK_TYPE);
        int intentTankId = getIntent().getIntExtra(EXTRA_TANK_ID, -1);

        Log.d("TankLogReport_DEBUG", "Attempting to retrieve with KEY_ID: '" + EXTRA_TANK_ID + "', KEY_NUMBER_NAME: '" + EXTRA_TANK_NUMBER_OR_NAME + "', KEY_TYPE: '" + EXTRA_TANK_TYPE + "'");
        Log.d("TankLogReport_DEBUG", "Retrieved values - ID: " + intentTankId + ", Number/Name: " + intentTankNumberOrName + ", Type: " + intentTankType);

        textViewTitle = findViewById(R.id.textViewTankLogReportTitle);
        spinnerLogFilter = findViewById(R.id.spinnerLogFilter);
        recyclerViewLogs = findViewById(R.id.recyclerViewTankLogs);
        buttonGeneratePdf = findViewById(R.id.buttonGeneratePdf);


        //String intentTankNumberOrName = getIntent().getStringExtra(EXTRA_TANK_NUMBER_OR_NAME);
       // String intentTankType = getIntent().getStringExtra(EXTRA_TANK_TYPE);
      //  int intentTankId = getIntent().getIntExtra(EXTRA_TANK_ID, -1);

        Log.d("TankLogReport", "onCreate - Received from Intent - ID: " + intentTankId +
                ", Number/Name: " + intentTankNumberOrName + ", Type: " + intentTankType);


        if (getIntent().hasExtra(EXTRA_TANK_ID)) { // Veya intentTankId != -1 kontrolü
            currentTankId = intentTankId;
            currentTankNumberOrName = intentTankNumberOrName != null ? intentTankNumberOrName : "N/A";
            ///currentTankName= intentTankName != null ? intentTankName : "N/A";
            currentTankType = intentTankType != null ? intentTankType : ""; // Boş bırakılabilir veya "N/A"

            String titleText = "Logs for Tank: " + currentTankNumberOrName ;
            if (!currentTankType.isEmpty() && !currentTankType.equalsIgnoreCase("N/A")) {
                titleText += " (" + currentTankType + ")";
            }
            textViewTitle.setText(titleText);

            // PDF oluştururken de bu birleşik ismi veya ayrı ayrı kullanabilirsiniz
            // Örneğin: generateAndSharePdf(currentLogs, currentTankNumberOrName + "_" + currentTankType);

        } else {
            Toast.makeText(this, "Tank information (ID) not found.", Toast.LENGTH_SHORT).show();
            Log.e("TankLogReport", "onCreate - EXTRA_TANK_ID not found. Finishing activity.");
            finish();
            return;
        }

        freshWaterViewModel = new ViewModelProvider(this).get(FreshWaterViewModel.class);
        setupRecyclerView();
        setupSpinner();

        // PDF oluşturma butonunun listener'ında currentTankNumberOrName ve currentTankType'ı
        // generateAndSharePdf metoduna uygun şekilde geçirin.
        buttonGeneratePdf.setOnClickListener(v -> {
            List<FreshWaterSoundingLog> currentLogs = soundingLogAdapter.getCurrentList();
            if (currentLogs != null && !currentLogs.isEmpty()) {
                String pdfFileNameBase = currentTankNumberOrName;
                if (currentTankType != null && !currentTankType.isEmpty()) {
                    pdfFileNameBase += "_" + currentTankType;
                }
                generateAndSharePdf(currentLogs, pdfFileNameBase); // Veya ayrı parametreler olarak
            } else {
                Toast.makeText(this, "No logs to export.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        soundingLogAdapter = new SoundingLogAdapter();
        recyclerViewLogs.setAdapter(soundingLogAdapter);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.log_filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLogFilter.setAdapter(adapter);

        spinnerLogFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                loadLogs(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Başlangıçta ilk filtre seçeneğiyle yükle
        if (spinnerLogFilter.getAdapter().getCount() > 0) {
            loadLogs(spinnerLogFilter.getItemAtPosition(0).toString());
        }
    }

    @UnstableApi
    private void loadLogs(String filter) {
        if (currentTankId == -1) {
            Log.e("TankLogReport", "loadLogs - currentTankId is -1, aborting.");
            return;
        }
        Log.d("TankLogReport", "loadLogs - Attempting to load logs for filter: '" + filter + "' with currentTankId: " + currentTankId);
        if (getString(R.string.log_filter_all).equals(filter)) {
            Log.d("TankLogReport", "loadLogs - Filter: ALL. Calling getSoundingLogsForTank(" + currentTankId + ")");
            freshWaterViewModel.getSoundingLogsForTank(currentTankId).observe(this, logs -> {
                Log.i("TankLogReport", "loadLogs - ALL Filter - Observer triggered. Logs received: " + (logs != null ? logs.size() : "null"));
                if (logs != null && !logs.isEmpty()) {
                    Log.d("TankLogReport", "loadLogs - ALL Filter - First log: " + logs.get(0).toString()); // İlk kaydı logla
                } else if (logs != null && logs.isEmpty()) {
                    Log.w("TankLogReport", "loadLogs - ALL Filter - Received an EMPTY list of logs for tankId: " + currentTankId);
                } else {
                    Log.w("TankLogReport", "loadLogs - ALL Filter - Received NULL logs for tankId: " + currentTankId);
                }
                soundingLogAdapter.submitList(logs);
                Log.d("TankLogReport", "loadLogs - ALL Filter - soundingLogAdapter.submitList called. Adapter item count: " + soundingLogAdapter.getItemCount());

            });
        } else if (getString(R.string.log_filter_last_10).equals(filter)) {
            Log.d("TankLogReport", "loadLogs - Filter: LAST 10. Calling getRecentSoundingLogsForTank(" + currentTankId + ", 10)");
            freshWaterViewModel.getRecentSoundingLogsForTank(currentTankId, 10).observe(this, logs -> {
                Log.i("TankLogReport", "loadLogs - LAST 10 Filter - Observer triggered. Logs received: " + (logs != null ? logs.size() : "null"));
                if (logs != null && !logs.isEmpty()) {
                    Log.d("TankLogReport", "loadLogs - LAST 10 Filter - First log: " + logs.get(0).toString());
                } else if (logs != null && logs.isEmpty()) {
                    Log.w("TankLogReport", "loadLogs - LAST 10 Filter - Received an EMPTY list of logs for tankId: " + currentTankId);
                } else {
                    Log.w("TankLogReport", "loadLogs - LAST 10 Filter - Received NULL logs for tankId: " + currentTankId);
                }
                soundingLogAdapter.submitList(logs);
                Log.d("TankLogReport", "loadLogs - LAST 10 Filter - soundingLogAdapter.submitList called. Adapter item count: " + soundingLogAdapter.getItemCount());
            });
        } else if (getString(R.string.log_filter_last_30_days).equals(filter)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            Date thirtyDaysAgo = calendar.getTime();
            Log.d("TankLogReport", "loadLogs - Filter: LAST 30 DAYS. Calling getSoundingLogsForTankSince(" + currentTankId + ", " + thirtyDaysAgo.toString() + ")");
            freshWaterViewModel.getSoundingLogsForTankSince(currentTankId, thirtyDaysAgo).observe(this, logs -> {
                Log.i("TankLogReport", "loadLogs - LAST 30 DAYS Filter - Observer triggered. Logs received: " + (logs != null ? logs.size() : "null"));
                if (logs != null && !logs.isEmpty()) {
                    Log.d("TankLogReport", "loadLogs - LAST 30 DAYS Filter - First log: " + logs.get(0).toString());
                } else if (logs != null && logs.isEmpty()) {
                    Log.w("TankLogReport", "loadLogs - LAST 30 DAYS Filter - Received an EMPTY list of logs for tankId: " + currentTankId + " since " + thirtyDaysAgo.toString());
                } else {
                    Log.w("TankLogReport", "loadLogs - LAST 30 DAYS Filter - Received NULL logs for tankId: " + currentTankId + " since " + thirtyDaysAgo.toString());
                }
                soundingLogAdapter.submitList(logs);
                Log.d("TankLogReport", "loadLogs - LAST 30 DAYS Filter - soundingLogAdapter.submitList called. Adapter item count: " + soundingLogAdapter.getItemCount());
            });
        } else {
            Log.e("TankLogReport", "loadLogs - Unknown filter selected: " + filter);
        }
        soundingLogAdapter.setOnItemLongClickListener(log -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this log?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        freshWaterViewModel.deleteLog(log);
                        Toast.makeText(this, "Log deleted.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void generateAndSharePdf(List<FreshWaterSoundingLog> logs, String tankNameForPdf) {
        String fileName = "FreshWaterLog_" + tankNameForPdf.replace(" ", "_").replace("/", "-") + "_" + new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
//String fileName = "FreshWaterReport_" + new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        // Scoped Storage için uygulamanın cache dizinini kullanmak paylaşım için daha uygundur.
        File outputDir = new File(getCacheDir(), "reports"); // "reports" adında bir alt klasör
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Klasör yoksa oluştur
        }
        File pdfFile = new File(outputDir, fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Font ayarı: Bold yazı için
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Başlıklar
            Paragraph title = new Paragraph("Fresh Water Sounding Log")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph tankName = new Paragraph("Tank: " + tankNameForPdf)
                    .setFont(normalFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(tankName);

            Paragraph generatedDate = new Paragraph("Report Generated: " +
                    new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(generatedDate);

            // Tablo
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Tablo başlık hücreleri
            table.addHeaderCell(new Paragraph("Date/Time").setFont(boldFont));
            table.addHeaderCell(new Paragraph("Sounding (m)").setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));
            table.addHeaderCell(new Paragraph("Volume (m³)").setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));

            // Veri satırları
            SimpleDateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            for (FreshWaterSoundingLog log : logs) {
                table.addCell(new Paragraph(logDateFormat.format(log.getMeasurementTime())).setFont(normalFont));
                table.addCell(new Paragraph(String.format(Locale.getDefault(), "%.2f", log.getSounding())).setFont(normalFont).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Paragraph(String.format(Locale.getDefault(), "%.2f", log.getCalculatedVolume())).setFont(normalFont).setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);
            document.close();

            // PDF'i Paylaş
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider", // Manifest'teki authority ile aynı olmalı
                    pdfFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Fresh Water Log Report: " + tankNameForPdf);

            startActivity(Intent.createChooser(shareIntent, "Share Report via"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to share the PDF.", Toast.LENGTH_SHORT).show();
        }
    }
}