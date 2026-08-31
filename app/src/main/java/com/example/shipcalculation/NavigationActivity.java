package com.example.shipcalculation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.OutputStream;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.IntStream;


public class NavigationActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private TextView txtCog, txtSpeed, txtWarning, txtDateTime, txtLat, txtLon, txtGpsStatus;
    ;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    private long lastGnssUpdateTime = 0;
    private ImageView imgSignal;

    private Handler logHandler = new Handler();
    private Runnable logRunnable;
    private boolean isLogging = false;
    private int intervalMinutes = 15;
    private Button btnLogToggle, btnShareLogs;
    private float filteredCog = 0f;
    private boolean cogInitialized = false;
    private final float cogAlpha = 0.08f; // daha yumuşak istersen 0.05 yap


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_navigation);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        txtLat = findViewById(R.id.txtLat);
        txtLon = findViewById(R.id.txtLon);
        txtDateTime = findViewById(R.id.txtDateTime);
        txtCog = findViewById(R.id.txtCog);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtWarning = findViewById(R.id.txtWarning);
        txtGpsStatus = findViewById(R.id.txtGpsStatus);
        imgSignal = findViewById(R.id.imgSignal);
        btnLogToggle = findViewById(R.id.btnLogToggle);
        btnShareLogs = findViewById(R.id.btnShareLogs);

        CheckBox checkboxKeepScreenOn = findViewById(R.id.checkboxKeepScreenOn);

        //txtDateTime.setText(sdf.format(new Date()));
        handler.post(timeUpdater); // Saat başlasın
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsStatusHandler.post(gpsTimeoutChecker);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(gnssCallback);
        }
        fetchGPS();
        checkLocationService();
        resetFields();
        // Sayfa açıldığında varsayılan olarak ekran açık olsun
        if (checkboxKeepScreenOn.isChecked()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        checkboxKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        // Log Başlat / Durdur
        btnLogToggle.setOnClickListener(v -> {
            NumberPicker numberPicker = findViewById(R.id.numberPickerInterval);
            int interval = numberPicker.getValue();

            if (!isLogging) {
                try {
                    intervalMinutes = numberPicker.getValue(); // ✔️ NumberPicker'dan alıyoruz
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter minute", Toast.LENGTH_SHORT).show();
                    return;
                }

                isLogging = true;
                Toast.makeText(this, "Kayıt başladı", Toast.LENGTH_SHORT).show();

                btnLogToggle.setText("Stop Log");
                btnLogToggle.setBackground(getResources().getDrawable(R.drawable.item_btn_record));
                btnLogToggle.setTextColor(getResources().getColor(R.color.white));
                startLogging();
            } else {
                stopLogging();
                btnLogToggle.setText("Start Log");
                btnLogToggle.setBackground(getResources().getDrawable(R.drawable.item_btn_background));
                btnLogToggle.setTextColor(getResources().getColor(R.color.btn_blue));
            }
        });
        // Paylaşım Butonu
        btnShareLogs.setOnClickListener(v -> shareLogFile());

        NumberPicker numberPicker = findViewById(R.id.numberPickerInterval);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(120);
        numberPicker.setValue(30); // varsayılan 30 dk

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            // Yeni aralık değeri alındı
            int selectedInterval = newVal;
            //Toast.makeText(this, "Kayıt Aralığı: " + selectedInterval + " dk", Toast.LENGTH_SHORT).show();

            // İsteğe bağlı olarak sakla
            SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
            prefs.edit().putInt("interval_minutes", selectedInterval).apply();
        });
        findViewById(R.id.btnViewLogs).setOnClickListener(v ->
                startActivity(new Intent(this, LogListActivity.class))
        );
