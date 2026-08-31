package com.example.shipcalculation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SatelliteBarView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Float> cn0List = new ArrayList<>();

    public SatelliteBarView(Context c, AttributeSet attrs) {
        super(c, attrs);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void updateBars(List<Float> cn0s) {
        cn0List.clear();
        cn0List.addAll(cn0s);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cn0List.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        int barW = w / cn0List.size();
        int maxCn0 = 50;   // max expected C/N0

        for (int i = 0; i < cn0List.size(); i++) {
            float cn0 = cn0List.get(i);
            float top = h * (1 - Math.min(1, cn0 / maxCn0));

            // color by level
            if (cn0 < 10)      barPaint.setColor(Color.RED);
            else if (cn0 < 20) barPaint.setColor(Color.parseColor("#FF9800"));
            else if (cn0 < 30) barPaint.setColor(Color.YELLOW);
            else               barPaint.setColor(Color.GREEN);

            float left = i * barW + 4;
            float right = (i + 1) * barW - 4;
            canvas.drawRect(left, top, right, h, barPaint);

            // PRN label
            canvas.drawText(String.valueOf(i + 1), left + barW / 2f, h - 5, textPaint);
        }
    }

}