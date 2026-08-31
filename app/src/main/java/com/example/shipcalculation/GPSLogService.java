package com.example.shipcalculation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Foreground service that keeps location updates running and writes last known location
 * to gps_log.csv every `intervalMinutes`.
 *
 * Minimal, safe and compatible with your existing file layout (getExternalFilesDir(null)).
 */
public class GPSLogService extends Service {

    public static final String CHANNEL_ID = "gps_log_channel";
    public static final int NOTIF_ID = 9981;
    public static final String EXTRA_INTERVAL_MIN = "interval_minutes";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation = null;

    private Handler handler;
    private Runnable writeRunnable;
    private int intervalMinutes = 30; // default

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    private void startLocationUpdates() {
        try {
            LocationRequest req = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setInterval(2000)   // keep updates frequent so lastLocation stays fresh
                    .setFastestInterval(1000);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;
                    Location loc = locationResult.getLastLocation();
                    if (loc != null) {
                        lastLocation = loc;
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            // permission might be missing; just stop service as it's not able to continue
            stopSelf();
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    private void schedulePeriodicWrites() {
        // Cancel previous if any
        if (writeRunnable != null) handler.removeCallbacks(writeRunnable);

        writeRunnable = new Runnable() {
            @Override
            public void run() {
                writeCurrentLocationToCsv();
                // schedule next
                handler.postDelayed(this, intervalMinutes * 60 * 1000L);
            }
        };
        handler.post(writeRunnable);
    }

    private void stopPeriodicWrites() {
        if (writeRunnable != null) handler.removeCallbacks(writeRunnable);
    }

    private void writeCurrentLocationToCsv() {
        // Compose CSV line similar to existing saveLogEntry() format:
        // timestamp;lat;lon;cog;sog\n
        try {
            if (lastLocation == null) return;

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            String lat = NavigationActivity.toDMM(lastLocation.getLatitude(), "N", "S");
            String lon = NavigationActivity.toDMM(lastLocation.getLongitude(), "E", "W");

            // compute cog & sog similar to NavigationActivity display (best effort)
            String cogStr = lastLocation.hasBearing() ? String.format(Locale.getDefault(), "%06.2f°", lastLocation.getBearing()) : "---°";
            double sogKn = (lastLocation.hasSpeed() && lastLocation.getSpeed() > 0.1f) ? lastLocation.getSpeed() * 1.94384 : 0.0;
            String sogStr = String.format(Locale.getDefault(), "%.2f knt", sogKn);

            String csvLine = timestamp + ";" + lat + ";" + lon + ";" + cogStr.replace("°", " deg") + ";" + sogStr + "\n";

            File logFile = new File(getExternalFilesDir(null), "gps_log.csv");
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.append(csvLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        String title = "GPS Log Service";
        String text = "Recording GPS logs every " + intervalMinutes + " minute(s)";
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_signal_strong) // replace with an existing small icon
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        return b.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GPS Log";
            String desc = "Foreground service for GPS logging";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(desc);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Read interval
        if (intent != null && intent.hasExtra(EXTRA_INTERVAL_MIN)) {
            intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MIN, intervalMinutes);
            if (intervalMinutes < 1) intervalMinutes = 1;
        }

        // Start foreground notification
        startForeground(NOTIF_ID, buildNotification());

        startLocationUpdates();
        schedulePeriodicWrites();

        // If the system kills the service, don't recreate automatically unless explicit intent (START_NOT_STICKY)
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopPeriodicWrites();
        stopLocationUpdates();
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // not a bound service
        return null;
    }
}
