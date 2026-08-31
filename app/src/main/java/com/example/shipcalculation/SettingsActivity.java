package com.example.shipcalculation;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity {
    FrameLayout frameLayout;
    EditText editTextForeDist, editTextMidDist, editTextAftDist, editTextLbm, editTextLbp,
            editTextShipName, editTextCallSign, editTextImo, editTextLightShip,
            editTextGross, editTextNetTon, editTextFlag,editTextDw,
            editTextHoldNo1Capacity, editTextHoldNo2Capacity, editTextHoldNo3Capacity, editTextHoldNo4Capacity, editTextHoldNo5Capacity, editTextHoldNo6Capacity;

FloatingActionButton btnGoIn;
    SharedPreferences pref;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


       // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        frameLayout = findViewById(R.id.frameLayout);
        pref = getSharedPreferences("MyData", MODE_PRIVATE);
        // Initialize the views
        initializeViews();

        // Set the initial values from the SharedPreferences
        setInitialValues();

        // Parse the values from the Intent
        btnGoIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (editTextGross.getText().toString().isEmpty() ||
                        editTextNetTon.getText().toString().isEmpty() ||
                        editTextLbp.getText().toString().isEmpty() ||
                        editTextLbm.getText().toString().isEmpty() ||
                        editTextForeDist.getText().toString().isEmpty() ||
                        editTextMidDist.getText().toString().isEmpty() ||
                        editTextShipName.getText().toString().isEmpty() ||
                        editTextDw.getText().toString().isEmpty() ||
                        editTextFlag.getText().toString().isEmpty() ||
                        editTextCallSign.getText().toString().isEmpty() ||
                        editTextImo.getText().toString().isEmpty() ||
                        editTextLightShip.getText().toString().isEmpty() ||
                        editTextHoldNo1Capacity.getText().toString().isEmpty() ||
                        editTextHoldNo2Capacity.getText().toString().isEmpty() ||
                        //editTextHoldNo3Capacity.getText().toString().isEmpty() ||
                        //editTextHoldNo4Capacity.getText().toString().isEmpty() ||
                       // editTextHoldNo5Capacity.getText().toString().isEmpty() ||
                        //editTextHoldNo6Capacity.getText().toString().isEmpty() ||

                        editTextAftDist.getText().toString().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please Fill All Data!", Toast.LENGTH_LONG).show();
                } else {
                    saveValuesToPreferences();
                    // SettingsActivity.java içindeki onClick metodundan bir parça
// ...
// 1 – 6 arası tüm alanları tek döngüde yaz
                    for (int i = 1; i <= 6; i++) {
                        String key = "holdNo" + i + "Capacity";
                        int editId = getResources().getIdentifier("editTextHoldNo" + i + "Capacity", "id", getPackageName());
                        EditText et = findViewById(editId);

                        String val = et.getText().toString().trim();
                        pref.edit().putString(key, val.isEmpty() ? "0.0" : val).apply(); // İşte burada!
                    }
// ...

                    Toast.makeText(SettingsActivity.this, "Data Saved Successfully", Toast.LENGTH_LONG).show();
                  //  Intent intent = new Intent(SettingsActivity.this, DraftActivity.class);
                  //  startActivity(intent);
                }

            }

            private void saveValuesToPreferences() {
                pref.edit().putString("gross", editTextGross.getText().toString()).apply();
                pref.edit().putString("netTon", editTextNetTon.getText().toString()).apply();
                pref.edit().putString("lbp", editTextLbp.getText().toString()).apply();
                pref.edit().putString("lbm", editTextLbm.getText().toString()).apply();
                pref.edit().putString("foreDist", editTextForeDist.getText().toString()).apply();
                pref.edit().putString("midDist", editTextMidDist.getText().toString()).apply();
                pref.edit().putString("aftDist", editTextAftDist.getText().toString()).apply();
                pref.edit().putString("shipName", editTextShipName.getText().toString()).apply();
                pref.edit().putString("dwtCapacity", editTextDw.getText().toString()).apply();
                pref.edit().putString("flag", editTextFlag.getText().toString()).apply();
                pref.edit().putString("callSign", editTextCallSign.getText().toString()).apply();
                pref.edit().putString("imo", editTextImo.getText().toString()).apply();
                pref.edit().putString("lightShip", editTextLightShip.getText().toString()).apply();
                pref.edit().putString("holdNo1Capacity", editTextHoldNo1Capacity.getText().toString()).apply();
                pref.edit().putString("holdNo2Capacity", editTextHoldNo2Capacity.getText().toString()).apply();
                pref.edit().putString("holdNo3Capacity", editTextHoldNo3Capacity.getText().toString()).apply();
                pref.edit().putString("holdNo4Capacity", editTextHoldNo4Capacity.getText().toString()).apply();
                pref.edit().putString("holdNo5Capacity", editTextHoldNo5Capacity.getText().toString()).apply();
                pref.edit().putString("holdNo6Capacity", editTextHoldNo6Capacity.getText().toString()).apply();
            }

        });

    }

    private void initializeViews() {
        editTextGross = findViewById(R.id.editTextGross);
        editTextNetTon = findViewById(R.id.editTextNetTon);
        editTextLbp = findViewById(R.id.editTextLbp);
        editTextLbm = findViewById(R.id.editTextLbm);
        editTextForeDist = findViewById(R.id.editTextForeDist);
        editTextMidDist = findViewById(R.id.editTextMidDist);
        editTextAftDist = findViewById(R.id.editTextAftDist);
        editTextShipName = findViewById(R.id.editTextShipName);
        editTextDw = findViewById(R.id.editTextDw);
        editTextFlag = findViewById(R.id.editTextFlag);
        editTextCallSign = findViewById(R.id.editTextCallSign);
        editTextImo = findViewById(R.id.editTextImo);
        editTextLightShip = findViewById(R.id.editTextLightShip);
        editTextHoldNo1Capacity = findViewById(R.id.editTextHoldNo1Capacity);
        editTextHoldNo2Capacity = findViewById(R.id.editTextHoldNo2Capacity);
        editTextHoldNo3Capacity = findViewById(R.id.editTextHoldNo3Capacity);
        editTextHoldNo4Capacity = findViewById(R.id.editTextHoldNo4Capacity);
        editTextHoldNo5Capacity = findViewById(R.id.editTextHoldNo5Capacity);
        editTextHoldNo6Capacity = findViewById(R.id.editTextHoldNo6Capacity);
        btnGoIn = findViewById(R.id.btnGoIn);

    }

    private void setInitialValues() {
        //Pref ten deger yukleme
        editTextGross.setText(pref.getString("gross", ""));
        editTextNetTon.setText(pref.getString("netTon", ""));
        editTextLbp.setText(pref.getString("lbp", ""));
        editTextLbm.setText(pref.getString("lbm", ""));
        editTextForeDist.setText(pref.getString("foreDist", ""));
        editTextMidDist.setText(pref.getString("midDist", ""));
        editTextAftDist.setText(pref.getString("aftDist", ""));
        editTextShipName.setText(pref.getString("shipName", ""));
        editTextDw.setText(pref.getString("dwtCapacity", ""));
        editTextFlag.setText(pref.getString("flag", ""));
        editTextCallSign.setText(pref.getString("callSign", ""));
        editTextImo.setText(pref.getString("imo", ""));
        editTextLightShip.setText(pref.getString("lightShip", ""));
        editTextHoldNo1Capacity.setText(pref.getString("holdNo1Capacity", ""));
        editTextHoldNo2Capacity.setText(pref.getString("holdNo2Capacity", ""));
        editTextHoldNo3Capacity.setText(pref.getString("holdNo3Capacity", ""));
        editTextHoldNo4Capacity.setText(pref.getString("holdNo4Capacity", ""));
        editTextHoldNo5Capacity.setText(pref.getString("holdNo5Capacity", ""));
        editTextHoldNo6Capacity.setText(pref.getString("holdNo6Capacity", ""));

    }


}
