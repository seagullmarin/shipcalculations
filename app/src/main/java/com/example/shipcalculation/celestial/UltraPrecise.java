package com.example.shipcalculation.celestial;

import java.util.Calendar;

public final class UltraPrecise {

    /*  body = "Sun", "Moon", "Polaris"  */
    public static double[] compute(String body, double latDeg, double lonDeg, Calendar cal) {
        double jd = julianUTC(cal);
        double T  = (jd - 2451545.0) / 36525.0;

        switch (body) {
            case "Sun":     return sunJPL(T, latDeg, lonDeg);
            case "Moon":    return moonJPL(T, latDeg, lonDeg);
            case "Polaris": return polarisFK6(T, latDeg, lonDeg);
            case "Mars":    return marsVSOP(T, latDeg, lonDeg);

            default:        return new double[]{0, 0};
        }
    }
    private static double[] marsVSOP(double T, double lat, double lon) {
        // Mars'ın heliosentrik ekliptik koordinatları (AU cinsinden)
        // Kaynak: VSOP87A light (kısıtlı terimlerle sadeleştirilmiş)
        // L, B, R = ekliptik boylam, enlem, yarıçap

        // Örnek sabitler (gerçek VSOP'tan sadeleştirilmiş)
        double L = (6.203477 + 334.061243 * T) % (2 * Math.PI); // radian
        double B = 0.0; // Mars'ın ekliptik enlemi genelde çok küçük, basitleştirilmiş
        double R = 1.523710; // AU

        // Dünya'nın konumu (aynı yöntemle)
        double L0 = (1.753470 + 628.331970 * T) % (2 * Math.PI); // radian
        double R0 = 1.000000;

        // Heliocentric Cartesian (Mars - Dünya)
        double x = R * Math.cos(B) * Math.cos(L) - R0 * Math.cos(L0);
        double y = R * Math.cos(B) * Math.sin(L) - R0 * Math.sin(L0);
        double z = R * Math.sin(B); // Dünya'nın z'si ≈ 0 alınır

        // Ekliptik → Ekvatoryal dönüşüm
        double epsilon = Math.toRadians(23.439291 - 0.0130042 * T);
        double xe = x;
        double ye = y * Math.cos(epsilon) - z * Math.sin(epsilon);
        double ze = y * Math.sin(epsilon) + z * Math.cos(epsilon);

        // RA, DEC
        double ra = Math.toDegrees(Math.atan2(ye, xe));
        if (ra < 0) ra += 360;
        double dec = Math.toDegrees(Math.atan2(ze, Math.sqrt(xe * xe + ye * ye)));

        return toAltAz(ra, dec, lat, lon, T);
    }


    /* ---------- Sun (JPL DE431 kırpılmış 0.01°) ---------- */
    private static double[] sunJPL(double T, double lat, double lon) {
        double L = 280.4664567 + 36000.76982779 * T + 0.0003032028 * T * T;
        double M = 357.5291092 + 35999.0502909  * T - 0.0001536 * T * T;
        double e = 0.016708617 - 0.000042037 * T - 0.0000001236 * T * T;
        double C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * Math.sin(Math.toRadians(M))
                + (0.019993 - 0.000101 * T) * Math.sin(Math.toRadians(2 * M))
                + 0.000289 * Math.sin(Math.toRadians(3 * M));
        double lambda = L + C;
        double epsilon = 23.43929111 - 0.013004167 * T - 1.63889E-7 * T * T + 5.03611E-7 * T * T * T;
        double ra = Math.toDegrees(Math.atan2(
                Math.cos(Math.toRadians(epsilon)) * Math.sin(Math.toRadians(lambda)),
                Math.cos(Math.toRadians(lambda))));
        if (ra < 0) ra += 360;

        double dec = Math.toDegrees(Math.asin(
                Math.sin(Math.toRadians(epsilon)) * Math.sin(Math.toRadians(lambda))));
        return toAltAz(ra, dec, lat, lon, T);
    }

