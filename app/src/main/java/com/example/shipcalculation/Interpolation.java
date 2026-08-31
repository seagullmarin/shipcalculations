package com.example.shipcalculation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class Interpolation extends AppCompatActivity {
    FrameLayout frameLayout;

    EditText editTextX, editTextX1, editTextX2, editTextY1, editTextY2,
            editT1, editT, editT2, editS1, editS, editS2, editQ1, editQ2, editX1, editX2;
    TextView textViewResultYx,
            textQ, textQS, textQC, textXS, textQS2;
    FloatingActionButton buttonClear, buttonClearTrim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_interpolation);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        frameLayout = findViewById(R.id.frameLayout);
        editTextX = findViewById(R.id.editTextX);
        editTextX1 = findViewById(R.id.editTextX1);
        editTextX2 = findViewById(R.id.editTextX2);
        editTextY1 = findViewById(R.id.editTextY1);
        editTextY2 = findViewById(R.id.editTextY2);
        editT = findViewById(R.id.editT);
        editT1 = findViewById(R.id.editT1);
        editT2 = findViewById(R.id.editT2);
        editS = findViewById(R.id.editS);
        editS2 = findViewById(R.id.editS2);
        editS1 = findViewById(R.id.editS1);
        editQ1 = findViewById(R.id.editQ1);
        editQ2 = findViewById(R.id.editQ2);
        editX1 = findViewById(R.id.editX1);
        editX2 = findViewById(R.id.editX2);

        textViewResultYx = findViewById(R.id.textViewResultYx);
        textQ = findViewById(R.id.textQ);
        textQS = findViewById(R.id.textQS);
        textQC = findViewById(R.id.textQC);
        textXS = findViewById(R.id.textXS);
        textQS2 = findViewById(R.id.textQS2);

        buttonClear = findViewById(R.id.buttonClear);
        buttonClearTrim = findViewById(R.id.buttonClearTrim);

        editTextX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Boş bırak
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Boş bırak
            }
        });
        // Diğer edittext alanları için de aynı işlemi yap
        editTextX1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextX2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextY1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextY2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        updateResults();

    }
    // Enterpolasyon hesabı yapan fonksiyon
    private double interpolate(double x, double x1, double x2, double y1, double y2) {
        double result = y1 + ((x - x1) * (y2 - y1)) / (x2 - x1);
        return result;
    }

    // Sonuçları güncelle
    private void updateResults() {
        try {
            double x = Double.parseDouble(editTextX.getText().toString());
            double x1 = Double.parseDouble(editTextX1.getText().toString());
            double x2 = Double.parseDouble(editTextX2.getText().toString());
            double y1 = Double.parseDouble(editTextY1.getText().toString());
            double y2 = Double.parseDouble(editTextY2.getText().toString());


            double result = interpolate(x, x1, x2, y1, y2);


            textViewResultYx.setText(String.format(Locale.US, "%.3f", result));


            Log.d("TAG", "x değeri: " + x); // Değişken değerini logla
            // Diğer değişkenleri de aynı şekilde logla

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        editT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editT2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editT1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editS1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editS2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editQ1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editQ2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editX1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editX2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        // İlk sonuçları güncelle


        trim();
    }
    private void trim() {
        try {
            double t1 = Double.parseDouble(editT1.getText().toString());
            double t = Double.parseDouble(editT.getText().toString());
            double t2 = Double.parseDouble(editT2.getText().toString());

            double s = Double.parseDouble(editS.getText().toString());
            double s1 = Double.parseDouble(editS1.getText().toString());
            double s2 = Double.parseDouble(editS2.getText().toString());

            double q1 = Double.parseDouble(editQ1.getText().toString());
            double q2 = Double.parseDouble(editQ2.getText().toString());

            double xx1 = Double.parseDouble(editX1.getText().toString());
            double xx2 = Double.parseDouble(editX2.getText().toString());

            double resultQ = q1 + ((t - t1) * (xx1 - q1)) / (t2 - t1);
            double resultQs2 = q2 + ((t - t1) * (xx2 - q2)) / (t2 -t1);
            double resultQs = q1 + ((s - s1) * (q2 - q1)) / (s2 - s1);
            double resultXs = xx1 + ((s - s1) * (xx2 - xx1)) / (s2 -s1);
            double resultQc = resultQ + ((s - s1) * (resultQs2 - resultQ)) / (s2 - s1);
            textQ.setText(String.format(Locale.US, "%.3f", resultQ));
            textQS.setText(String.format(Locale.US, "%.3f", resultQs));
            textQC.setText(String.format(Locale.US, "%.3f", resultQc));
            textXS.setText(String.format(Locale.US, "%.3f", resultXs));
            textQS2.setText(String.format(Locale.US, "%.3f", resultQs2));

            Log.d("TAG", "resultQ: " + resultQ);
            Log.d("TAG", "resultQs2: " + resultQs2);
            Log.d("TAG", "resultQs: " + resultQs);
            Log.d("TAG", "resultXs: " + resultXs);
            Log.d("TAG", "resultQc: " + resultQc);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        buttonClear.setOnClickListener(v -> {
            editTextX.getText().clear();
            editTextX1.getText().clear();
            editTextX2.getText().clear();
            editTextY1.getText().clear();
            editTextY2.getText().clear();
            textViewResultYx.setText("");
        });
        buttonClearTrim.setOnClickListener(v -> {
            editT.getText().clear();
            editT1.getText().clear();
            editT2.getText().clear();
            editS.getText().clear();
            editS1.getText().clear();
            editS2.getText().clear();
            editQ1.getText().clear();
            editQ2.getText().clear();
            editX1.getText().clear();
            editX2.getText().clear();
            textQ.setText("");
            textQS.setText("");
            textQC.setText("");
            textXS.setText("");
            textQS2.setText("");

        });
    }
}



