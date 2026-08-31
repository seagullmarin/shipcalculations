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
import android.view.View;
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
import com.example.shipcalculation.adapters.BallastAdapter;
import com.example.shipcalculation.models.BallastRecord;
import com.example.shipcalculation.viewmodels.BallastViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Yeni tank ekleme butonu için
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BallastTankActivity extends AppCompatActivity implements BallastAdapter.OnItemClickListener {

    FrameLayout frameLayout;

    private BallastViewModel ballastViewModel;
    private BallastAdapter ballastAdapter;
    private RecyclerView recyclerViewBallastTanks;
    private TextView textViewTotalVolume;
    private TextView textViewTotalWeight;
    private TextView textViewTotalCapacity;
    private FloatingActionButton fabAddBallastTank, fabCreatePdf; // Yeni tank ekleme butonu

    // CreateBallastTankActivity'den sonuç almak için Request Code
    public static final int ADD_BALLAST_REQUEST = 1;
    public static final int EDIT_BALLAST_REQUEST = 2;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballast_tanks); // RecyclerView'ı içeren layout dosyanızın adı
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        frameLayout = findViewById(R.id.frameLayout);

        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);


        textViewTotalVolume = findViewById(R.id.textViewTotalVolume);
        textViewTotalWeight = findViewById(R.id.textViewTotalWeight);
        textViewTotalCapacity = findViewById(R.id.textViewTotalCapacity);

        recyclerViewBallastTanks = findViewById(R.id.recyclerViewBallastTanks);


        recyclerViewBallastTanks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBallastTanks.setHasFixedSize(true);

        ballastAdapter = new BallastAdapter(this, this); // 'this' listener olarak Activity'yi veriyor
        recyclerViewBallastTanks.setAdapter(ballastAdapter);

        // ViewModel'ı al
        ballastViewModel = new ViewModelProvider(this).get(BallastViewModel.class);

        // Tüm balast kayıtlarını gözlemle
        ballastViewModel.getAllBallastRecords().observe(this, ballastRecords -> {
            ballastAdapter.submitList(ballastRecords);
            // İsteğe bağlı: Liste boşsa bir mesaj gösterilebilir
        });


        // Toplam hacmi gözlemle
        ballastViewModel.getTotalVolume().observe(this, totalVolume -> {
            if (totalVolume != null) {
                textViewTotalVolume.setText(String.format(Locale.getDefault(), "Total Volume in Tanks: %.3f m³", totalVolume));
            } else {
                textViewTotalVolume.setText("Total Volume in Tanks: 0.00 m³");
            }
        });

        // Toplam ağırlığı gözlemle
        ballastViewModel.getTotalWeight().observe(this, totalWeight -> {
            if (totalWeight != null) {
                textViewTotalWeight.setText(String.format(Locale.getDefault(), "Total Weight in Tanks: %.3f MT", totalWeight));
            } else {
                textViewTotalWeight.setText("Total Weight in Tanks: 0.00 MT");
            }
        });