// COG TextView’e tıklanınca compass ekranı
        // NavigationActivity içinde
        txtCog.setOnClickListener(v -> {
            Intent i = new Intent(NavigationActivity.this, CompassActivity.class);
            i.putExtra("lat", txtLat.getText().toString());
            i.putExtra("lon", txtLon.getText().toString());
            i.putExtra("sog", txtSpeed.getText().toString());
            i.putExtra("cog", txtCog.getText().toString());
            startActivity(i);
        });
    }

    private void startLogging() {

        // Kullanıcının seçtiği intervalMinutes bilgisini servise gönderiyoruz
        Intent svc = new Intent(NavigationActivity.this, GPSLogService.class);
        svc.putExtra(GPSLogService.EXTRA_INTERVAL_MIN, intervalMinutes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }

        isLogging = true;

        Toast.makeText(this, "Kayıt başladı (arka planda devam ediyor)", Toast.LENGTH_SHORT).show();
    }


    private void stopLogging() {

        // Foreground Servis durduruluyor
        Intent svc = new Intent(NavigationActivity.this, GPSLogService.class);
        stopService(svc);

        isLogging = false;

        Toast.makeText(this, "Kayıt sona erdi", Toast.LENGTH_SHORT).show();
    }



    // ✔ DÜZELTİLEN DOSYA KAYIT YÖNTEMİ
    private void saveLogEntry() {

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String lat = txtLat.getText().toString();
        String lon = txtLon.getText().toString();
        String cog = txtCog.getText().toString();
        String sog = txtSpeed.getText().toString();

        String csvLine = timestamp + ";" + lat + ";" + lon + ";" +
                cog.replace("°", " deg") + ";" + sog + "\n";

        // ✔ Android 11+ için doğru yöntem
        File logFile = new File(getExternalFilesDir(null), "gps_log.csv");

        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.append(csvLine);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Dosya yazma hatası!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLogFile() {
        File logFile = new File(getExternalFilesDir(null), "gps_log.csv");

        if (!logFile.exists()) {
            Toast.makeText(this, "Kayıt dosyası bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }


        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                logFile
        );
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "gps_log.csv dosyasını paylaş"));
    }



    private void resetFields() {
        txtLat.setText("00 00.000 N");
        txtLon.setText("000 00.000 E");
        txtWarning.setText("Waiting GPS Signal. Be sure to turn on GPS. or Input data Manually");
        txtWarning.setVisibility(TextView.VISIBLE);
        txtCog.setText("");      // COG
        txtSpeed.setText("");    // Speed
    }

    private void checkLocationService() {
        android.location.LocationManager lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Lütfen GPS’i açın", Toast.LENGTH_LONG).show();
        }
    }

    /* ---------- GPS ---------- */
    private LocationCallback locationCallback;


    private void fetchGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    float speedMs = location.getSpeed();
                    float cogRaw = location.getBearing();
                    double cogDeg;
                    final float tempGpsBearing; // Önce ham veya NaN değeri belirle
                    if (location.hasBearing() && location.getSpeed() > 1.0f) { // Veya MIN_SPEED_FOR_COG_MPS
                        float bearingAccuracy = Float.NaN;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            bearingAccuracy = location.getBearingAccuracyDegrees();
                        }

                        // Yön doğruluğu kontrolü
                        if (!Float.isNaN(bearingAccuracy) && bearingAccuracy > 45f) { // Veya MAX_BEARING_ACCURACY_DEGREES
                            tempGpsBearing = Float.NaN;
                        } else {
                            tempGpsBearing = cogRaw; // cogRaw, location.getBearing() olmalı
                        }
                    } else {
                        tempGpsBearing = Float.NaN;
                    }
                    final boolean isCogValid = !Float.isNaN(tempGpsBearing);
                    if (isCogValid) {
                        if (!cogInitialized || Float.isNaN(filteredCog) /*Eğer filtre NaN ise yeniden başlat*/) {
                            filteredCog = tempGpsBearing;
                            cogInitialized = true;
                        } else {
                            float delta = ((tempGpsBearing - filteredCog + 540f) % 360f) - 180f;
                            filteredCog = (filteredCog + cogAlpha * delta + 360f) % 360f;
                        }
                        cogDeg = filteredCog; // UI için filtrelenmiş değer
                    } else {
                        cogDeg = Double.NaN; // UI için NaN
                        // cogInitialized = false; // Opsiyonel: Filtreyi sıfırla ki bir sonraki geçerli bearing'de taze başlasın
                        // Bu, "---" gösterimini artırabilir ama daha doğru bir yeniden başlatma sağlar.
                        // Eğer bu satır olmazsa, filteredCog son geçerli değerini korur,
                        // bu da geçici "---" durumlarından sonra daha yumuşak bir devamlılık sağlayabilir.
                    }


                    double speedKn = (location.hasSpeed() && location.getSpeed() > 1.0)
                            ? speedMs * 1.94384 : 0;

                    txtLat.setText(toDMM(lat, "N", "S"));
                    txtLon.setText(toDMM(lon, "E", "W"));

                    if (!Double.isNaN(cogDeg)) {
                        txtCog.setText(String.format(Locale.getDefault(), "COG : %06.1f°", cogDeg));
                    } else {
                        txtCog.setText("COG : ---°");
                    }

                    txtSpeed.setText(String.format(Locale.getDefault(), "SOG : %.1f kn", speedKn));
                    txtWarning.setVisibility(TextView.GONE);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    /* ---------- DMM <-> Decimal ---------- */
    public static String toDMM(double dec, String pos, String neg) {
        int deg = (int) Math.abs(dec);
        double min = (Math.abs(dec) - deg) * 60;
        return String.format(Locale.getDefault(), "%02d %06.3f %s", deg, min, dec >= 0 ? pos : neg);
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
    private final Handler handler = new Handler();
    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            //sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // UTC saati istersen
            String currentTime = sdf.format(new Date());
            txtDateTime.setText(currentTime);
            handler.postDelayed(this, 1000); // her saniye çalıştır
           // etDateTime.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date(location.getTime()))); // Konumun zamanını kullanmak isterseniz

        }
    };
    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {


        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            lastGnssUpdateTime = System.currentTimeMillis();

            int total = status.getSatelliteCount();
            int used = 0;

            int gps = 0, glonass = 0, galileo = 0, beidou = 0;

            for (int i = 0; i < total; i++) {
                if (status.usedInFix(i)) used++;

                int constType = status.getConstellationType(i);
                switch (constType) {
                    case GnssStatus.CONSTELLATION_GPS: gps++; break;
                    case GnssStatus.CONSTELLATION_GLONASS: glonass++; break;
                    case GnssStatus.CONSTELLATION_GALILEO: galileo++; break;
                    case GnssStatus.CONSTELLATION_BEIDOU: beidou++; break;
                    // Ekstra: QZSS, IRNSS, SBAS gibi sistemler varsa eklenebilir
                }
            }

            String info = "Fix: " + used + " / " + total + "\n"
                    + "GPS: " + gps + " | GLONASS: " + glonass + "\n"
                    + "Galileo: " + galileo + " | BeiDou: " + beidou;

            int finalUsed = used;
            runOnUiThread(() -> {
                txtGpsStatus.setText(info);

                if (finalUsed >= 4) {
                    txtGpsStatus.setTextColor(Color.GREEN);
                } else if (finalUsed > 0) {
                    txtGpsStatus.setTextColor(Color.YELLOW);
                } else {
                    txtGpsStatus.setTextColor(Color.RED);
                }
                float totalCn0 = 0f;
                int countCn0 = 0;

                for (int i = 0; i < status.getSatelliteCount(); i++) {
                    if (status.usedInFix(i)) {
                        float cn0 = status.getCn0DbHz(i);
                        if (cn0 > 0) {
                            totalCn0 += cn0;
                            countCn0++;
                        }
                    }
                }

                float avgCn0 = (countCn0 > 0) ? totalCn0 / countCn0 : 0;
                int iconRes;
                if (avgCn0 > 40) {
                    iconRes = R.drawable.ic_signal_strong;
                } else if (avgCn0 > 25) {
                    iconRes = R.drawable.ic_signal_good;
                } else if (avgCn0 > 10) {
                    iconRes = R.drawable.ic_signal_weak;
                } else {
                    iconRes = R.drawable.ic_signal_none;
                }
                int finalIconRes = iconRes;
                runOnUiThread(() -> imgSignal.setImageResource(finalIconRes));

            });
        }

    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsStatusHandler.removeCallbacks(gpsTimeoutChecker);

        handler.removeCallbacks(timeUpdater); // Durdur
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.unregisterGnssStatusCallback(gnssCallback);
        }
    }
    private final Handler gpsStatusHandler = new Handler();
    private final Runnable gpsTimeoutChecker = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now - lastGnssUpdateTime > 10000) { // 10 saniye geçtiyse
                runOnUiThread(() -> {
                    txtGpsStatus.setText("GPS: No Signal");
                    txtGpsStatus.setTextColor(Color.RED);
                    imgSignal.setImageResource(R.drawable.ic_signal_none);

                });
            }
            gpsStatusHandler.postDelayed(this, 2000); // her 2 saniyede bir kontrol et
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        fetchGPS();
        // Konum servisi tekrar açılmış olabilir
        requestLocationIfPermissionGranted();
    }
    private void requestLocationIfPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location != null) {
                    updateUIWithLocation(location);
                }
            });
        }
    }
    private void updateUIWithLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double cog = (location.hasBearing() && location.getSpeed() > 1.2) ? location.getBearing() : 0;
        double speedKn = (location.hasSpeed() && location.getSpeed() > 1.0) ? location.getSpeed() * 1.94384 : 0;
        //double cog = location.hasBearing() ? location.getBearing() : 0;
        //double speedKn = location.hasSpeed() ? location.getSpeed() * 1.94384 : 0; // m/s → knots

        txtLat.setText(toDMM(lat, "N", "S"));
        txtLon.setText(toDMM(lon, "E", "W"));
        txtCog.setText(String.format(Locale.getDefault(), "COG : %06.1f°", cog));
        txtSpeed.setText(String.format(Locale.getDefault(), "SOG : %.1f kn", speedKn));
        txtWarning.setVisibility(TextView.GONE);
    }

}