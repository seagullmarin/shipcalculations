package com.example.shipcalculation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class CompassActivity extends AppCompatActivity {
    private SatelliteBarView satBarView;
    private LocationManager locationManager;
    private GnssStatus.Callback gnssCallback;
    private FusedLocationProviderClient fused;
    private LocationCallback locationCallback;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private GyroCompassView gyroView;
    private TextView txtLatLon, txtSog, txtCog, txtGpsStatus;

    private SensorManager sensorManager;
    private Sensor magnetometer, accelerometer;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float magneticHeading = 0f;   // manyetik 0-360
    private float filteredMag = 0f;
    private float trueHeading = 0f;
    private float declination = 0f;
    private float filteredCog = 0f;
    private boolean cogInitialized = false;
    private final float cogAlpha = 0.08f;  // Daha küçük = daha sabit


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_compass);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        gyroView   = findViewById(R.id.gyroView);
        txtLatLon  = findViewById(R.id.txtLatLon);
        txtCog     = findViewById(R.id.txtCog);
        txtSog     = findViewById(R.id.txtSog);
        txtGpsStatus = findViewById(R.id.txtGpsStatusCompass);
        satBarView = findViewById(R.id.satBarView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fused = LocationServices.getFusedLocationProviderClient(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        CheckBox checkboxKeepScreenOn = findViewById(R.id.checkboxKeepScreenOn);
        // ilk değerleri Navigation’dan al
        txtLatLon.setText(getIntent().getStringExtra("lat") + "  " +
                getIntent().getStringExtra("lon"));
        txtCog.setText(getIntent().getStringExtra("cog"));
        txtSog.setText(getIntent().getStringExtra("sog"));
        txtGpsStatus.setText("Waiting GPS..📡");
        txtGpsStatus.setTextColor(Color.RED);
        startLiveUpdates();


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
    }


    @SuppressLint("MissingPermission")
    private void startLiveUpdates() {
        LocationRequest req = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) {
                    runOnUiThread(() -> txtGpsStatus.setText("No GPS..📡"));
                    txtGpsStatus.setTextColor(Color.YELLOW);
                    return;
                }
                runOnUiThread(() -> txtGpsStatus.setText("GPS Active 📶"));
                txtGpsStatus.setTextColor(Color.GREEN);

                double lat = loc.getLatitude();
                double lon = loc.getLongitude();
                float alt = (float) loc.getAltitude();

                // Sapma hesapla (manyetik - gerçek kuzey farkı)
                GeomagneticField geoField = new GeomagneticField(
                        (float) lat,
                        (float) lon,
                        alt,
                        System.currentTimeMillis()
                );
                declination = geoField.getDeclination(); // true north düzeltmesi
                gyroView.setDeclination(declination);
                // derece
                // COG (GPS yönü)
                final float gpsBearingFinal;
                if (loc.hasBearing() && loc.getSpeed() > 1.0f) {
                    float bearingAccuracy = loc.getBearingAccuracyDegrees();

                    // Yalnızca bearing doğruluğu düşükse NaN yap
                    if (!Float.isNaN(bearingAccuracy) && bearingAccuracy > 45f) {
                        gpsBearingFinal = Float.NaN;
                    } else {
                        gpsBearingFinal = loc.getBearing();
                    }

                } else {
                    gpsBearingFinal = Float.NaN;
                }

                final float sogKn = (loc.hasSpeed() && loc.getSpeed() > 1.0f) ? loc.getSpeed() * 1.94384f : 0f;

                final boolean isCogValid = !Float.isNaN(gpsBearingFinal);

// 🔁 Magnetic ok COG'a göre dönecekse düzelt
                final float displayMagneticHeading;
                if (isCogValid) {
                    displayMagneticHeading = (trueHeading - gpsBearingFinal + 360f) % 360f;
                } else {
                    displayMagneticHeading = trueHeading; // daha mantıklı olur
                }
// LocationCallback içinde
                float decl = geoField.getDeclination();
                gyroView.setDeclination(decl);

                float gpsBearing = isCogValid ? gpsBearingFinal : Float.NaN;
                gyroView.setBearing(gpsBearing);        // artık NaN’a da dayanıklı
// 🔄 COG filtrelemesi (daha yumuşak geçiş için)
                final float displayCog;
                if (isCogValid) {
                    if (!cogInitialized) {
                        filteredCog = gpsBearingFinal;
                        cogInitialized = true;
                    } else {
                        float delta = ((gpsBearingFinal - filteredCog + 540f) % 360f) - 180f;
                        filteredCog = (filteredCog + cogAlpha * delta + 360f) % 360f;
                    }
                    displayCog = filteredCog;
                } else {
                    displayCog = Float.NaN;
                }

// 🧭 UI Güncellemesi
                runOnUiThread(() -> {
                    txtLatLon.setText(NavigationActivity.toDMM(lat, "N", "S") + "  " +
                            NavigationActivity.toDMM(lon, "E", "W"));
                    gyroView.setDeclination(declination);

                    if (!Float.isNaN(displayCog)) {
                        txtCog.setText(String.format("COG : %05.1f°", displayCog));
                        gyroView.setBearing(displayCog); // pusula gülünü filtreli değere göre döndür
                    } else {
                        txtCog.setText("COG : ---°");
                        gyroView.setBearing(Float.NaN); // sabit bırak
                    }

                    txtSog.setText(String.format("Speed : %.1f knt", sogKn));

                    // ⬇️ Manyetik ok (sensor yönü) - gül sabit değilse buna göre çizilir
                    gyroView.setMagneticHeading(displayMagneticHeading);
                });

            }
        };

        fused.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, magnetometer,  SensorManager.SENSOR_DELAY_UI);
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        gnssCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                List<Float> list = new ArrayList<>();
                for (int i = 0; i < status.getSatelliteCount(); i++) {
                    if (status.usedInFix(i)) {
                        list.add(status.getCn0DbHz(i));
                    }
                }
                runOnUiThread(() -> satBarView.updateBars(list));
            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(gnssCallback);
        }
        startLiveUpdates();// EKLE
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
        fused.removeLocationUpdates(locationCallback); // BU SATIR ÖNEMLİ
        if (locationManager != null && gnssCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssCallback);
        }

    }
    private final SensorEventListener sensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor s, int acc) {}

        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.05f;   // filtre sertliği

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mGravity = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mGeomagnetic = event.values.clone();
                    break;
            }

            if (mGravity == null || mGeomagnetic == null) return;

            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float raw = (float) Math.toDegrees(orientation[0]);
                raw = (raw + 360f) % 360f;

                // DAİRESEL FARK (360 geçişini düzeltir)
                float delta = ((raw - filteredMag + 540f) % 360f) - 180f;
                filteredMag = (filteredMag + alpha * delta + 360f) % 360f;

                magneticHeading = filteredMag;

                runOnUiThread(() -> gyroView.setMagneticHeading(magneticHeading));
            }
        }
    };

}