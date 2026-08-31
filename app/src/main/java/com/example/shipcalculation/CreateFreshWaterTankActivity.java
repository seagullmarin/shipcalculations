package com.example.shipcalculation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.shipcalculation.models.FreshWaterRecord;
import com.example.shipcalculation.models.FreshWaterSoundingLog;
import com.example.shipcalculation.viewmodels.FreshWaterViewModel;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateFreshWaterTankActivity extends AppCompatActivity {

    // Intent'ten veri almak için anahtar
    public static final String EXTRA_FRESH_WATER_ID = "com.example.shipcalculation.EXTRA_FRESH_WATER_ID";
    // Eğer düzenleme modunda olduğumuzu belirtmek için bir ID değeri yoksa kullanılabilecek varsayılan değer
    public static final int DEFAULT_ID = -1;
    private TextView textViewTitle;
    private EditText editTextTankNo, editTextTankCapacity, editTextTankSounding,
             editTextTankVolume,editTextDateTime;

    private Button buttonSaveTank; // Tek bir kaydetme butonu (Create/Update için)

    private FreshWaterViewModel freshWaterViewModel;
    private int currentFreshWaterId = DEFAULT_ID; // Mevcut tankın ID'si, varsayılan olarak -1 (yeni tank)
    private Calendar selectedDateTime = Calendar.getInstance(); // Seçilen veya başlangıçtaki tarih/saati tutar

    private AutoCompleteTextView autoCompleteTankType;
    private TextInputLayout tankTypeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fresh_water_tank); // XML layout dosyanız

        // ViewModel'ı başlat
        freshWaterViewModel = new ViewModelProvider(this).get(FreshWaterViewModel.class);

        // View'ları bul
        editTextTankNo = findViewById(R.id.editTextTankNo);
        //editTextTankType = findViewById(R.id.editTextTankType); // FreshWaterRecord'da bu alan String ise inputType="text" olmalı
        editTextTankCapacity = findViewById(R.id.editTextTankCapacity);
        editTextTankSounding = findViewById(R.id.editTextTankSounding);

        editTextTankVolume = findViewById(R.id.editTextTankVolume);

        editTextDateTime = findViewById(R.id.editTextDateTime);// XML'de bu ID ile bir TextView olmalı
        buttonSaveTank = findViewById(R.id.buttonCreateFreshWaterTank); // XML'deki buton ID'niz buysa
        tankTypeLayout = findViewById(R.id.tankTypeLayout);
        autoCompleteTankType = findViewById(R.id.autoCompleteTankType);
        textViewTitle = findViewById(R.id.textViewTitle);

        // Tank Tipi için seçenekleri tanımlayın
        String[] tankTypes = new String[] {"FW", "AFT", "CENTER", "OTHER"};

        // ArrayAdapter oluşturun
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, // Basit bir dropdown item layout'u
                tankTypes
        );

        // Adapter'ı AutoCompleteTextView'a bağlayın
        autoCompleteTankType.setAdapter(adapter);

    // Intent'i kontrol et (Düzenleme modu için)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_FRESH_WATER_ID)) {
            currentFreshWaterId = intent.getIntExtra(EXTRA_FRESH_WATER_ID, DEFAULT_ID);
            if (currentFreshWaterId != DEFAULT_ID) {
                // Düzenleme modu
                setTitle("Edit FreshWater Tank");
                textViewTitle.setText("Edit Fresh Water Tank");
                buttonSaveTank.setText("Update Tank");

                freshWaterViewModel.getFreshWaterRecordById(currentFreshWaterId).observe(this, freshWaterRecord -> {
                    if (freshWaterRecord != null) {
                        populateUI(freshWaterRecord); // Önce mevcut verileri yükle

                        // --- YENİ EKLENEN KISIM ---
                        // Mevcut veriler yüklendikten sonra,
                        // selectedDateTime'ı o anki sistem zamanıyla güncelle.
                        // Kullanıcı yine de editTextDateTime'a tıklayarak bunu değiştirebilir.
                        selectedDateTime = Calendar.getInstance();
                        updateDateTimeDisplay(); // Güncellenmiş zamanı EditText'te göster
                        // --- YENİ EKLENEN KISIM SONU ---
                    } else {
                        // Kayıt bulunamadıysa (nadiren olmalı ama bir önlem)
                        // Yeni kayıt moduna benzer şekilde davranabilir veya hata gösterebilirsiniz.
                        setTitle("Create FreshWater Tank");
                        textViewTitle.setText("Create FreshWater Tank");
                        buttonSaveTank.setText("Create Tank");
                        selectedDateTime = Calendar.getInstance();
                        updateDateTimeDisplay();
                        Toast.makeText(CreateFreshWaterTankActivity.this, "Record not found, creating new.", Toast.LENGTH_SHORT).show();
                        currentFreshWaterId = DEFAULT_ID; // Yeni kayıt moduna geç
                    }
                });
            } else {
                // ID geçersiz, yeni tank modu
                setTitle("Create FreshWater Tank");
                textViewTitle.setText("Create FreshWater Tank");
                buttonSaveTank.setText("Create Tank");
                selectedDateTime = Calendar.getInstance();
                updateDateTimeDisplay();
            }
        } else {
            // Yeni tank modu
            setTitle("Create FreshWater Tank");
            textViewTitle.setText("Create FreshWater Tank");
            buttonSaveTank.setText("Create Tank");
            selectedDateTime = Calendar.getInstance();
            updateDateTimeDisplay();
        }

