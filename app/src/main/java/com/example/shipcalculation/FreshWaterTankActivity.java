package com.example.shipcalculation; // Kendi paket adınız

import static androidx.media3.common.MediaLibraryInfo.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipcalculation.adapters.FreshWaterAdapter;
import com.example.shipcalculation.models.FreshWaterRecord;
import com.example.shipcalculation.viewmodels.FreshWaterViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public  class FreshWaterTankActivity extends AppCompatActivity implements FreshWaterAdapter.OnItemClickListener {
        FrameLayout frameLayout;
    private FreshWaterViewModel freshWaterViewModel;
    private FreshWaterAdapter freshWaterAdapter;
    private RecyclerView recyclerViewFreshWaterTanks;
    private TextView textViewTotalVolume;

    private TextView textViewTotalCapacity;
    private FloatingActionButton fabAddFreshWaterTank, fabCreatePdf; // Yeni tank ekleme butonu

    // CreateFreshWaterTankActivity'den sonuç almak için Request Code
    public static final int ADD_FRESH_WATER_REQUEST = 1;
    public static final int EDIT_FRESH_WATER_REQUEST = 2;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_fresh_water_tanks); // RecyclerView'ı içeren layout dosyanızın adı
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout = findViewById(R.id.frameLayout);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);
        textViewTotalVolume = findViewById(R.id.textViewTotalVolume);

        textViewTotalCapacity = findViewById(R.id.textViewTotalCapacity);

        recyclerViewFreshWaterTanks = findViewById(R.id.recyclerViewFreshWaterTanks);


        recyclerViewFreshWaterTanks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFreshWaterTanks.setHasFixedSize(true);

        freshWaterAdapter = new FreshWaterAdapter(this, this); // 'this' listener olarak Activity'yi veriyor
        recyclerViewFreshWaterTanks.setAdapter(freshWaterAdapter);

        // ViewModel'ı al
        freshWaterViewModel = new ViewModelProvider(this).get(FreshWaterViewModel.class);

        // Tüm balast kayıtlarını gözlemle
        freshWaterViewModel.getAllFreshWaterRecords().observe(this, freshWaterRecords -> {
            freshWaterAdapter.submitList(freshWaterRecords);
            // İsteğe bağlı: Liste boşsa bir mesaj gösterilebilir
        });

        // Toplam hacmi gözlemle
        freshWaterViewModel.getTotalVolume().observe(this, totalVolume -> {
            if (totalVolume != null) {
                textViewTotalVolume.setText(String.format(Locale.getDefault(), "Total Volume in Tanks: %.3f m³", totalVolume));
            } else {
                textViewTotalVolume.setText("Total Volume in Tanks: 0.00 m³");
            }
        });


