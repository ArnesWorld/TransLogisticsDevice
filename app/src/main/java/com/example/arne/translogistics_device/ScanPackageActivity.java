package com.example.arne.translogistics_device;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.arne.translogistics_device.DAL.AppDataBase;

public class ScanPackageActivity extends AppCompatActivity {

    private Button btnEnterPackageData;
    private AppDataBase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_package);
        //Initialize database
        database = AppDataBase.getInstance(getApplicationContext());

        btnEnterPackageData = findViewById(R.id.btnEnterPackageData);

        btnEnterPackageData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Layout dialog = (Layout) getResources().getLayout(R.layout.dialog_package_input);

                buildDialog(v);
            }
        });
    }

    private void buildDialog(View v) {
        PackageDialogFragment packDialog = new PackageDialogFragment();
        packDialog.show(getSupportFragmentManager(), "package");

    }
}
