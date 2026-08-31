package com.example.shipcalculation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // textViewTankWeight için
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // ViewModel için

import com.example.shipcalculation.models.BallastRecord;
import com.example.shipcalculation.viewmodels.BallastViewModel; // ViewModel importu
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class CreateBallastTankActivity extends AppCompatActivity {

    // Intent'ten veri almak için anahtar
    public static final String EXTRA_BALLAST_ID = "com.example.shipcalculation.EXTRA_BALLAST_ID";
    // Eğer düzenleme modunda olduğumuzu belirtmek için bir ID değeri yoksa kullanılabilecek varsayılan değer
    public static final int DEFAULT_ID = -1;
    private TextView textViewTitle;
    private EditText editTextTankNo, editTextTankType, editTextTankCapacity, editTextTankSounding,
            editTextDensity, editTextTankVolume,editTextDateTime;
    private TextView textViewTankWeight; // Ağırlığı göstermek için
    private Button buttonSaveTank; // Tek bir kaydetme butonu (Create/Update için)

    private BallastViewModel ballastViewModel;
    private int currentBallastId = DEFAULT_ID; // Mevcut tankın ID'si, varsayılan olarak -1 (yeni tank)
    private Calendar selectedDateTime = Calendar.getInstance(); // Seçilen veya başlangıçtaki tarih/saati tutar

    private AutoCompleteTextView autoCompleteTankType;
    private TextInputLayout tankTypeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ballast_tank); // XML layout dosyanız

        // ViewModel'ı başlat
        ballastViewModel = new ViewModelProvider(this).get(BallastViewModel.class);

        // View'ları bul
        editTextTankNo = findViewById(R.id.editTextTankNo);
        //editTextTankType = findViewById(R.id.editTextTankType); // BallastRecord'da bu alan String ise inputType="text" olmalı
        editTextTankCapacity = findViewById(R.id.editTextTankCapacity);
        editTextTankSounding = findViewById(R.id.editTextTankSounding);
        editTextDensity = findViewById(R.id.editTextDensity);
        editTextTankVolume = findViewById(R.id.editTextTankVolume);
        textViewTankWeight = findViewById(R.id.textViewTankWeight);
        editTextDateTime = findViewById(R.id.editTextDateTime);// XML'de bu ID ile bir TextView olmalı
        buttonSaveTank = findViewById(R.id.buttonCreateBallastTank); // XML'deki buton ID'niz buysa
        tankTypeLayout = findViewById(R.id.tankTypeLayout);
        autoCompleteTankType = findViewById(R.id.autoCompleteTankType);
        textViewTitle = findViewById(R.id.textViewTitle);

        // Tank Tipi için seçenekleri tanımlayın
        String[] tankTypes = new String[] {"PORT", "STB", "CENTER", "OTHER"};

        // ArrayAdapter oluşturun
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, // Basit bir dropdown item layout'u
                tankTypes
        );

        // Adapter'ı AutoCompleteTextView'a bağlayın
        autoCompleteTankType.setAdapter(adapter);

        // İsteğe bağlı: Kullanıcı "OTHER" seçtiğinde veya farklı bir şey yazdığında
        // özel bir işlem yapmak isterseniz listener ekleyebilirsiniz.
        /*
        autoCompleteTankType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            // Seçilen öğeyle bir şeyler yapın
            if ("OTHER".equals(selectedType)) {
                // Belki başka bir EditText'i görünür yapın veya özel bir flag ayarlayın
            }
        });
        */

        // Kaydetme veya veri alma işlemi sırasında autoCompleteTankType.getText().toString() ile değeri alabilirsiniz.



    // Intent'i kontrol et (Düzenleme modu için)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BALLAST_ID)) {
            currentBallastId = intent.getIntExtra(EXTRA_BALLAST_ID, DEFAULT_ID);
            if (currentBallastId != DEFAULT_ID) {
                // Düzenleme modu
                setTitle("Edit Ballast Tank");
                textViewTitle.setText("Edit Ballast Tank");
                buttonSaveTank.setText("Update Tank");

                ballastViewModel.getBallastRecordById(currentBallastId).observe(this, ballastRecord -> {
                    if (ballastRecord != null) {
                        populateUI(ballastRecord); // Önce mevcut verileri yükle

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
                        textViewTitle.setText("Create Ballast Tank");
                        buttonSaveTank.setText("Create Tank");
                        selectedDateTime = Calendar.getInstance();
                        updateDateTimeDisplay();
                        editTextDensity.setText("1.025");
                        Toast.makeText(CreateBallastTankActivity.this, "Record not found, creating new.", Toast.LENGTH_SHORT).show();
                        currentBallastId = DEFAULT_ID; // Yeni kayıt moduna geç
                    }
                });
            } else {
                // ID geçersiz, yeni tank modu
                setTitle("Create Ballast Tank");
                textViewTitle.setText("Create Ballast Tank");
                buttonSaveTank.setText("Create Tank");
                selectedDateTime = Calendar.getInstance();
                updateDateTimeDisplay();
                editTextDensity.setText("1.025");
            }
        } else {
            // Yeni tank modu
            setTitle("Create Ballast Tank");
            textViewTitle.setText("Create Ballast Tank");
            buttonSaveTank.setText("Create Tank");
            selectedDateTime = Calendar.getInstance();
            updateDateTimeDisplay();
            editTextDensity.setText("1.025");
        }

