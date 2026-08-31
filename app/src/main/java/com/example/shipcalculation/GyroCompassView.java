package com.example.shipcalculation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class GyroCompassView extends View {


    private float filteredMag = 0f;
    private final Paint
            circlePaint  = new Paint(Paint.ANTI_ALIAS_FLAG),
            tickPaint    = new Paint(Paint.ANTI_ALIAS_FLAG),
            textPaint    = new Paint(Paint.ANTI_ALIAS_FLAG),
            lubberPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF circleRect = new RectF();
    private float bearing = 0f;   // 0-360
    private float declinationDeg = 0f;
    private Paint declTextPaint;
    private Paint trueTextPaint;
    private Paint magTextPaint;


    public GyroCompassView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    private void init() {
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStrokeWidth(4f);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeWidth(2f);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        lubberPaint.setColor(Color.RED);
        lubberPaint.setStyle(Paint.Style.FILL);
        declTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        declTextPaint.setColor(Color.parseColor("#40E0D0")); // istediğin renk
        declTextPaint.setTextAlign(Paint.Align.CENTER);
        declTextPaint.setTextSize(40f); // başlangıç boyut (piksel cinsinden)

        magTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magTextPaint.setColor(Color.parseColor("#40E0D0")); // istediğin renk
        magTextPaint.setTextAlign(Paint.Align.CENTER);
        magTextPaint.setTextSize(60f); // başlangıç boyut (piksel cinsinden)

        trueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trueTextPaint.setColor(Color.parseColor("#40E0D0"));
        trueTextPaint.setTextSize(60f);
        trueTextPaint.setTextAlign(Paint.Align.RIGHT);
    }
    public void setDeclination(float deg) {
        declinationDeg = deg;
        invalidate();
    }

    public void setBearing(float bearing) {
        if (Float.isNaN(bearing)) {
            // NaN geldiğinde ekranı bozmamak için eski değeri koru
            // veya 0° yap
            this.bearing = 0f;
        } else {
            this.bearing = (bearing + 360f) % 360f;
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w   = getWidth();
        int h   = getHeight();
        int dia = Math.min(w, h);
        float r = dia / 2f - 20;
        float cx = w / 2f;
        float cy = h / 2f;

        // Outer ring
        circleRect.set(cx - r, cy - r, cx + r, cy + r);
        canvas.drawOval(circleRect, circlePaint);

        // Ticks & numbers
        // ----------------------------------------
// 1) Ticks & big-label numbers (0-20-40...)
        textPaint.setTextSize(r * 0.11f);
        for (int deg = 0; deg < 360; deg += 2) {
            float angle = deg - bearing;
            float rad = (float) Math.toRadians(angle);

            // Çizgi uzunluğu
            // 1) Boy seçimi
            boolean bigTick   = deg % 20 == 0;      // 0,20,40...
            boolean midTick   = deg % 10 == 0;      // 10,30,50... (rakam yok)
            float startR = bigTick ? r * 0.82f      // uzun + rakam
                    : midTick ? r * 0.87f      // orta uzunluk, rakam yok
                    : r * 0.91f;               // en kısa 5-derece çizgisi
            float endR   = r;

            float x1 = (float) (cx + startR * Math.sin(rad));
            float y1 = (float) (cy - startR * Math.cos(rad));
            float x2 = (float) (cx + endR   * Math.sin(rad));
            float y2 = (float) (cy - endR   * Math.cos(rad));
            canvas.drawLine(x1, y1, x2, y2, tickPaint);

            // Sadece 0-20-40-60… yaz
            if (bigTick) {
                float tx = (float) (cx + (r * 0.75f) * Math.sin(rad));
                float ty = (float) (cy - (r * 0.75f) * Math.cos(rad)) + (r * 0.04f);
                canvas.drawText(String.valueOf(deg), tx, ty, textPaint);
            }
        }
        canvas.save();
        canvas.rotate(-bearing, cx, cy);

// ----------------------------------------
// === PUSULA YILDIZI ===
// === 8 YÖNLÜ KOMPAS GÜLÜ ===
        Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.LTGRAY);
        starPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);

            // Ana yönlerde uzun kol, ara yönlerde kısa kol
            float len = (i % 2 == 0) ? r * 0.50f : r * 0.40f;
            float base = (i % 2 == 0) ? r * 0.10f : r * 0.10f;

            float xOuter = (float) (cx + Math.sin(angle) * len);
            float yOuter = (float) (cy - Math.cos(angle) * len);

            float xLeft = (float) (cx + Math.sin(angle - Math.PI / 6) * base);
            float yLeft = (float) (cy - Math.cos(angle - Math.PI / 6) * base);

            float xRight = (float) (cx + Math.sin(angle + Math.PI / 6) * base);
            float yRight = (float) (cy - Math.cos(angle + Math.PI / 6) * base);

            Path arm = new Path();
            arm.moveTo(xOuter, yOuter);
            arm.lineTo(xLeft, yLeft);
            arm.lineTo(xRight, yRight);
            arm.close();
            if (i == 0) {
                starPaint.setColor(Color.RED); // N yönü belirgin olsun
            }else if (i == 4) {
                    starPaint.setColor(Color.BLUE); // S yönü belirgin olsun
            } else {
                starPaint.setColor(Color.LTGRAY);
            }

            canvas.drawPath(arm, starPaint);
        }
