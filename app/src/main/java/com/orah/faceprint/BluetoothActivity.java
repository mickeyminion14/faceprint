package com.orah.faceprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import me.aflak.bluetooth.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class BluetoothActivity extends AppCompatActivity {
    private boolean found = false;
    private Button clickme;

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private Button btn_sendData;
    private BluetoothDevice foundDevice = null;
    private BluetoothConnector.BluetoothSocketWrapper wrapper = null;
    private TextView textView;
    private BluetoothAdapter mBluetoothAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("MENIME", "Started Discovery");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("MENIME", "Finished Discovery");
                if(found) {
                    BluetoothConnector bc = new BluetoothConnector(foundDevice, true, mBluetoothAdapter, null);
                    try {
                        wrapper = bc.connect();
                        Log.d("MENIME","CONNECTED TO TARGET DEVICE");
                        Toast.makeText(context, "connected to "+foundDevice.getName(), Toast.LENGTH_SHORT).show();
                        btn_sendData.setVisibility(View.VISIBLE);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }


                    found = false;

                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Toast.makeText(context, device.getName().toString(), Toast.LENGTH_SHORT).show();

                if(device.getName().toString().equalsIgnoreCase("faceprint")) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d("MENIME","INTENDED DEVICE FOUND");
                    found = true;
                    foundDevice = device;

//                    ConnectThread ct = new ConnectThread(device ,mBluetoothAdapter);
                }
                Log.d("MENIME", device.getName());


            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        try {
            wrapper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        textView = (TextView) findViewById(R.id.textview);
        btn_sendData=findViewById(R.id.btn_reconnect);
        btn_sendData.setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        textView = (TextView) findViewById(R.id.textview);
        clickme=findViewById(R.id.clickme);

        clickme.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FingerprintHandler helper = new FingerprintHandler(getApplicationContext());
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        });


        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device Not Supported", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
            }

            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);

            mBluetoothAdapter.startDiscovery();

        }
        btn_sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject o=new JSONObject();
                try {
                    o.accumulate("username","test");
                    wrapper.getOutputStream().write(o.toString().getBytes(Charset.forName("UTF-8")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
//            BiometricPrompt biometricPrompt = getSystemService(BIO)



            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
//                if(fingerprintManager.FIN)
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                textView.setText("Your device doesn't support fingerprint authentication");
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                textView.setText("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                textView.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                textView.setText("Please enable lockscreen security in your device's Settings");
            } else {
                try {

                    generateKey();
                } catch (BluetoothActivity.FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
//                    FingerprintHandler helper = new FingerprintHandler(this);
//                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void generateKey() throws BluetoothActivity.FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//            textView.setText("no allowed bro");
//        }
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new BluetoothActivity.FingerprintException(exc);
        }
    }

    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (@SuppressLint("NewApi") KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }
}
