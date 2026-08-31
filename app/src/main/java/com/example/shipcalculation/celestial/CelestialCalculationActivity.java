package com.example.shipcalculation.celestial;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.media3.common.util.UnstableApi;

import com.example.shipcalculation.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.*;

public class CelestialCalculationActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private Spinner spinnerBody;
    private EditText etLat, etLon, etDateTime,etObsBearing,etGyroHeading,etMagHeading,etVariation;
    private TextView txtCoords, txtResult,txtWarning,txtCompassResult,txtVariationDirection;
    private SwitchMaterial switchManualMode;
    private Button btnKonumAl,btnHesapla,btnVariation;
    private final String[] bodies = {"Sun", "Moon","Polaris", "Mars"};
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    private double currentCalculatedAzimuth = -1.0; // Başlangıçta geçersiz bir değer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_celestial_calculation);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        spinnerBody = findViewById(R.id.spinnerGokCismi);
        etLat       = findViewById(R.id.etLat);
        etLon       = findViewById(R.id.etLon);
        etDateTime  = findViewById(R.id.etDateTime);
        txtCoords   = findViewById(R.id.txtKoordinatlar);
        txtResult   = findViewById(R.id.txtSonuc);
        txtWarning = findViewById(R.id.txtWarning);
        txtCompassResult = findViewById(R.id.txtCompassResult);
        etObsBearing = findViewById(R.id.etObsBearing);
        etGyroHeading = findViewById(R.id.etGyroHeading);
        etMagHeading = findViewById(R.id.etMagHeading);
        etVariation = findViewById(R.id.etVariation);
        txtVariationDirection = findViewById(R.id.txtVariationDirection);
        switchManualMode = findViewById(R.id.switchManualMode);
        btnKonumAl = findViewById(R.id.btnKonumAl);
        btnHesapla = findViewById(R.id.btnHesapla);
        btnVariation = findViewById(R.id.btnVariation);
        switchManualMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { // Manuel Mod Aktif
                Toast.makeText(CelestialCalculationActivity.this, "Manuel giriş modu aktif.", Toast.LENGTH_SHORT).show();
                btnKonumAl.setBackgroundResource(R.drawable.item_btn_manual);
                txtWarning.setText("Add Manual Values!\nBe sure correct format!\nPush to GPS Button.");
                // Manuel modda tüm ilgili alanları düzenlenebilir yap
                setFieldsEditable(true);

            } else { // Manuel Mod Kapalı (GPS Modu)
                Toast.makeText(CelestialCalculationActivity.this, "GPS modu aktif.", Toast.LENGTH_SHORT).show();
                btnKonumAl.setBackgroundResource(R.drawable.btn_custom); // Orijinal drawable
                txtWarning.setText("Push to GPS Button.\nWait for GPS Signal!\nBe sure GPS is on!\nOr Enter Manual Values.");
                // GPS modunda alanları düzenlenemez yap
                setFieldsEditable(false);

                // GPS moduna geçildiğinde, eğer GPS'ten taze veri alınmayacaksa
                // mevcut değerleri koruyabilir veya sıfırlayabilirsiniz.
                // Şimdilik sadece düzenlenebilirliği kapatıyoruz.
                // Eğer fetchGPS() çağrılacaksa, o zaten alanları güncelleyecektir.
            }
        });
        setFieldsEditable(switchManualMode.isChecked());
        etVariation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateVariationDirection();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                bodies);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerBody.setAdapter(adapter);


        // Tarih/saat alanını otomatik doldur
        etDateTime.setText(sdf.format(new Date()));

        checkLocationService();
        btnKonumAl.setOnClickListener(v -> {
            if (switchManualMode.isChecked()) {

                calculateManual();
                Toast.makeText(CelestialCalculationActivity.this, "Manuel mod aktif. GPS kullanılmayacak.", Toast.LENGTH_LONG).show();
            } else {
                fetchGPS(); // Bu metot içinde calculateManual() çağrılıyor
            }
        });

        btnHesapla.setOnClickListener(v -> {
            if (switchManualMode.isChecked()) { // Manuel Modda
                // Önce Azimut/Alt hesapla (kullanıcının girdiği değerlerle)

                if (currentCalculatedAzimuth == -1.0) {
                    // calculateManual içinde zaten Toast gösterilmiş olmalı
                    return; // Azimut hesaplanamadıysa devam etme
                }
            } else { // GPS Modunda (Azimut'un fetchGPS ile hesaplandığını varsayıyoruz)
                if (currentCalculatedAzimuth == -1.0) {
                    Toast.makeText(this, "Lütfen önce 'Konum Al' ile GPS ve Azimut bilgilerini alın.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Hem manuel modda azimut hesaplandıktan sonra, hem de GPS modunda azimut zaten varsa
            // pusula hatalarını hesapla
            if (etObsBearing.getText().toString().trim().isEmpty() ||
                    etGyroHeading.getText().toString().trim().isEmpty() ||
                    etMagHeading.getText().toString().trim().isEmpty() ||
                    etVariation.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Pusula hatası hesaplaması için lütfen tüm kerteriz ve pruva değerlerini girin.", Toast.LENGTH_LONG).show();
                return;
            }
            calculateCompassErrors();
        });
        resetFields();
        btnVariation.setOnClickListener(v -> {
            try {
                double lat = fromDMM(etLat.getText().toString());
                double lon = fromDMM(etLon.getText().toString());

                GeomagneticField field = new GeomagneticField(
                        (float) lat,
                        (float) lon,
                        0f, // yükseklik yoksa sıfır kabul edilir
                        System.currentTimeMillis()
                );

                float var = field.getDeclination(); // manyetik sapma
                etVariation.setText(String.format(Locale.getDefault(), "%.1f", var));
                Toast.makeText(this, "Manyetik sapma otomatik güncellendi", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Konum bilgisi eksik", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // MainActivity.java

// ... (diğer metotlar ve üye değişkenleriniz, currentCalculatedAzimuth dahil)

    @OptIn(markerClass = UnstableApi.class) private void calculateCompassErrors() {
        if (currentCalculatedAzimuth == -1.0) {
            Toast.makeText(this, "Azimuth not calculated yet. Please calculate celestial body position first.", Toast.LENGTH_LONG).show();
            txtCompassResult.setText("Error: Azimuth not available.");
            return;
        }

        try {
            double trueBearing = normalize(currentCalculatedAzimuth);
            double gyroBearing = normalize(Double.parseDouble(etObsBearing.getText().toString().trim().replace(',', '.')));
            double gyroHeading = normalize(Double.parseDouble(etGyroHeading.getText().toString().trim().replace(',', '.')));
            double compassHeading = normalize(Double.parseDouble(etMagHeading.getText().toString().trim().replace(',', '.')));
            double variation = normalizeSigned(Double.parseDouble(etVariation.getText().toString().trim().replace(',', '.')));

            double gyroError = normalizeSigned(trueBearing - gyroBearing);
            String gyroErrorDir = gyroError >= 0 ? "E" : "W";
            double trueHeading = normalize(gyroHeading + gyroError);
            double compassError = normalizeSigned(trueHeading - compassHeading);
            String compassErrorDir = compassError >= 0 ? "E" : "W";
            double deviation = normalizeSigned(compassError - variation);
            String deviationDir = deviation >= 0 ? "E" : "W";
            String variationDir = variation >= 0 ? "E" : "W";

            // Hizalama için etiketlerin maksimum genişliğini belirleyin (örneğin 28 karakter)
            int labelWidth = 25; // Bu değeri en uzun etiketinize göre ayarlayın

            String resultText = String.format(Locale.getDefault(),
                    "%-" + labelWidth + "s : %05.1f°\n" +               // True Bearing (Azimuth)
                            "%-" + labelWidth + "s: %05.1f°\n\n" +            // Gyro Bearing (Observed)
                            "%-" + labelWidth + "s     : %.1f° %s\n" +           // Gyro Error (GE)
                            "%-" + labelWidth + "s : %05.1f°\n" +              // Gyro Heading (GH)
                            "%-" + labelWidth + "s : %05.1f°\n\n" +            // True Heading (TH)
                            "%-" + labelWidth + "s           : %.1f° %s\n" +           // Variation (Var)
                            "%-" + labelWidth + "s: %05.1f°\n" +              // Compass Heading (CH)
                            "%-" + labelWidth + "s     : %.1f° %s\n" +           // Compass Error (CE)
                            "%-" + labelWidth + "s         : %.1f° %s",              // Deviation (Dev)

                    "True Bearing (Azimuth)", trueBearing,
                    "Gyro Bearing (Observed)", gyroBearing,
                    "Gyro Error (GE)", Math.abs(gyroError), gyroErrorDir,
                    "Gyro Heading (GH)", gyroHeading,
                    "True Heading (TH)", trueHeading,
                    "Variation (Var)", Math.abs(variation), variationDir,
                    "Compass Heading (CH)", compassHeading,
                    "Compass Error (CE)", Math.abs(compassError), compassErrorDir,
                    "Deviation (Dev)", Math.abs(deviation), deviationDir
            );


            txtCompassResult.setText(resultText);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter all numeric fields correctly (e.g., 123.4)", Toast.LENGTH_SHORT).show();
            txtCompassResult.setText("Error: Invalid input format.");
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred during compass error calculation.", Toast.LENGTH_SHORT).show();
            txtCompassResult.setText("Error in calculation.");
            androidx.media3.common.util.Log.e("CompassError", "Calculation error: ", e);
        }
    }

    // Normalize açıları [0,360) aralığına getirir
    private double normalize(double angle) {
        angle %= 360;
        if (angle < 0) angle += 360;
        return angle;
    }

    // Normalize açıları [-180,180) aralığına getirir (hatalar ve sapmalar için)
    private double normalizeSigned(double angle) {
        // Bu yöntem -180 ile +180 arasında bir değer döndürür.
        // Örneğin, -181 -> +179, 361 -> +1, -179 -> -179
        double normalized = angle % 360;
        if (normalized > 180) {
            normalized -= 360;
        } else if (normalized <= -180) { // <= kullandığımıza dikkat, -180 için +180 olmasını istemiyorsak
            normalized += 360;
        }
        return normalized;
    }

    private void resetFields() {
        etLat.setText("00 00.000 N");
        etLon.setText("000 00.000 E");
        txtWarning.setText("Push to GPS Button.\nWait for GPS Signal!\nBe sure GPS is on!\nOr Enter Manual Values.");
        txtWarning.setVisibility(TextView.VISIBLE);
        txtCoords.setText("");
        txtResult.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        // etDateTime tıklandığında Date Time Picker'ı sadece Manuel Mod'da aç
        etDateTime.setOnClickListener(v -> {
            if (switchManualMode.isChecked()) { // Sadece Manuel Mod aktifse aç
                openDateTimePicker();
            } else {
                Toast.makeText(CelestialCalculationActivity.this, "Date/Time can only be set in Manual Mode.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Tarih-saat seçiciyi aç */
    private void openDateTimePicker() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);
        int h = now.get(Calendar.HOUR_OF_DAY);
        int min = now.get(Calendar.MINUTE);

        DatePickerDialog dateDlg = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timeDlg = new TimePickerDialog(this,
                            (v, hourOfDay, minute) -> {
                                Calendar selected = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                selected.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                etDateTime.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",
                                        Locale.getDefault()).format(selected.getTime()));
                            }, h, min, true);
                    timeDlg.show();
                }, y, m, d);
        dateDlg.show();
    }
    private void checkLocationService() {
        android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please Activate GPS !", Toast.LENGTH_LONG).show();
        }
    }

    /* ---------- GPS ---------- */
    private void fetchGPS() {
        resetFields(); // Alanları sıfırla
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        txtWarning.setText("Fetching GPS data, please wait..."); // Kullanıcıya bilgi ver
        txtWarning.setVisibility(TextView.VISIBLE);
        txtResult.setText(""); // Önceki sonuçları temizle
        txtCompassResult.setText(""); // Önceki pusula hatalarını temizle
        currentCalculatedAzimuth = -1.0; // Azimutu sıfırla

        fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null) // Veya bir CancellationToken kullanabilirsiniz
                .addOnSuccessListener(this, location -> { // `this` veya lambda için uygun context
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        etLat.setText(toDMM(lat, "N", "S"));
                        etLon.setText(toDMM(lon, "E", "W"));

                        // Tarih/saat alanını GÜNCEL GMT zamanı ile doldur
                        etDateTime.setText(sdf.format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime()));
                        etDateTime.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date(location.getTime()))); // Konumun zamanını kullanmak isterseniz

                        txtWarning.setVisibility(TextView.GONE);
                        txtCoords.setText(String.format(Locale.getDefault(),
                                "Lat.: %s\nLong.: %s", etLat.getText(), etLon.getText()));

                        // GPS BAŞARIYLA ALINDIKTAN SONRA OTOMATİK OLARAK AZİMUT HESAPLA
                        calculateManual();

                        // Kullanıcıya diğer alanları doldurması için bir mesaj gösterebilirsiniz (opsiyonel)
                        if (currentCalculatedAzimuth != -1.0) {
                            Toast.makeText(CelestialCalculationActivity.this, "Azimuth calculated. Enter observed values and press 'Calculate Errors'.", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(CelestialCalculationActivity.this, "No GPS Signal! – Be sure to turn on GPS or enter manually.", Toast.LENGTH_LONG).show();
                        txtWarning.setText("No GPS Signal!\nEnsure GPS is on or enter manually.");
                        resetFields(); // Veya sadece koordinatları ve sonucu sıfırla
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(CelestialCalculationActivity.this, "GPS Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    txtWarning.setText("GPS Error.\nPlease enter manually.");
                    resetFields();
                });
    }
    /* ---------- Manuel Hesaplama ---------- */
    private void calculateManual() {
        try {
            String body = spinnerBody.getSelectedItem().toString();
            double lat = fromDMM(etLat.getText().toString());
            double lon = fromDMM(etLon.getText().toString());

            Date date = sdf.parse(etDateTime.getText().toString());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.setTime(date);

            double[] altAz = UltraPrecise.compute(body, lat, lon, cal);
            double altitude = altAz[0];
            double azimuth = altAz[1]; // Hesaplanan azimut
            // Hesaplanan azimut değerini üye değişkene ata
            currentCalculatedAzimuth = normalize(azimuth); // Normalize etmeyi unutmayın

            String dir = direction(currentCalculatedAzimuth); // Normalize edilmiş azimutu kullan
            txtCoords.setText(String.format(Locale.getDefault(),
                    "Lat.: %s\nLong.: %s", etLat.getText(), etLon.getText()));
            txtResult.setText(String.format(Locale.getDefault(),
                    "%s:\nAltitude : %.2f°\nAzimuth: %s (%.2f°)",
                    body, altitude, dir, currentCalculatedAzimuth)); // Burada da normalize edilmiş değeri göster


        } catch (Exception e) {
            currentCalculatedAzimuth = -1.0; // Hata durumunda geçersiz kıl
            Toast.makeText(this, "Please Check Values:\nDD MM.MMM N/E", Toast.LENGTH_LONG).show();
            txtResult.setText("Error in calculation."); // Veya boş bırakın
        }
    }


    /* ---------- DMM <-> Decimal ---------- */
    private String toDMM(double dec, String pos, String neg) {
        int deg = (int) Math.abs(dec);
        double min = (Math.abs(dec) - deg) * 60;
        return String.format(Locale.getDefault(), "%02d %06.3f %s", deg, min, dec >= 0 ? pos : neg);
    }

    private double fromDMM(String raw) {
        // 1) Büyük/küçük harf, fazla boşluk, virgül temizliği
        String s = raw.trim().toUpperCase()
                .replace(',', '.')
                .replaceAll("\\s{2,}", " ")
                .replaceAll("[^0-9NEWS. ]", "");

        // 2) Parçaları al (örn. "40 23.456 N")
        String[] p = s.split("\\s+");
        if (p.length != 3) throw new IllegalArgumentException("3 will be splitted: dd mm.mmm N/E");

        double deg = Double.parseDouble(p[0]);
        double min = Double.parseDouble(p[1]);
        char hem   = p[2].charAt(0);          // N, S, E, W

        double dec = deg + min / 60.0;
        return (hem == 'S' || hem == 'W') ? -dec : dec;
    }

    /* ---------- Yön ---------- */
    private String direction(double az) {
        az = (az % 360 + 360) % 360;
        if (az < 22.5 || az >= 337.5) return "N";
        if (az < 67.5) return "NE";
        if (az < 112.5) return "E";
        if (az < 157.5) return "SE";
        if (az < 202.5) return "S";
        if (az < 247.5) return "SW";
        if (az < 292.5) return "W";
        return "NW";
    }

    /* ---------- İzin ---------- */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchGPS();
        }
    }
    // Variation yönünü (E/W) güncelleyen metot
    private void updateVariationDirection() {
        String variationStr = etVariation.getText().toString().trim();
        if (!variationStr.isEmpty() && !variationStr.equals("-") && !variationStr.equals(".")) { // Geçerli bir sayıya benziyorsa
            try {
                double variationValue = Double.parseDouble(variationStr.replace(',', '.'));
                // normalizeSigned burada kullanılmaz, çünkü kullanıcının girdiği +/- işaretini korumak istiyoruz
                // yönü belirlemek için.

                if (variationValue > 0) {
                    txtVariationDirection.setText("E");
                } else if (variationValue < 0) {
                    txtVariationDirection.setText("W");
                } else { // variationValue == 0
                    txtVariationDirection.setText(""); // Veya "0" ya da "-"
                }
            } catch (NumberFormatException e) {
                txtVariationDirection.setText("?"); // Hatalı giriş
            }
        } else {
            txtVariationDirection.setText(""); // Boş veya geçersiz giriş
        }
    }
    // Alanların düzenlenebilirlik durumunu ayarlayan yardımcı metot
    private void setFieldsEditable(boolean editable) {
        // Latitude
        etLat.setFocusable(editable);
        etLat.setFocusableInTouchMode(editable);
        // etLat.setClickable(editable); // Genellikle EditText için focusable yeterlidir,
        // onClickListener'ı yoksa clickable'a gerek kalmaz.

        // Longitude
        etLon.setFocusable(editable);
        etLon.setFocusableInTouchMode(editable);
        // etLon.setClickable(editable);

        // DateTime
        etDateTime.setFocusable(editable);
        etDateTime.setFocusableInTouchMode(editable);
        // etDateTime için clickable'ı doğrudan yönetmiyoruz,
        // çünkü onClickListener'ı içinde zaten switchManualMode.isChecked() kontrolü var.
        // Ancak, görsel olarak tıklanamaz göstermek için isterseniz ekleyebilirsiniz.
        // etDateTime.setClickable(editable); // Bu, onClickListener'ı engellemez, sadece görsel etki yapar.

        // Eğer GPS modunda klavyenin hiç açılmamasını istiyorsanız:
        if (!editable) {
            // Klavyeyi gizle (eğer açıksa)
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}