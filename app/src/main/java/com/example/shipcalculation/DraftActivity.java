package com.example.shipcalculation;


import static android.view.View.GONE;

import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore; // MediaStore'u ekleyin
import androidx.activity.result.ActivityResultLauncher; // Gerekli importları ekleyin
import androidx.activity.result.IntentSenderRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream; // OutputStream'i ekleyin
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.Calendar;
import java.util.Locale;

public class DraftActivity extends AppCompatActivity {

    FrameLayout frameLayout;

    EditText editTextDated, editTextFwdPortd, editTextFwdStbd, editTextMidPortd, editTextMidStbd, editTextAftPortd, editTextAftStbd;

    TextView textViewFwdd, textViewMidd, textViewAftd, textViewFwdCorrd, textViewMidCorrd, textViewAftCorrd,
            textViewDeflectiond, textViewTrimAppd, textViewTrimCorrd, textViewT_Meand;
    ImageView ShipImageView;

    FloatingActionButton btnSaveInd, btnSavePdf;
    CheckBox checkBoxFwdd, checkBoxMidd, checkBoxAftd;
    SharedPreferences pref;
    private static final String TAG = "DraftActivity";
    private ActivityResultLauncher<IntentSenderRequest> savePdfLauncher;
    private Calendar selectedDateTime = Calendar.getInstance(); // Seçilen veya başlangıçtaki tarih/saati tutar
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_draft);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        frameLayout = findViewById(R.id.frameLayout);

        pref = getSharedPreferences("MyData", MODE_PRIVATE);
        editTextDated = findViewById(R.id.editTextDated);
        editTextFwdPortd = findViewById(R.id.editTextFwdPortd);
        editTextFwdStbd = findViewById(R.id.editTextFwdStbd);
        editTextMidPortd = findViewById(R.id.editTextMidPortd);
        editTextMidStbd = findViewById(R.id.editTextMidStbd);
        editTextAftPortd = findViewById(R.id.editTextAftPortd);
        editTextAftStbd = findViewById(R.id.editTextAftStbd);
        btnSaveInd = findViewById(R.id.btnSaveInd);

        checkBoxFwdd = findViewById(R.id.checkBoxFwdd);
        checkBoxMidd = findViewById(R.id.checkBoxMidd);
        checkBoxAftd = findViewById(R.id.checkBoxAftd);
        textViewFwdd = findViewById(R.id.textViewFwdd);
        textViewMidd = findViewById(R.id.textViewMidd);
        textViewAftd = findViewById(R.id.textViewAftd);
        textViewMidCorrd = findViewById(R.id.textViewMidCorrd);
        textViewFwdCorrd = findViewById(R.id.textViewFwdCorrd);
        textViewAftCorrd = findViewById(R.id.textViewAftCorrd);
        textViewDeflectiond = findViewById(R.id.textViewDeflectiond);
        textViewTrimAppd = findViewById(R.id.textViewTrimAppd);
        textViewTrimCorrd = findViewById(R.id.textViewTrimCorrd);
        textViewT_Meand = findViewById(R.id.textViewT_Meand);
        btnSavePdf = findViewById(R.id.btnSavePdf);
        ShipImageView = findViewById(R.id.shipImageView);
        btnSavePdf.setVisibility(GONE);// PDF kaydetme butonu


        int[] editTextIds = {R.id.editTextDated, R.id.editTextFwdPortd, R.id.editTextFwdStbd,
                R.id.editTextMidPortd, R.id.editTextMidStbd, R.id.editTextAftPortd, R.id.editTextAftStbd};
        int[] checkBoxIds = {R.id.checkBoxFwdd, R.id.checkBoxMidd, R.id.checkBoxAftd};
        for (int id : editTextIds) {
            EditText editText = findViewById(id);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    btnSavePdf.setVisibility(GONE);
                    unsavedChanges = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        for (int id : checkBoxIds) {
            CheckBox checkBox = findViewById(id);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // Unchecked
                            new int[]{android.R.attr.state_checked}   // Checked
                    },
                    new int[]{
                            Color.GRAY,     // Unchecked color
                            Color.BLACK    // Checked color
                    }
            );
            CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                btnSavePdf.setVisibility(GONE);
            });

        }
        //editTextDated.setText(pref.getString("dated", ""));
        editTextFwdPortd.setText(pref.getString("fwdPortd", ""));
        editTextFwdStbd.setText(pref.getString("fwdStbd", ""));
        editTextMidPortd.setText(pref.getString("midPortd", ""));
        editTextMidStbd.setText(pref.getString("midStbd", ""));
        editTextAftStbd.setText(pref.getString("aftStbd", ""));
        editTextAftPortd.setText(pref.getString("aftPortd", ""));
        boolean isCheckedFwdd = pref.getBoolean("checkBoxFwdd", false);
        checkBoxFwdd.setChecked(isCheckedFwdd);
        boolean isCheckedMidd = pref.getBoolean("checkBoxMidd", false);
        checkBoxMidd.setChecked(isCheckedMidd);
        boolean isCheckedAftd = pref.getBoolean("checkBoxAftd", false);
        checkBoxAftd.setChecked(isCheckedAftd);
        btnSavePdf.setOnClickListener(v -> saveToPdf());
        btnSaveInd.setOnClickListener(v -> {

            try {
                //String dated = editTextDated.getText().toString();
                String fwdPortd = editTextFwdPortd.getText().toString();
                String fwdStbd = editTextFwdStbd.getText().toString();
                String midStbd = editTextMidStbd.getText().toString();
                String midPortd = editTextMidPortd.getText().toString();
                String aftPortd = editTextAftPortd.getText().toString();
                String aftStbd = editTextAftStbd.getText().toString();
// from SettingsActivity
                String lbmValue = pref.getString("lbm", "");
                String lbpValue = pref.getString("lbp", "");
                String foreDistValue = pref.getString("foreDist", "");
                String midDistValue = pref.getString("midDist", "");
                String aftDistValue = pref.getString("aftDist", "");
// from SettingsActivity
                double lbm = Double.parseDouble(lbmValue);
                double lbp = Double.parseDouble(lbpValue);
                double foreDist = Double.parseDouble(foreDistValue);
                double midDist = Double.parseDouble(midDistValue);
                double aftDist = Double.parseDouble(aftDistValue);
                double fwdPortValue = Double.parseDouble(fwdPortd);
                double fwdStbValue = Double.parseDouble(fwdStbd);
                double midStbValue = Double.parseDouble(midStbd);
                double midPortValue = Double.parseDouble(midPortd);
                double aftPortValue = Double.parseDouble(aftPortd);
                double aftStbValue = Double.parseDouble(aftStbd);
                double fwdd = (fwdPortValue + fwdStbValue) / 2;
                double midd = (midPortValue + midStbValue) / 2;
                double aftd = (aftPortValue + aftStbValue) / 2;
                double trimd = aftd - fwdd;
                double fwdCorrd;
                double midCorrd;
                double aftCorrd;
                if (checkBoxMidd.isChecked()) {
                    midCorrd = midd + ((midDist / lbm) * trimd);
                    textViewMidCorrd.setBackgroundResource(R.drawable.chk);
                    textViewMidCorrd.setText(String.format(Locale.US, "Corr.Mid: %.3f", midCorrd));
                } else {
                    midCorrd = midd;
                    textViewMidCorrd.setBackgroundResource(R.drawable.background_text);
                    textViewMidCorrd.setText("No need Corr.");
                }
                if (checkBoxAftd.isChecked()) {
                    aftCorrd = aftd + ((aftDist / lbm) * trimd);
                    textViewAftCorrd.setBackgroundResource(R.drawable.chk);
                    textViewAftCorrd.setText(String.format(Locale.US, "Corr.Aft: %.3f", aftCorrd));
                } else {
                    aftCorrd = aftd;
                    textViewAftCorrd.setBackgroundResource(R.drawable.background_text);
                    textViewAftCorrd.setText("No need Corr.");
                }
                if (checkBoxFwdd.isChecked()) {
                    fwdCorrd = fwdd + ((foreDist / lbm) * trimd);
                    textViewFwdCorrd.setBackgroundResource(R.drawable.chk);
                    textViewFwdCorrd.setText(String.format(Locale.US, "Corr.Fwd: %.3f", fwdCorrd));
                } else {
                    fwdCorrd = fwdd;
                    textViewFwdCorrd.setBackgroundResource(R.drawable.background_text);
                    textViewFwdCorrd.setText("No need Corr.");
                }
                double trimCorrd = Math.round(aftCorrd * 1000.0) / 1000.0 - Math.round(fwdCorrd * 1000.0) / 1000.0;
                double meand = ((Math.round(midCorrd * 1000.0) / 1000.0 * 6) + (Math.round(fwdCorrd * 1000.0) / 1000.0 + Math.round(aftCorrd * 1000.0) / 1000.0)) / 8;
                double hogSagd = (((aftd + fwdd) / 2) - midd)*100;
                textViewFwdd.setText(String.format(Locale.US, "%.3f", fwdd));
                textViewMidd.setText(String.format(Locale.US, "%.3f", midd));
                textViewAftd.setText(String.format(Locale.US, "%.3f", aftd));
                textViewTrimAppd.setText(String.format(Locale.US, "Trim: %.3f", trimd));
                textViewTrimCorrd.setText(String.format(Locale.US, "Corrected Trim : %.3f", trimCorrd));
                textViewT_Meand.setText(String.format(Locale.US, "Mean of Mean :  %.4f", meand));
                //pref.edit().putString("dated", dated).apply();
                pref.edit().putString("fwdPortd", fwdPortd).apply();
                pref.edit().putString("fwdStbd", fwdStbd).apply();
                pref.edit().putString("midStbd", midStbd).apply();
                pref.edit().putString("midPortd", midPortd).apply();
                pref.edit().putString("aftPortd", aftPortd).apply();
                pref.edit().putString("aftStbd", aftStbd).apply();
                pref.edit().putString("fwdd", String.valueOf(fwdd)).apply();
                pref.edit().putString("midd", String.valueOf(midd)).apply();
                pref.edit().putString("aftd", String.valueOf(aftd)).apply();
                pref.edit().putString("fwdCorrd", String.valueOf(fwdCorrd)).apply();
                pref.edit().putString("midCorrd", String.valueOf(midCorrd)).apply();
                pref.edit().putString("aftCorrd", String.valueOf(aftCorrd)).apply();
                pref.edit().putString("trimd", String.valueOf(trimd)).apply();
                pref.edit().putString("trimCorrd", String.valueOf(trimCorrd)).apply();
                pref.edit().putString("meand", String.valueOf(meand)).apply();
                pref.edit().putBoolean("checkBoxFwdd", checkBoxFwdd.isChecked()).apply();
                pref.edit().putBoolean("checkBoxMidd", checkBoxMidd.isChecked()).apply();
                pref.edit().putBoolean("checkBoxAftd", checkBoxAftd.isChecked()).apply();
                double tolerance = 0.0001;
                if (hogSagd < -tolerance) {
                    textViewDeflectiond.setTextColor(Color.RED);
                    textViewDeflectiond.setText(String.format(Locale.US, "Sag:%.2fcm", hogSagd));
                    ShipImageView.setImageResource(R.drawable.ship_hog);
                } else if (hogSagd > tolerance) {
                    textViewDeflectiond.setTextColor(Color.BLUE);
                    textViewDeflectiond.setText(String.format(Locale.US, "Hog:%.2fcm", hogSagd));
                    ShipImageView.setImageResource(R.drawable.ship_sag);
                } else {
                    textViewDeflectiond.setTextColor(Color.BLACK);
                    textViewDeflectiond.setText("No Deflection");
                    ShipImageView.setImageResource(R.drawable.ship_four);
                }

                btnSavePdf.setVisibility(View.VISIBLE);
                Toast.makeText(DraftActivity.this, "Data Saved Successfully", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(DraftActivity.this, "Invalid data! Fill in the blank fields and home page data or enter 0.0" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("DraftActivity", "Error Happen", e);
            }

        });

        // Set an OnClickListener to handle when the user taps the EditText
        // onCreate (veya onViewCreated) içinde

// 1. Gerçek zamanı al ve EditText'e yaz
        Calendar calendar = Calendar.getInstance(); // Dışarıda tanımladık, hem ilk değer hem picker'lar için
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String dateTimeString = String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d",
                dayOfMonth, month + 1, year, hour, minute);
        editTextDated.setText(dateTimeString);

// 2. Kullanıcı tıkladığında tarih ve saat seçsin
        editTextDated.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        // Tarih seçildiğinde saat seçici aç
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                this,
                                (view1, selectedHour, selectedMinute) -> {
                                    // calendar'ı güncelle ve göster
                                    calendar.set(selectedYear, selectedMonth, selectedDayOfMonth, selectedHour, selectedMinute);
                                    String selectedDateTime = String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d",
                                            selectedDayOfMonth, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                                    editTextDated.setText(selectedDateTime);
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true // 24 saat biçimi
                        );
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

// 3. Kullanıcının manuel girdiği değeri doğrula (opsiyonel ama önerilir)
        editTextDated.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String dateTimeStr = s.toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sdf.setLenient(false);
                try {
                    Date date = sdf.parse(dateTimeStr);
                    calendar.setTime(date); // geçerli ise calendar'ı güncelle
                    editTextDated.setError(null);
                } catch (ParseException e) {
                    editTextDated.setError("Biçim: gg/aa/yyyy ss:dd");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


    }
    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        editTextDated.setText(sdf.format(selectedDateTime.getTime()));
    }
    boolean unsavedChanges = false;

    private void saveToPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(350, 350, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        int y = 50;
        y = drawTitleWithUnderline(canvas, paint, "Draft Calculation", y);
        y += 20;

        y = drawDateWithUnderline(canvas, paint, "Date&Time", editTextDated.getText().toString(), y);


        y += 20;

        y = drawFourTextsWithLabel(canvas, paint, "Fwd:", editTextFwdPortd.getText().toString(), editTextFwdStbd.getText().toString(), textViewFwdd.getText().toString(), textViewFwdCorrd.getText().toString(), y, true, false);
        y = drawFourTextsWithLabel(canvas, paint, "Mid:", editTextMidPortd.getText().toString(), editTextMidStbd.getText().toString(), textViewMidd.getText().toString(), textViewMidCorrd.getText().toString(), y, false, false);
        y = drawFourTextsWithLabel(canvas, paint, "Aft:", editTextAftPortd.getText().toString(), editTextAftStbd.getText().toString(), textViewAftd.getText().toString(), textViewAftCorrd.getText().toString(), y, false, true);
        y += 20; // Araya boşluk ekleyelim
        y = drawDownWithLabel(canvas, paint, textViewDeflectiond.getText().toString(), y);
        y += 10; // Araya boşluk ekleyelim
        y = drawDownWithLabel(canvas, paint, textViewTrimAppd.getText().toString(), y);
        y += 10; // Araya boşluk ekleyelim
        y = drawDownWithLabel(canvas, paint, textViewTrimCorrd.getText().toString(), y);
        y += 10; // Araya boşluk ekleyelim
        y = drawDownWithLabel(canvas, paint, textViewT_Meand.getText().toString(), y);

        document.finishPage(page);

        String filename = "draft_calculation_" + new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToPdfModern(document, filename);
        } else {
            saveToPdfLegacy(document, filename);
        }
    }
    private void saveToPdfModern(PdfDocument document, String filename) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/DraftSurvey");

        Uri pdfUri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);
        try (OutputStream outputStream = getContentResolver().openOutputStream(pdfUri)) {
            document.writeTo(outputStream);
            Toast.makeText(this, " PDF Saved Successfully: ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "  PDF Saving Error!!!" + e.getMessage(), e);
            Toast.makeText(this, " PDF Saving Error!!! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveToPdfLegacy(PdfDocument document, String filename) {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DraftSurvey");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Toast.makeText(this, "PDF Saved Successfully " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, " PDF Saving Error!!! " + e.getMessage(), e);
            Toast.makeText(this, " PDF Saving Error!!! " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private int drawTitleWithUnderline(Canvas canvas, Paint paint, String title, int y) {
        float originalTextSize = paint.getTextSize();
        // Başlığı çizin
        paint.setTextSize(20);
        canvas.drawText(title, 100, y, paint);
        paint.setStyle(Paint.Style.FILL);

        // Alt çizgiyi çizin
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(100, y + 5, 100 + paint.measureText(title), y + 5, paint);
        paint.setStyle(Paint.Style.FILL);
        // Yazı boyutunu eski haline döndürün
        paint.setTextSize(originalTextSize);
        // Y değerini güncelleyin
        return y + 30; // Başlık yüksekliği + boşluk
    }
    private int drawDateWithUnderline(Canvas canvas, Paint paint, String label, String value, int y) {
        float originalTextSize = paint.getTextSize();
        // Tarih etiketini çizin
        canvas.drawText(label + ":", 150, y, paint);
        canvas.drawText(value, 220, y, paint);

        // Alt çizgiyi çizin
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(150, y + 5, 320, y + 5, paint); // Çizgi sonu
        paint.setStyle(Paint.Style.FILL); // Doldurma moduna geç
        // Yazı boyutunu eski haline döndürün
        paint.setTextSize(originalTextSize);
        // Y değerini güncelleyin
        return y + 30; // Tarih yüksekliği + boşluk
    }
    private int drawTextWithLabel(Canvas canvas, Paint paint, EditText view, int y) {
        return drawTextWithLabel(canvas, paint, view.getText().toString(), y);
    }

    private int drawTextWithLabel(Canvas canvas, Paint paint, String value, int y) {
        canvas.drawText("Date:", 180, y, paint);
        canvas.drawText(value, 220, y, paint);
        return y + 20;
    }
    private int drawDownWithLabel(Canvas canvas, Paint paint, String value, int y) {
        canvas.drawText(value, 20, y, paint);
        return y + 10;
    }
    private int drawFourTextsWithLabel(Canvas canvas, Paint paint, String label, String value1, String value2, String value3, String value4, int y, boolean drawLine, boolean drawUnderLine) {
        if (drawLine) {
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(10, y - 20, 330, y - 20, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        // Çizgi çizin
        //canvas.drawLine(20, y - 10, 330, y - 10, paint); // Çizgi için başlangıç ve bitiş noktaları
        Typeface originalTypeface = paint.getTypeface();
        float originalTextSize = paint.getTextSize();
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(label, 15, y, paint);
        paint.setTextSize(16);
        paint.setColor(Color.RED);
        canvas.drawText(value1, 60, y, paint);
        paint.setColor(Color.GREEN);
        canvas.drawText(value2, 105, y, paint);
        paint.setTextSize(originalTextSize);
        paint.setColor(Color.BLACK);
        canvas.drawText(value3, 150, y, paint);
        canvas.drawText(value4, 230, y, paint);
        // Eğer altında çizgi çizilecekse
        if (drawUnderLine) {
            paint.setStrokeWidth(2); // Çizgi kalınlığı
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(10, y + 15, 330, y + 15, paint); // Alt çizgi
            paint.setStyle(Paint.Style.FILL); // Doldurma moduna geç
        }

        return y + 20; // Y değeri güncelleniyor
    }



}