// === Ana yönler ===
        String[] dirs = {"N", "E", "S", "W"};
        for (int i = 0; i < dirs.length; i++) {
            int angleDeg = i * 90;  // 💡 AÇIKÇA TANIMLIYORUZ
            double angle = Math.toRadians(angleDeg);
            float labelRadius = r * 0.55f;

            float x = (float) (cx + Math.sin(angle) * labelRadius);
            float y = (float) (cy - Math.cos(angle) * labelRadius) + (r * 0.03f);

            textPaint.setTextSize(r * 0.12f);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            if (angleDeg == 0) {
                textPaint.setColor(Color.RED); // North
            } else if (angleDeg == 180) {
                textPaint.setColor(Color.BLUE); // South
            } else {
                textPaint.setColor(Color.LTGRAY);
            }

            canvas.drawText(dirs[i], x, y, textPaint);
        }

// === Ara yönler ===
        String[] dirsSmall = {"NE", "SE", "SW", "NW"};
        for (int i = 0; i < dirsSmall.length; i++) {
            double angle = Math.toRadians(45 + i * 90); // 45, 135, 225, 315
            float labelRadius = r * 0.45f; // daha içte

            float x = (float) (cx + Math.sin(angle) * labelRadius);
            float y = (float) (cy - Math.cos(angle) * labelRadius) + (r * 0.02f);

            textPaint.setTextSize(r * 0.08f); // daha küçük
            textPaint.setTypeface(Typeface.DEFAULT); // normal yazı tipi
            canvas.drawText(dirsSmall[i], x, y, textPaint);
        }
        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.DKGRAY);
        canvas.drawCircle(cx, cy, r * 0.1f, centerPaint);

        canvas.restore();


        // === MAGNETIC RING (kalın) ===
        Paint magRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magRingPaint.setStyle(Paint.Style.STROKE);
        // magRingPaint.setColor(Color.RED);   // <-- eski satır
        magRingPaint.setColor(Color.parseColor("#40E0D0")); // Turquoise
        magRingPaint.setStrokeWidth(25f);          // kalın

        float magRingR = r * 1.0f;
        RectF magRingRect = new RectF(cx - magRingR, cy - magRingR,
                cx + magRingR, cy + magRingR);
        canvas.drawOval(magRingRect, magRingPaint);

// === MAGNETIC CURSOR (turkuaz, belirgin) ===
        Paint magCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magCursorPaint.setColor(Color.parseColor("#40E0D0"));
        magCursorPaint.setStyle(Paint.Style.FILL);

        float delta = (magneticHeading - bearing + 360f) % 360f;
        canvas.save();
        canvas.rotate(delta, cx, cy);


//  uzunluğunda turkuaz ok
        float cursorLen = r * 0.95f;
        Path cursor = new Path();
        cursor.moveTo(cx, cy - magRingR);
        cursor.lineTo(cx - 25, cy - magRingR + cursorLen);
        cursor.lineTo(cx + 25, cy - magRingR + cursorLen);
        cursor.close();
        canvas.drawPath(cursor, magCursorPaint);

            canvas.restore(); // güvenli restore

// === Declination Yazısı ===
        //textPaint.setTextSize(r * 0.08f);
        declTextPaint.setTextAlign(Paint.Align.LEFT);
        String dir = declinationDeg >= 0 ? "E" : "W";
        float absDecl = Math.abs(declinationDeg);
        String declStr = String.format("Var.: %.1f°%s", absDecl, dir);

// cx + r = sağ; cy + r = alt
        canvas.drawText(declStr, cx + r * -1.055f, cy + r * 1.02f, declTextPaint);

        declTextPaint.setTextSize(r * 0.12f); // örneğin pusula yarıçapının %7'si kadar
/// mag------
        magTextPaint.setTextAlign(Paint.Align.LEFT);
        String magStr;
        magStr = String.format("Mag: %.0f°", magneticHeading, dir);
        canvas.drawText(magStr, cx + r * 0.45f, cy + r * 1.02f, magTextPaint);

        //----true-----------
        trueTextPaint.setTextAlign(Paint.Align.RIGHT);
        trueTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float trueHeading = (magneticHeading - declinationDeg + 360f) % 360f;
        String trueStr = String.format("True: %.0f°", trueHeading);

// Sağ üstte, biraz yukarı
        canvas.drawText(trueStr, cx + r * 0.98f, cy - r * 0.95f, trueTextPaint);

        // Inner TRUE ring
        textPaint.setTextSize(r * 0.08f);
        canvas.drawText("TRUE", cx, cy - r * 0.45f, textPaint);

        // Lubber line (fixed red triangle on top)
        Path lubber = new Path();
        float top = cy - r * 0.90f;
        lubber.moveTo(cx, top);
        lubber.lineTo(cx - 25, top + 400);
        lubber.lineTo(cx + 25, top + 400);
        lubber.close();
        canvas.drawPath(lubber, lubberPaint);

        // MN / MS labels
        //textPaint.setTextSize(r * 0.07f);
        //canvas.drawText("MN", cx - r * 0.48f, cy - r * 0.20f, textPaint);
       // canvas.drawText("MS", cx + r * 0.48f, cy + r * 0.20f, textPaint);
    }

    private float magneticHeading = 0f;
    public void setMagneticHeading(float h) {
        magneticHeading = (h + 360f) % 360f;
        invalidate();
    }

}