// Toplam kapasite gözlemle
        freshWaterViewModel.getTotalCapacity().observe(this, totalCapacity -> {
            if (totalCapacity != null) {
                textViewTotalCapacity.setText(String.format(Locale.getDefault(), "Total Capacity: %.3f m³", totalCapacity));
            } else {
                textViewTotalCapacity.setText("Total Weight in Tanks: 0.00 MT");
            }
        });
        // Yeni tank ekleme butonu

        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_add_fw_tank, R.drawable.ic_add_tank)
                        .setLabel("Add Tank")
                        .setLabelColor(ContextCompat.getColor(this, R.color.white)) // Yazı rengi
                        .setLabelBackgroundColor(ContextCompat.getColor(this, R.color.gray)) // Arka plan
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Buton arka planı
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.black)) // Buton ikon rengi
                        .create());

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_create_pdf, R.drawable.ic_save_pdf)
                        .setLabel("Export PDF")
                        .setLabelColor(ContextCompat.getColor(this, R.color.white)) // Yazı rengi
                        .setLabelBackgroundColor(ContextCompat.getColor(this, R.color.gray)) // Arka plan
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.gray)) // Buton arka planı
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.black)) // Buton ikon rengi
                        .create());

        speedDialView.setOnActionSelectedListener(actionItem -> {
            int id = actionItem.getId();

            if (id == R.id.fab_add_fw_tank) {
                startActivity(new Intent(this, CreateFreshWaterTankActivity.class));
                return false;
            } else if (id == R.id.fab_create_pdf) {
                createAndSharePdfReport();
                return false;
            } else {
                return false;
            }
        });
    }
    @Override
    public void onItemClick(FreshWaterRecord freshWaterRecord) {
        // Bu metoda tıklandığında ne olacağını buraya yazın.
        // Örneğin, bir tank detay sayfasına gidebilir veya bir Toast mesajı gösterebilirsiniz.
        // Şimdilik boş bırakabilir veya basit bir Toast ekleyebilirsiniz:
        Toast.makeText(this, "Clicked on: " + freshWaterRecord.getFreshWaterTankNo(), Toast.LENGTH_SHORT).show();

        // Eğer bu tıklama için özel bir işleviniz yoksa ve sadece diğer butonlar
        // (edit, delete, create report) için listener kullanıyorsanız,
        // bu metodu boş bırakmak da bir seçenektir. Ancak yine de tanımlanmış olmalı.
    }


    @Override
    public void onEditClick(FreshWaterRecord freshWaterRecord) {
        Intent intent = new Intent(FreshWaterTankActivity.this, CreateFreshWaterTankActivity.class);
        intent.putExtra(CreateFreshWaterTankActivity.EXTRA_FRESH_WATER_ID, freshWaterRecord.getId());
        // Diğer verileri de intent ile gönderebilirsiniz, veya CreateFreshWaterTankActivity ID'yi alıp
        // ViewModel üzerinden veriyi çekebilir. Basitlik için sadece ID gönderelim.
        // startActivityForResult(intent, EDIT_FRESH_WATER_REQUEST);
        startActivity(intent);
        Toast.makeText(this, "Editing: " + freshWaterRecord.getFreshWaterTankNo()+freshWaterRecord.getFreshWaterTankType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(final FreshWaterRecord freshWaterRecord) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tank")
                .setMessage("Are you sure you want to delete tank '" + freshWaterRecord.getFreshWaterTankNo() +freshWaterRecord.getFreshWaterTankType() + "'?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        freshWaterViewModel.delete(freshWaterRecord);
                        Toast.makeText(FreshWaterTankActivity.this, "Tank deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
// ... implements FreshWaterAdapter.OnItemClickListener ...

    @Override
    public void onCreateReportClick(FreshWaterRecord freshWaterRecord) {
        Intent intent = new Intent(FreshWaterTankActivity.this, TankLogReportActivity.class);
        intent.putExtra(TankLogReportActivity.EXTRA_TANK_ID, freshWaterRecord.getId());
        intent.putExtra(TankLogReportActivity.EXTRA_TANK_NUMBER_OR_NAME, freshWaterRecord.getFreshWaterTankNo());
        intent.putExtra(TankLogReportActivity.EXTRA_TANK_TYPE, freshWaterRecord.getFreshWaterTankType());
        //intent.putExtra(TankLogReportActivity.EXTRA_TANK_NAME, freshWaterRecord.getFreshWaterTankNo());
        startActivity(intent);
    }
    // ...
    @OptIn(markerClass = UnstableApi.class)
    private void createAndSharePdfReport() {
        List<FreshWaterRecord> currentFreshWaterRecords = freshWaterAdapter.getCurrentList();

        if (currentFreshWaterRecords == null || currentFreshWaterRecords.isEmpty()) {
            Toast.makeText(this, "No data to create PDF.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "PDF creation aborted: No data in freshWaterAdapter.");
            return;
        }
        Log.d(TAG, "Creating PDF with " + currentFreshWaterRecords.size() + " records.");

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint tableHeaderPaint = new Paint();
        Paint tableTextPaint = new Paint();

        // Sayfa bilgileri (A4 boyutuna yakın)
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // --- Rapor Başlığı ve Tarih ---
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Fresh Water Report", pageWidth / 2f, margin + 20, titlePaint);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        paint.setTextSize(10f);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Date: " + currentDate, pageWidth - margin, margin + 40, paint);
        paint.setTextAlign(Paint.Align.LEFT); // Sonraki çizimler için sola hizala

        // --- Tablo Çizimi ---
        float currentY = margin + 70; // Başlangıç Y pozisyonu
        float lineHeight = 20f; // Satır yüksekliği
        float padding = 5f;

        // Tablo Başlıkları
        tableHeaderPaint.setTextSize(10f);
        tableHeaderPaint.setFakeBoldText(true);
        tableHeaderPaint.setColor(Color.BLACK);

        // --- Sütun pozisyonlarını ayarla ---
        float col1X = margin;
        float col2X = margin + 200; // Sounding sütunu
        float col3X = margin + 300; // Volume sütunu

        canvas.drawText("Tank No/Type", col1X, currentY, tableHeaderPaint);
        canvas.drawText("Sounding (m)", col2X, currentY, tableHeaderPaint);
        canvas.drawText("Volume (m³)", col3X, currentY, tableHeaderPaint);
        // Density ve Weight başlıkları kaldırıldı.

        currentY += lineHeight;
        canvas.drawLine(margin, currentY - padding, col3X + 100, currentY - padding, paint); // Başlık altı çizgisi (kısaltıldı)

        // Tablo Verileri
        tableTextPaint.setTextSize(10f);
        tableTextPaint.setColor(Color.DKGRAY);
        double totalVolume = 0;

        for (FreshWaterRecord record : currentFreshWaterRecords) {
            currentY += lineHeight;
            canvas.drawText(record.getFreshWaterTankNo() + " (" + record.getFreshWaterTankType() + ")", col1X, currentY, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.2f", record.getFreshWaterTankSounding()), col2X, currentY, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.3f", record.getFreshWaterTankVolume()), col3X, currentY, tableTextPaint); // Hacim için 3 ondalık
            // Density ve Weight verileri kaldırıldı.

            totalVolume += record.getFreshWaterTankVolume();
        }
        currentY += padding;
        canvas.drawLine(margin, currentY, col3X + 100, currentY, paint); // Son kayıt altı çizgisi (kısaltıldı)

        // --- Toplamlar ---
        currentY += lineHeight * 2; // Biraz boşluk bırak
        paint.setTextSize(12f);
        paint.setFakeBoldText(true);
        String totalVolumeStr = String.format(Locale.US, "Total Volume in Tanks: %.3f m³", totalVolume);
        // Total Weight kaldırıldı.

        // Metni sağa hizalamak için genişliğini ölç
        float totalVolumeWidth = paint.measureText(totalVolumeStr);
        canvas.drawText(totalVolumeStr, pageWidth - margin - totalVolumeWidth, currentY, paint);
        // Total Weight çizimi kaldırıldı.

        pdfDocument.finishPage(page);
        File pdfFile = savePdfToFile(pdfDocument);

        if (pdfFile != null) {
            sharePdf(pdfFile);
        } else {
            Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }



    @UnstableApi
    private File savePdfToFile(PdfDocument pdfDocument) {
        File outputDir = new File(getCacheDir(), "pdf_fresh_water_reports"); // "pdf_reports" -> "pdf_freshWater_reports" olarak değiştirildi
        Log.d(TAG, "Cache Directory: " + getCacheDir().getAbsolutePath());
        Log.d(TAG, "Output Directory for PDF: " + outputDir.getAbsolutePath());
        if (!outputDir.exists()) {
            boolean success = outputDir.mkdirs();
            Log.d(TAG, "Output directory (" + outputDir.getName() + ") created: " + success);
        }
       // String filename = "FreshWaterWaterReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        String fileName = "FreshWaterReport_" + new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File file = new File(outputDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdfDocument.writeTo(fos);
            Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "PDF saved successfully to: " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void sharePdf(File pdfFile) {
        // FileProvider kullanarak URI oluştur
        Uri pdfUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider", // Manifest'te tanımladığınız authority
                pdfFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // URI okuma izni ver

        // Kullanıcının seçebileceği uygulamaları göster
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share PDF Report via");
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        } else {
            Toast.makeText(this, "No app found to share PDF", Toast.LENGTH_SHORT).show();
        }
    }
}

