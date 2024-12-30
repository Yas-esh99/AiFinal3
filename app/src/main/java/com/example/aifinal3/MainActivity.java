
package com.example.aifinal3;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.aifinal3.databinding.ActivityMainBinding;
import java.util.List;
import java.util.ArrayList;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private static final int PERMISSION_REQUEST_CODE = 1;
    
    private Intent serviceIntent;
    public BlueClass blueClass;
    private ExecutorService executorService;
   // private Handler uiHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        
        checkPer();
        
        executorService = Executors.newSingleThreadExecutor();
        blueClass = new BlueClass(MainActivity.this);
        BluetoothHelper.setBlueclass(blueClass);
        
      //  uiHandler = new Handler();
       // ForeService.setHandler(uiHandler);
        executorService.submit(() -> {
            binding.startbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                blueClass.start(enableBluetoothLauncher);
            }
            
        });
        
        binding.connbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                    
                    serviceIntent = new Intent(MainActivity.this,ForeService.class);
                    MainActivity.log("Foreground service starting",MainActivity.this);
                startService(serviceIntent);
                    
            }
            
        });
        binding.dissbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                stopService(serviceIntent);
                    MainActivity.log("Foreground service stoped suck",MainActivity.this);
            }
            
        });
        });
    }
    
    private void checkPer() {
        String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.INTERNET,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        };

        List<String> permissionsToRequest =
                new ArrayList<>(); // Create a list to hold permissions that need to be requested

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(
                        permission); // Add to the list if permission is not granted
            }
        }

        // If there are permissions to request, do so
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
    
    public static void log(String s,Context c){
        Log.d(c.getClass().getSimpleName(),s);
    }
    
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                BluetoothHelper.setBlueclass(blueClass);
                MainActivity.log("Bluetooth enabled",this);
            } else {
                MainActivity.log("Error enabling Bluetooth",this);
            }
        }
    );
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
