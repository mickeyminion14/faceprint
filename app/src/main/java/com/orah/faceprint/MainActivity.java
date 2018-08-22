package com.orah.faceprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static boolean LOGGED_IN = false;

    private Button btn_submit;
    private EditText username, password;
    public static SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        p=getSharedPreferences("Detail",MODE_PRIVATE);
//        Toast.makeText(this, p.getString("password",""), Toast.LENGTH_SHORT).show();

        if(p.getString("email","").equalsIgnoreCase("test")&& p.getString("password","").equalsIgnoreCase("1234")) {
            Intent i =new Intent(getApplicationContext(),BluetoothActivity.class);
            startActivity(i);
        }
        btn_submit = findViewById(R.id.btn_submit);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
                if(LOGGED_IN) {
                askForPermissions();
            }
            }
        });

    }

    protected void validateLogin() {
            String email=username.getText().toString().trim();
            String password1=password.getText().toString().trim();
        if (email.equalsIgnoreCase("test") && password1.equalsIgnoreCase("1234")) {

            Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, BluetoothActivity.class);
            LOGGED_IN = true;
            SharedPreferences.Editor ed=p.edit();
            ed.putString("email",email);
            ed.putString("password",password1);
            ed.commit();
            startActivity(i);
        } else {
            Toast.makeText(this, "Username or Password Incorrect !", Toast.LENGTH_SHORT).show();
        }

    }

    protected void askForPermissions() {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    Toast.makeText(this, "permission dialog as been initialized", Toast.LENGTH_SHORT).show();
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            200);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
            }
        }
    }
