package com.example.shipcalculation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils; // Eksik import eklendi
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class CargoCalculation extends AppCompatActivity {

    FrameLayout frameLayout;
    private EditText StwFct, editTextFw, editTextBunker, editTextLubOil, editTextBallast, editTextOther, editTextConstant,
            Yuzde1, Yuzde2, Yuzde3, Yuzde4, Yuzde5, Yuzde6;
    private TextView no1, no2, no3,no4,no5,no6, totalCum3, total_mt, textCargo, text_Cu_Mt, totalyuzde, textViewDwt;
    FloatingActionButton button_save; // FloatingActionButton olarak kalmalı
    SharedPreferences pref;
    private LinearLayout layout3, layout4, layout5, layout6;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_cargo_calculation);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        frameLayout = findViewById(R.id.frameLayout);
        pref = getSharedPreferences("MyData", MODE_PRIVATE);

        // EditText ve TextView'ların findViewById çağrıları
        editTextFw = findViewById(R.id.editTextFw);
        editTextBunker = findViewById(R.id.editTextBunker);
        editTextBallast = findViewById(R.id.editTextBallast);
        editTextLubOil = findViewById(R.id.editTextLubOil);
        editTextOther = findViewById(R.id.editTextOther);
        editTextConstant = findViewById(R.id.editTextConstant);
        layout3 = findViewById(R.id.layout3);
        layout4 = findViewById(R.id.layout4);
        layout5 = findViewById(R.id.layout5);
        layout6 = findViewById(R.id.layout6);

        textCargo = findViewById(R.id.textCargo);
        textViewDwt = findViewById(R.id.textViewDwt);

        StwFct = findViewById(R.id.StwFct);
        no1 = findViewById(R.id.no1);
        no2 = findViewById(R.id.no2);
        no3 = findViewById(R.id.no3);
        no4 = findViewById(R.id.no4);
        no5 = findViewById(R.id.no5);
        no6 = findViewById(R.id.no6);

        Yuzde1 = findViewById(R.id.Yuzde1);
        Yuzde2 = findViewById(R.id.Yuzde2);
        Yuzde3 = findViewById(R.id.Yuzde3);
        Yuzde4 = findViewById(R.id.Yuzde4);
        Yuzde5 = findViewById(R.id.Yuzde5);
        Yuzde6 = findViewById(R.id.Yuzde6);

        totalyuzde = findViewById(R.id.totalyuzde);
        totalCum3 = findViewById(R.id.totalCum3);
        text_Cu_Mt = findViewById(R.id.text_Cu_Mt);
        total_mt = findViewById(R.id.total_mt);

        button_save = findViewById(R.id.button_save); // XML'deki ID ile aynı olmalı

        // SharedPreferences'dan EditText değerlerini yükle
        loadPreferences();

        // Sayfa açıldığında veriler doluysa hesaplamaları yap
        calculateAndDisplayData(); // YENİ: Sayfa açıldığında hesaplama yap



        button_save.setOnClickListener(v -> {
            savePreferences(); // YENİ: Kaydetme işlemini ayrı bir metoda taşıdık
            calculateAndDisplayData(); // YENİ: Kaydettikten sonra tekrar hesapla ve göster
        });
    }

    // YENİ METOT: SharedPreferences'dan değerleri yükler
    private void loadPreferences() {
        editTextFw.setText(pref.getString("edit_Fw", "0.0"));
        editTextBallast.setText(pref.getString("edit_Ballast", "0.0"));
        editTextBunker.setText(pref.getString("edit_Bunker", "0.0"));
        editTextLubOil.setText(pref.getString("edit_LubOil", "0.0"));
        editTextOther.setText(pref.getString("edit_Other", "0.0"));
        editTextConstant.setText(pref.getString("edit_Constant", "0.0"));

        Yuzde1.setText(pref.getString("yuzde_1", "100"));
        Yuzde2.setText(pref.getString("yuzde_2", "100"));
        Yuzde3.setText(pref.getString("yuzde_3", "100"));
        Yuzde4.setText(pref.getString("yuzde_4", "100"));
        Yuzde5.setText(pref.getString("yuzde_5", "100"));
        Yuzde6.setText(pref.getString("yuzde_6", "100"));

        StwFct.setText(pref.getString("stwc", "0.0"));
    }

    // YENİ METOT: EditText'lerdeki değerleri SharedPreferences'a kaydeder
    private void savePreferences() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("edit_Fw", editTextFw.getText().toString());
        editor.putString("edit_Ballast", editTextBallast.getText().toString());
        editor.putString("edit_Bunker", editTextBunker.getText().toString());
        editor.putString("edit_LubOil", editTextLubOil.getText().toString());
        editor.putString("edit_Other", editTextOther.getText().toString());
        editor.putString("edit_Constant", editTextConstant.getText().toString());

        // Yüzde ve StwFct değerleri de butona basıldığında güncelleniyorsa buraya eklenebilir.
        // Eğer bu değerler sadece başlangıçta yükleniyorsa ve kullanıcı tarafından değiştirilmiyorsa
        // tekrar kaydetmeye gerek olmayabilir. Ancak genellikle kullanıcı bu alanları da değiştirebilir.
        editor.putString("yuzde_1", Yuzde1.getText().toString());
        editor.putString("yuzde_2", Yuzde2.getText().toString());
        editor.putString("yuzde_3", Yuzde3.getText().toString());
        editor.putString("yuzde_4", Yuzde4.getText().toString());
        editor.putString("yuzde_5", Yuzde5.getText().toString());
        editor.putString("yuzde_6", Yuzde6.getText().toString());

        editor.putString("stwc", StwFct.getText().toString());

        editor.apply();
        Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show(); // Kullanıcıya geri bildirim
    }


    // YENİ METOT: Hesaplamaları yapar ve TextView'ları günceller
    @SuppressLint("SetTextI18n")
    private void calculateAndDisplayData() {
        try {
            String edit_Fw_str = editTextFw.getText().toString();
            String edit_Ballast_str = editTextBallast.getText().toString();
            String edit_Bunker_str = editTextBunker.getText().toString();
            String edit_LubOil_str = editTextLubOil.getText().toString();
            String edit_Other_str = editTextOther.getText().toString();
            String edit_Constant_str = editTextConstant.getText().toString();

            String yuzde_1_str = Yuzde1.getText().toString();
            String yuzde_2_str = Yuzde2.getText().toString();
            String yuzde_3_str = Yuzde3.getText().toString();
            String yuzde_4_str = Yuzde4.getText().toString();
            String yuzde_5_str = Yuzde5.getText().toString();
            String yuzde_6_str = Yuzde6.getText().toString();

            String stwc_str = StwFct.getText().toString();

            // SharedPreferences'dan kapasite değerlerini al
            String hold1Value_str = pref.getString("holdNo1Capacity", "");
            String hold2Value_str = pref.getString("holdNo2Capacity", "");
            String hold3Value_str = pref.getString("holdNo3Capacity", "0.0");
            String hold4Value_str = pref.getString("holdNo4Capacity", "0.0");
            String hold5Value_str = pref.getString("holdNo5Capacity", "0.0");
            String hold6Value_str = pref.getString("holdNo6Capacity", "0.0");
            String dwValue_str = pref.getString("dwtCapacity", "");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Gerekli alanların boş olup olmadığını kontrol et
            if (TextUtils.isEmpty(stwc_str) || stwc_str.equals("0.0") || TextUtils.isEmpty(hold1Value_str) ||
                    TextUtils.isEmpty(hold2Value_str) ||
                    TextUtils.isEmpty(dwValue_str)) {

                // Eğer EditText'ler boşsa veya varsayılan "0.0" ise ve kapasite bilgileri eksikse
                // kullanıcıyı bilgilendir.
                boolean allInputsEmpty = TextUtils.isEmpty(edit_Fw_str) && TextUtils.isEmpty(edit_Ballast_str) &&
                        TextUtils.isEmpty(edit_Bunker_str) && TextUtils.isEmpty(edit_LubOil_str) &&
                        TextUtils.isEmpty(edit_Other_str) && TextUtils.isEmpty(edit_Constant_str) &&
                        stwc_str.equals("0.0"); // StwFct de başlangıç değeriyle kontrol edilebilir

                if (TextUtils.isEmpty(hold1Value_str) || TextUtils.isEmpty(hold2Value_str) ||
                         TextUtils.isEmpty(dwValue_str)){
                    Toast.makeText(CargoCalculation.this, "Ship capacity data is missing. Please set it in settings or relevant screen.", Toast.LENGTH_LONG).show();
                    // Belki kullanıcıyı kapasite bilgilerini gireceği ekrana yönlendirebilirsiniz.
                    // clearResultFields(); // Sonuç alanlarını temizle
                    return; // Hesaplama yapma
                }

                if (stwc_str.equals("0.0") && !allInputsEmpty){ // StwFct "0.0" ama diğer alanlar doluysa uyar
                    Toast.makeText(CargoCalculation.this, "Please enter a valid Stowage Factor (StwFct).", Toast.LENGTH_LONG).show();
                    // clearResultFields();
                    return;
                }
                return; // Eğer kritik veriler eksikse hesaplama yapma
            }

            double Fw = Double.parseDouble(edit_Fw_str);
            double Ballast = Double.parseDouble(edit_Ballast_str);
            double Bunker = Double.parseDouble(edit_Bunker_str);
            double LubOil = Double.parseDouble(edit_LubOil_str);
            double Other = Double.parseDouble(edit_Other_str);
            double Constant = Double.parseDouble(edit_Constant_str);

            double no1Value = Double.parseDouble(hold1Value_str);
            double no2Value = Double.parseDouble(hold2Value_str);
            double no3Value = Double.parseDouble(hold3Value_str);
            double no4Value = Double.parseDouble(hold4Value_str);
            double no5Value = Double.parseDouble(hold5Value_str);
            double no6Value = Double.parseDouble(hold6Value_str);

            double dwtValue = Double.parseDouble(dwValue_str);

            double yuzde1Value = Double.parseDouble(yuzde_1_str);
            double yuzde2Value = Double.parseDouble(yuzde_2_str);
            double yuzde3Value = Double.parseDouble(yuzde_3_str);
            double yuzde4Value = Double.parseDouble(yuzde_4_str);
            double yuzde5Value = Double.parseDouble(yuzde_5_str);
            double yuzde6Value = Double.parseDouble(yuzde_6_str);

            double stwcValue = Double.parseDouble(stwc_str);
            if (stwcValue == 0) { // Sıfıra bölme hatasını önle
                Toast.makeText(CargoCalculation.this, "Stowage Factor (StwFct) cannot be zero.", Toast.LENGTH_LONG).show();
                clearResultFields(); // Sonuç alanlarını temizle veya varsayılana döndür
                return;
            }
            double Mtm3 = 35.875 / stwcValue;
            // double mtm3Value = Double.parseDouble(String.valueOf(Mtm3)); // Bu satır gereksiz, Mtm3 zaten double

            double HoldNo1 = (no1Value * Mtm3) * yuzde1Value / 100;
            double HoldNo2 = (no2Value * Mtm3) * yuzde2Value / 100;
            double HoldNo3 = (no3Value * Mtm3) * yuzde3Value / 100;
            double HoldNo4 = (no4Value * Mtm3) * yuzde4Value / 100;
            double HoldNo5 = (no5Value * Mtm3) * yuzde5Value / 100;
            double HoldNo6 = (no6Value * Mtm3) * yuzde6Value / 100;

            double loadCapacity = dwtValue - (Fw + Ballast + Bunker + LubOil + Other + Constant);
            double totalMtValue = (HoldNo1 + HoldNo2 + HoldNo3 + HoldNo4 + HoldNo5 + HoldNo6);
            double totalCuM3Value = no1Value + no2Value + no3Value + no4Value + no5Value + no6Value; // Bu, hold kapasitelerinin toplamı, M/T cinsinden değil, Cu.M3
            //double totalyuzdeValue = (yuzde1Value + yuzde2Value + yuzde3Value + yuzde4Value + yuzde5Value + yuzde6Value) / 6;//????? bura da hesaplama kaç ambar varsa o kadar olacak??????

            int activeHoldCount = 0;
            double totalYuzde = 0.0;

            if (no1Value > 0) { totalYuzde += yuzde1Value; activeHoldCount++; }
            if (no2Value > 0) { totalYuzde += yuzde2Value; activeHoldCount++; }
            if (no3Value > 0) { totalYuzde += yuzde3Value; activeHoldCount++; }
            if (no4Value > 0) { totalYuzde += yuzde4Value; activeHoldCount++; }
            if (no5Value > 0) { totalYuzde += yuzde5Value; activeHoldCount++; }
            if (no6Value > 0) { totalYuzde += yuzde6Value; activeHoldCount++; }

            double totalyuzdeValue = activeHoldCount > 0 ? totalYuzde / activeHoldCount : 0.0;

            textCargo.setText(String.format(Locale.US, "Total Available Capacity: %.3f M/T", loadCapacity));
            if (loadCapacity < totalMtValue) {
                total_mt.setBackgroundResource(R.drawable.border); // Kırmızı çerçeve
                total_mt.setBackgroundColor(Color.RED);
                total_mt.setTextColor(Color.WHITE);
                Toast.makeText(CargoCalculation.this, "Attention! Exceeds the loading Capacity!",
                        Toast.LENGTH_LONG).show();
            } else {
                total_mt.setBackgroundColor(Color.GREEN);
                total_mt.setTextColor(Color.BLACK);
                total_mt.setBackgroundResource(R.drawable.back_green); // Yeşil arka plan
            }
            no1.setText(String.format(Locale.US, "Hold 1: %.3f M/T", HoldNo1));
            no2.setText(String.format(Locale.US, "Hold 2: %.3f M/T", HoldNo2));
            no3.setText(String.format(Locale.US, "Hold 3: %.3f M/T", HoldNo3));
            no4.setText(String.format(Locale.US, "Hold 4: %.3f M/T", HoldNo4));
            no5.setText(String.format(Locale.US, "Hold 5: %.3f M/T", HoldNo5));
            no6.setText(String.format(Locale.US, "Hold 6: %.3f M/T", HoldNo6));
            totalCum3.setText(String.format(Locale.US, "Total Hold Capacity: %.3f m³", totalCuM3Value)); // Etiketi düzelttim
            total_mt.setText(String.format(Locale.US, "Total  :  %.3f M/T", totalMtValue)); // Etiketi düzelttim
            text_Cu_Mt.setText(String.format(Locale.US, "%.3f M.T/m³", Mtm3)); // mtm3Value yerine Mtm3 kullandım
            textViewDwt.setText(String.format(Locale.US, " D.W.T : %.3f M/T", dwtValue));
            if (no3Value > 0 && yuzde3Value > 0) {
                double holdNo3 = (no3Value * Mtm3) * yuzde3Value / 100;
                HoldNo3 = holdNo3; // ana toplam için
                no3.setVisibility(View.VISIBLE);
                no3.setText(String.format(Locale.US, "Hold 3: %.3f M/T", holdNo3));
            } else {
                HoldNo3 = 0.0;
                no3.setVisibility(View.GONE);
                Yuzde3.setVisibility(View.GONE);
                layout3.setVisibility(View.GONE);

            }

            if (no4Value > 0 && yuzde4Value > 0) {
                double holdNo4 = (no4Value * Mtm3) * yuzde4Value / 100;
                HoldNo4 = holdNo4; // ana toplam için
                no4.setVisibility(View.VISIBLE);
                no4.setText(String.format(Locale.US, "Hold 4: %.3f M/T", holdNo4));
            } else {
                HoldNo4 = 0.0;
                no4.setVisibility(View.GONE);
                Yuzde4.setVisibility(View.GONE);
                layout4.setVisibility(View.GONE);

            }
            if (no5Value > 0 && yuzde5Value > 0) {
                double holdNo5 = (no5Value * Mtm3) * yuzde5Value / 100;
                HoldNo5 = holdNo5; // ana toplam için
                no5.setVisibility(View.VISIBLE);
                no5.setText(String.format(Locale.US, "Hold 5: %.3f M/T", holdNo5));
            } else {
                HoldNo5 = 0.0;
                no5.setVisibility(View.GONE);
                Yuzde5.setVisibility(View.GONE);
                layout5.setVisibility(View.GONE);

            }
            if (no6Value > 0 && yuzde6Value > 0) {
                double holdNo6 = (no6Value * Mtm3) * yuzde6Value / 100;
                HoldNo6 = holdNo6; // ana toplam için
                no6.setVisibility(View.VISIBLE);
                no6.setText(String.format(Locale.US, "Hold 6: %.3f M/T", holdNo6));
            } else {
                HoldNo6 = 0.0;
                no6.setVisibility(View.GONE);
                Yuzde6.setVisibility(View.GONE);
                layout6.setVisibility(View.GONE);

            }

            if (totalyuzdeValue == 100) {
                totalyuzde.setText("Full");
                totalyuzde.setBackgroundColor(Color.YELLOW);
                totalyuzde.setTextColor(Color.RED);
            } else if (totalyuzdeValue == 0 && (yuzde1Value == 0 && yuzde2Value == 0 && yuzde3Value == 0)){
                totalyuzde.setText("0.0"); // Hepsi 0 ise "0.0" yazsın
                totalyuzde.setBackgroundResource(R.drawable.back_green);
                totalyuzde.setTextColor(Color.BLACK);
            }
            else {
                totalyuzde.setText(String.format(Locale.US, "%.1f %%", totalyuzdeValue)); // Yüzde işareti eklendi
                totalyuzde.setBackgroundResource(R.drawable.back_green);
                totalyuzde.setTextColor(Color.BLACK);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(CargoCalculation.this, "Please Enter Valid Numerical Data.", Toast.LENGTH_SHORT).show();
            Log.e("CargoCalculation", "Invalid number format: " + e.getMessage());
            clearResultFields(); // Hata durumunda sonuçları temizle
        } catch (Exception e) {
            Toast.makeText(CargoCalculation.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("CargoCalculation", "Error happened: " + e.getMessage());
            clearResultFields(); // Hata durumunda sonuçları temizle
        }
    }

    private double getDoubleFromPrefs(SharedPreferences prefs, String key) {
        String value = prefs.getString(key, "0.0");
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    // YENİ METOT: Sonuç TextView'larını temizler veya varsayılan değerlere döndürür
    @SuppressLint("SetTextI18n")
    private void clearResultFields() {
        textCargo.setText("Total Available Capacity: -");
        no1.setText("- M/T");
        no2.setText("- M/T");
        no3.setText("- M/T");
        no4.setText("- M/T");
        no5.setText("- M/T");
        no6.setText("- M/T");
        totalCum3.setText("Total Hold Capacity: - Cu.M3");
        total_mt.setText("Total Calculated Cargo: - M/T");
        text_Cu_Mt.setText("- M.T/Cu.M3");
        textViewDwt.setText("D.W.T : - M/T");
        totalyuzde.setText("- %");
        totalyuzde.setBackgroundResource(R.drawable.border); // Veya varsayılan arka plan
        totalyuzde.setTextColor(Color.BLACK);
        total_mt.setBackgroundResource(R.drawable.border); // Varsa varsayılan arka plan
        total_mt.setTextColor(Color.BLACK);
    }
}