// Toplam kapasite gözlemle
        ballastViewModel.getTotalCapacity().observe(this, totalCapacity -> {
            if (totalCapacity != null) {
                textViewTotalCapacity.setText(String.format(Locale.getDefault(), "Total Capacity: %.3f m³", totalCapacity));
            } else {
                textViewTotalCapacity.setText("Total Weight in Tanks: 0.00 MT");
            }
        });
        // Yeni tank ekleme butonu

        SpeedDialView speedDialView = findViewById(R.id.speedDial);


        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_add_ballast_tank, R.drawable.ic_add_tank)
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
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Buton arka planı
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.black)) // Buton ikon rengi
                        .create());

        speedDialView.setOnActionSelectedListener(actionItem -> {
            int id = actionItem.getId();

            if (id == R.id.fab_add_ballast_tank) {
                startActivity(new Intent(this, CreateBallastTankActivity.class));
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
    public void onEditClick(BallastRecord ballastRecord) {
        Intent intent = new Intent(BallastTankActivity.this, CreateBallastTankActivity.class);
        intent.putExtra(CreateBallastTankActivity.EXTRA_BALLAST_ID, ballastRecord.getId());
        // Diğer verileri de intent ile gönderebilirsiniz, veya CreateBallastTankActivity ID'yi alıp
        // ViewModel üzerinden veriyi çekebilir. Basitlik için sadece ID gönderelim.
        // startActivityForResult(intent, EDIT_BALLAST_REQUEST);
        startActivity(intent);
        Toast.makeText(this, "Editing: " + ballastRecord.getBallastTankNo()+ballastRecord.getBallastTankType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(final BallastRecord ballastRecord) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tank")
                .setMessage("Are you sure you want to delete tank '" + ballastRecord.getBallastTankNo() +ballastRecord.getBallastTankType() + "'?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ballastViewModel.delete(ballastRecord);
                        Toast.makeText(BallastTankActivity.this, "Tank deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void createAndSharePdfReport() {
        // ViewModel'dan veya Adapter'dan güncel listeyi al
        List<com.example.shipcalculation.models.BallastRecord> currentBallastRecords = ballastAdapter.getCurrentList(); // BU SATIR ŞİMDİ DOĞRU ÇALIŞMALI

        if (currentBallastRecords == null || currentBallastRecords.isEmpty()) {
            Toast.makeText(this, "No data to create PDF.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "PDF creation aborted: No data in ballastAdapter."); // Log ekleyelim
            return; // Veri yoksa PDF oluşturma
        }
        Log.d(TAG, "Creating PDF with " + currentBallastRecords.size() + " records."); // Kaç kayıtla PDF oluşturulduğunu logla


        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint tableHeaderPaint = new Paint();
        Paint tableTextPaint = new Paint();

        // Sayfa bilgileri (A4 boyutuna yakın)
        int pageWidth = 595; // A4 genişliği (yaklaşık 72 dpi'da)
        int pageHeight = 842; // A4 yüksekliği
        int margin = 40;
        // int contentWidth = pageWidth - 2 * margin; // Bu değişken kullanılmıyor, kaldırılabilir

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // --- Rapor Başlığı ve Tarih ---
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Ballast Water Report", pageWidth / 2f, margin + 20, titlePaint);

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

        float col1X = margin;
        float col2X = margin + 150; // Sounding
        float col3X = margin + 220; // Volume
        float col4X = margin + 290; // Density
        float col5X = margin + 360; // Weight

        canvas.drawText("Tank No/Type", col1X, currentY, tableHeaderPaint);
        canvas.drawText("Sounding (m)", col2X, currentY, tableHeaderPaint);
        canvas.drawText("Volume (m³)", col3X, currentY, tableHeaderPaint);
        canvas.drawText("Density(kg/l)", col4X, currentY, tableHeaderPaint);
        canvas.drawText("Weight (MT)", col5X, currentY, tableHeaderPaint);
        currentY += lineHeight;
        canvas.drawLine(margin, currentY - padding, pageWidth - margin, currentY - padding, paint); // Başlık altı çizgi

        // Tablo Verileri
        tableTextPaint.setTextSize(10f);
        tableTextPaint.setColor(Color.DKGRAY);
        double totalVolume = 0;
        double totalWeight = 0;

        // BURAYI DİKKATLİCE DEĞİŞTİRİN: Adapter'dan alınan listeyi kullanın
        // ve BallastRecord modelinizin tam yolunu kullanın (eğer iç içe değilse com.example.shipcalculation.BallastRecord yerine
        // com.example.shipcalculation.models.BallastRecord gibi)
        for (com.example.shipcalculation.models.BallastRecord record : currentBallastRecords) { // currentBallastRecords kullanılıyor
            currentY += lineHeight;
            // Modelinizdeki alan adlarının doğru olduğundan emin olun (tankNoAndType, sounding vb.)
            // Eğer BallastRecord sınıfınız com.example.shipcalculation.models paketindeyse, getter'ları kullanmanız daha iyi olur.
            // Örneğin: record.getTankNoAndType()
            canvas.drawText(record.getBallastTankNo() + " (" + record.getBallastTankType() + ")", col1X, currentY, tableTextPaint); // Örnek: getter kullanımı
            canvas.drawText(String.format(Locale.US, "%.2f", record.getBallastTankSounding()), col2X, currentY, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.2f", record.getBallastTankVolume()), col3X, currentY, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.3f", record.getDensity()), col4X, currentY, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.2f", record.getBallastTankWeight()), col5X, currentY, tableTextPaint);

            totalVolume += record.getBallastTankVolume();
            totalWeight += record.getBallastTankWeight();
        }
        currentY += padding;
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint); // Son kayıt altı çizgi

        // --- Toplamlar ---
        currentY += lineHeight * 2; // Biraz boşluk bırak
        paint.setTextSize(12f);
        paint.setFakeBoldText(true);
        String totalVolumeStr = String.format(Locale.US, "Total Volume in Tanks: %.3f m³", totalVolume);
        String totalWeightStr = String.format(Locale.US, "Total Weight in Tanks: %.3f MT", totalWeight);

        float totalVolumeWidth = paint.measureText(totalVolumeStr);
        float totalWeightWidth = paint.measureText(totalWeightStr);

        canvas.drawText(totalVolumeStr, pageWidth - margin - totalVolumeWidth, currentY, paint);
        currentY += lineHeight;
        canvas.drawText(totalWeightStr, pageWidth - margin - totalWeightWidth, currentY, paint);

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
        File outputDir = new File(getCacheDir(), "pdf_ballast_reports"); // "pdf_reports" -> "pdf_ballast_reports" olarak değiştirildi
        Log.d(TAG, "Cache Directory: " + getCacheDir().getAbsolutePath());
        Log.d(TAG, "Output Directory for PDF: " + outputDir.getAbsolutePath());
        if (!outputDir.exists()) {
            boolean success = outputDir.mkdirs();
            Log.d(TAG, "Output directory (" + outputDir.getName() + ") created: " + success);
        }
       // String filename = "BallastWaterReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        String fileName = "BallastWaterReport_" + new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
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