// DateTime EditText'ine tıklandığında Date ve Time Picker'ları aç
        editTextDateTime.setOnClickListener(v -> showDateTimePicker());
        // editTextDateTime.setFocusable(false); // Klavye açılmasını engellemek için, sadece tıklama ile çalışsın
        // editTextDateTime.setClickable(true);

        // TextWatcher'lar ile anlık ağırlık hesaplaması
        TextWatcher weightCalculatorTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayWeight();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextTankVolume.addTextChangedListener(weightCalculatorTextWatcher);
        editTextDensity.addTextChangedListener(weightCalculatorTextWatcher);
        calculateAndDisplayWeight();// Başlangıçta da bir kez hesapla (eğer değerler varsa)


        // Kaydetme butonu tıklama olayı
        buttonSaveTank.setOnClickListener(v -> saveOrUpdateTank());
    }

    private void populateUI(BallastRecord record) {
        editTextTankNo.setText(record.getBallastTankNo());
        //editTextTankType.setText(record.getBallastTankType()); // eski
        autoCompleteTankType.setText(record.getBallastTankType());//yeni
        editTextTankCapacity.setText(String.valueOf(record.getBallastTankCapacity()));
        editTextTankSounding.setText(String.valueOf(record.getBallastTankSounding()));
        editTextDensity.setText(String.valueOf(record.getDensity()));
        editTextTankVolume.setText(String.valueOf(record.getBallastTankVolume()));
        // Ağırlık, hacim ve yoğunluktan hesaplanacağı için doğrudan set etmeye gerek yok,
        // TextWatcher'lar bunu halledecektir. Ya da doğrudan da set edebilirsiniz:
        textViewTankWeight.setText(String.format(Locale.getDefault(), "%.3f MT", record.getBallastTankWeight()));
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
            new TimePickerDialog(CreateBallastTankActivity.this, (timeView, hourOfDay, minute) -> {
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


    private void calculateAndDisplayWeight() {
        String volumeStr = editTextTankVolume.getText().toString().trim();
        String densityStr = editTextDensity.getText().toString().trim();

        if (!TextUtils.isEmpty(volumeStr) && !TextUtils.isEmpty(densityStr)) {
            try {
                double volume = Double.parseDouble(volumeStr);
                double density = Double.parseDouble(densityStr);
                double weight = volume * density;
                textViewTankWeight.setText(String.format(Locale.getDefault(), "%.3f MT", weight));
            } catch (NumberFormatException e) {
                textViewTankWeight.setText("Invalid Input");
            }
        } else {
            textViewTankWeight.setText("0.000 MT");
        }
    }

    private void saveOrUpdateTank() {
        String tankNo = editTextTankNo.getText().toString().trim();
       //String tankType = editTextTankType.getText().toString().trim(); // String ise
        String tankType = autoCompleteTankType.getText().toString().trim();
        String capacityStr = editTextTankCapacity.getText().toString().trim();
        String soundingStr = editTextTankSounding.getText().toString().trim();
        String densityStr = editTextDensity.getText().toString().trim();
        String volumeStr = editTextTankVolume.getText().toString().trim();

        // Temel doğrulamalar
        if (TextUtils.isEmpty(tankNo) || TextUtils.isEmpty(capacityStr) ||
                TextUtils.isEmpty(soundingStr) || TextUtils.isEmpty(densityStr) || TextUtils.isEmpty(volumeStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double capacity = Double.parseDouble(capacityStr);
            double sounding = Double.parseDouble(soundingStr);
            double density = Double.parseDouble(densityStr);
            double volume = Double.parseDouble(volumeStr);
            double weight = volume * density; // Ağırlığı burada kesin olarak hesapla
// Kullanıcının seçtiği veya başlangıçta ayarlanan tarihi al
            Date finalDateTime = selectedDateTime.getTime();
            BallastRecord ballastRecord = new BallastRecord(tankNo, tankType, capacity, sounding, density, volume, weight, finalDateTime);

            if (currentBallastId == DEFAULT_ID) {
                // Yeni tank oluşturma
                ballastViewModel.insert(ballastRecord);
                Toast.makeText(this, "Ballast tank created", Toast.LENGTH_SHORT).show();
            } else {
                // Mevcut tankı güncelleme
                ballastRecord.setId(currentBallastId); // Güncellenecek kaydın ID'sini set et
                ballastViewModel.update(ballastRecord);
                Toast.makeText(this, "Ballast tank updated", Toast.LENGTH_SHORT).show();
            }
            finish(); // Activity'yi kapat ve önceki ekrana dön

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for capacity, sounding, density, and volume", Toast.LENGTH_LONG).show();
        }
    }
}