// DateTime EditText'ine tıklandığında Date ve Time Picker'ları aç
        editTextDateTime.setOnClickListener(v -> showDateTimePicker());

        // TextWatcher'lar ile anlık ağırlık hesaplaması
        TextWatcher weightCalculatorTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextTankVolume.addTextChangedListener(weightCalculatorTextWatcher);
        // Kaydetme butonu tıklama olayı
        buttonSaveTank.setOnClickListener(v -> saveOrUpdateTank());
    }
    private void populateUI(FreshWaterRecord record) {
        editTextTankNo.setText(record.getFreshWaterTankNo());
        autoCompleteTankType.setText(record.getFreshWaterTankType());//yeni
        editTextTankCapacity.setText(String.valueOf(record.getFreshWaterTankCapacity()));
        editTextTankSounding.setText(String.valueOf(record.getFreshWaterTankSounding()));
        editTextTankVolume.setText(String.valueOf(record.getFreshWaterTankVolume()));
        // Ağırlık, hacim ve yoğunluktan hesaplanacağı için doğrudan set etmeye gerek yok,
        // TextWatcher'lar bunu halledecektir. Ya da doğrudan da set edebilirsiniz:

        if (record.getLastUpdated() != null) {
            selectedDateTime.setTime(record.getLastUpdated());
        } else {
            selectedDateTime = Calendar.getInstance(); // Eğer null ise o anki zaman
        }
        updateDateTimeDisplay();
    }
    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance(); // Picker'ı başlatmak için o anki tarih
        if (selectedDateTime != null) { // Eğer daha önce bir tarih seçilmişse onu kullan
            currentDate.setTime(selectedDateTime.getTime());
        }

        new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            selectedDateTime.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(CreateFreshWaterTankActivity.this, (timeView, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0); // Saniyeyi sıfırla (isteğe bağlı)
                updateDateTimeDisplay();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show(); // 24 saat formatı
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }
    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        editTextDateTime.setText(sdf.format(selectedDateTime.getTime()));
    }
    private void saveOrUpdateTank() {
        String tankNo = editTextTankNo.getText().toString().trim();
        String tankType = autoCompleteTankType.getText().toString().trim();
        String capacityStr = editTextTankCapacity.getText().toString().trim();
        String soundingStr = editTextTankSounding.getText().toString().trim();
        String volumeStr = editTextTankVolume.getText().toString().trim(); // Kullanıcının girdiği hacim

        // Temel doğrulamalar (tankNo, capacityStr, soundingStr, volumeStr)
        if (TextUtils.isEmpty(tankNo) || TextUtils.isEmpty(tankType) || TextUtils.isEmpty(capacityStr) ||
                TextUtils.isEmpty(soundingStr) || TextUtils.isEmpty(volumeStr) ||
                TextUtils.isEmpty(editTextDateTime.getText().toString())) { // Tarih de boş olmasın
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double capacity = Double.parseDouble(capacityStr);
            double sounding = Double.parseDouble(soundingStr);
            double volume = Double.parseDouble(volumeStr); // Manuel girilen hacim

            Date finalDateTime = selectedDateTime.getTime();

            FreshWaterRecord freshWaterRecord;

            if (currentFreshWaterId == DEFAULT_ID) {
                // Yeni tank oluşturma
                freshWaterRecord = new FreshWaterRecord(tankNo, tankType, capacity, sounding, volume, finalDateTime);
                freshWaterViewModel.insert(freshWaterRecord);
                Toast.makeText(this, "FreshWater tank created.", Toast.LENGTH_SHORT).show();
                // Bu durumda log oluşturulmuyor. İlk log, tank düzenlendiğinde oluşacak.
            } else {
                // Mevcut tankı güncelleme
                freshWaterRecord = new FreshWaterRecord(tankNo, tankType, capacity, sounding, volume, finalDateTime);
                freshWaterRecord.setId(currentFreshWaterId);
                freshWaterViewModel.update(freshWaterRecord); // Ana kaydı güncelle

                // AYNI ZAMANDA YENİ BİR SOUNDING LOGU OLUŞTUR
                FreshWaterSoundingLog newLog = new FreshWaterSoundingLog(
                        currentFreshWaterId, // Ana tankın ID'si
                        finalDateTime,       // Logun zamanı (ana kayıtla aynı)
                        sounding,            // Girilen sounding
                        volume               // Girilen hacim
                );
                freshWaterViewModel.insertSoundingLog(newLog); // ViewModel'daki metot
                Toast.makeText(this, "Tank updated and sounding log created.", Toast.LENGTH_SHORT).show();
            }
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for capacity, sounding, and volume", Toast.LENGTH_LONG).show();
        }
    }
}