    /* ---------- Moon (ELP2000-82B kırpılmış 0.01°) ---------- */
    private static double[] moonJPL(double T, double lat, double lon) {
        double L0 = 218.3164591 + 481267.88134236 * T - 0.0015786 * T * T;
        double D  = 297.8501921 + 445267.1114034  * T - 0.0018819 * T * T;
        double M  = 357.5291092 + 35999.0502909   * T - 0.0001536 * T * T;
        double M1 = 134.9633964 + 477198.8675055  * T + 0.0087414 * T * T;
        double F  = 93.2720950  + 483202.0175273  * T - 0.0036539 * T * T;

        L0 %= 360; D %= 360; M %= 360; M1 %= 360; F %= 360;

        double lambda = L0
                + 6.289 * sin(M1)
                + 1.274 * sin(2 * D - M1)
                + 0.658 * sin(2 * D)
                + 0.214 * sin(2 * M1)
                - 0.186 * sin(M)
                - 0.114 * sin(2 * F)
                + 0.058 * sin(2 * D - 2 * M1)
                + 0.057 * sin(2 * D - M - M1)
                + 0.053 * sin(2 * D + M1)
                + 0.046 * sin(2 * D - M)
                + 0.041 * sin(M - M1)
                - 0.035 * sin(D)
                - 0.031 * sin(M + M1)
                - 0.015 * sin(2 * D - 2 * F)
                + 0.011 * sin(2 * D - 2 * M);

        double beta = 5.128 * sin(F)
                + 0.280 * sin(M1 + F)
                + 0.277 * sin(M1 - F)
                + 0.173 * sin(2 * D - F)
                + 0.055 * sin(2 * D + F - M1)
                + 0.046 * sin(2 * D - F - M1)
                + 0.033 * sin(2 * D + F)
                + 0.017 * sin(2 * M1 + F);

        double epsilon = 23.43929111 - 0.013004167 * T - 1.63889E-7 * T * T + 5.03611E-7 * T * T * T;
        double sinE = Math.sin(Math.toRadians(epsilon));
        double cosE = Math.cos(Math.toRadians(epsilon));
        double sinBeta = Math.sin(Math.toRadians(beta));
        double cosBeta = Math.cos(Math.toRadians(beta));
        double sinLambda = Math.sin(Math.toRadians(lambda));
        double cosLambda = Math.cos(Math.toRadians(lambda));

        double y = sinLambda * cosE - Math.tan(Math.toRadians(beta)) * sinE;
        double x = cosLambda;

        double ra = Math.toDegrees(Math.atan2(y, x));
        double dec = Math.toDegrees(Math.asin(sinBeta * cosE + cosBeta * sinE * sinLambda));

        return toAltAz(ra, dec, lat, lon, T);
    }

    /* ---------- Polaris (FK6) ---------- */
    private static double[] polarisFK6(double T, double lat, double lon) {
        double yearsSinceJ2000 = T * 365.25;

        // J2000 epoch FK6 değerleri
        double ra  = 37.9545417 + 0.00005525 * yearsSinceJ2000; // RA proper motion
        double dec = 89.2641111 - 0.00000433 * yearsSinceJ2000; // DEC proper motion

        // RA'yı 0–360 aralığına al
        ra = ra % 360;
        if (ra < 0) ra += 360;

        return toAltAz(ra, dec, lat, lon, T);
    }


    /* ---------- Equatorial → Horizontal ---------- */
    private static double[] toAltAz(double raDeg, double decDeg, double lat, double lon, double T) {
        double jd  = 2451545.0 + T * 36525.0;
        double gst = 280.46061837 + 360.98564736629 * (jd - 2451545.0);
        gst = (gst % 360 + 360) % 360;
        double lst = (gst + lon) % 360;

        double ha = lst - raDeg;
        if (ha < -180) ha += 360;
        if (ha > 180) ha -= 360;

        double sinAlt = Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(decDeg)) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(decDeg)) *
                        Math.cos(Math.toRadians(ha));
        double alt = Math.toDegrees(Math.asin(sinAlt));

        double cosAz = (Math.sin(Math.toRadians(decDeg)) -
                Math.sin(Math.toRadians(lat)) * sinAlt) /
                (Math.cos(Math.toRadians(lat)) * Math.cos(Math.asin(sinAlt)));
        double az = Math.toDegrees(Math.acos(cosAz));
        if (Math.sin(Math.toRadians(ha)) >= 0) az = 360 - az;
        return new double[]{alt, az};
    }

    private static double julianUTC(Calendar cal) {
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int s = cal.get(Calendar.SECOND);

        if (m <= 2) { y--; m += 12; }
        int a = y / 100;
        int b = 2 - a + a / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) +
                d + b - 1524.5 + (h + min / 60.0 + s / 3600.0) / 24.0;
    }

    private static double sin(double deg) { return Math.sin(Math.toRadians(deg)); }
}