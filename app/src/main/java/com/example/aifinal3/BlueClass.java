package com.example.aifinal3;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import java.util.UUID;
import android.bluetooth.BluetoothDevice;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Intent;
import android.content.BroadcastReceiver;
import java.util.Set;
import android.content.IntentFilter;
import java.io.IOException;
import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioAttributes;
import android.media.AudioManager;
import java.io.InputStream;
import android.app.Activity;
import android.content.Context;

public class BlueClass {
    
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private final String espAdd = "A0:B7:65:0F:67:1E";
    private UUID ESP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Context context;
    private PublicInterface pi;
    private AudioAttributes audioAttributes;
    private AudioTrack audioTrack;
    
    public BlueClass(Context x){
        context = x;
    }
    
    public void start(ActivityResultLauncher<Intent> laucher){
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        if (checkBluetoothSupport()) {
                MainActivity.log("Bluetooth is supported",context);
                if (checkBluetoothEnable()) {
                    MainActivity.log("Bluetooth is already enabled",context);
                } else {
                    MainActivity.log("Enabling Bluetooth",context);
                    enableBluetooth(laucher);
                }
            } else {
                MainActivity.log("Bluetooth not supported",context);
            }
    }
    
    public void connect(){
        if (checkBluetoothEnable()) {
                BluetoothDevice device = getEspDevice();
                if (checkEspDevice(device)) {
                    MainActivity.log("Device found",context);
                    if (checkEspBonded()) {
                        MainActivity.log("ESP is bonded",context);
                        if (checkEspConnected(bluetoothSocket)) {
                            MainActivity.log("ESP is already connected",context);
                        } else {
                            connectEsp(device);
                        }
                    } else {
                        MainActivity.log("Bonding ESP...",context);
                        bondEsp(device);
                    }
                } else {
                    MainActivity.log("Device Not Found",context);
                }
            } else {
                MainActivity.log("Bluetooth is not enabled",context);
            }
    }
    public void setContext(Context c){
        context = c;
    }
    
    private boolean checkBluetoothSupport() {
        return bluetoothAdapter != null;
    }

    private boolean checkBluetoothEnable() {
        return bluetoothAdapter.isEnabled();
    }

    private void enableBluetooth(ActivityResultLauncher<Intent> laucher) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        laucher.launch(enableBtIntent);
    }

    private BluetoothDevice getEspDevice() {
        return bluetoothAdapter.getRemoteDevice(espAdd);
    }

    private boolean checkEspDevice(BluetoothDevice device) {
        return device != null;
    }

    private boolean checkEspBonded() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(espAdd)) {
                return true;
            }
        }
        return false;
    }
    
    private void bondEsp(BluetoothDevice device) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(bondReceiver, filter);
        device.createBond();
        MainActivity.log("Bonding...",context);
    }

    private boolean checkEspConnected(BluetoothSocket socket) {
        return socket != null && socket.isConnected();
    }

    private void connectEsp(BluetoothDevice device) {
        try {
            if (device == null) {
                MainActivity.log("Device is null",context);
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP_UUID);
            bluetoothSocket.connect();
            MainActivity.log("ESP connected",context);
            sendData(bluetoothSocket);
          //  startAudioStream(bluetoothSocket);
        } catch (IOException e) {
            MainActivity.log("Can't connect to ESP",context);
            try {
                if (bluetoothSocket != null) {
                    bluetoothSocket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void disconnectEsp() {
        if (audioTrack != null) {
    if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
        audioTrack.stop(); // Stop playback if running
    }
    audioTrack.release(); // Release resources
    audioTrack = null; // Nullify reference
}
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.close();
                MainActivity.log("Device disconnected",context);
            } catch (IOException e) {
                MainActivity.log("Failed to disconnect",context);
                
            }
        } else {
            MainActivity.log("Device not connected",context);
        }
        
    }
    
    private void sendData(BluetoothSocket bluetoothSocket){
        
        final int SAMPLE_RATE = 16000; // 16 kHz
         //   final int BUFFER_SIZE = 1024;
            final int BUFFER_SIZE = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
         
        try{
            byte[] packet = new byte[BUFFER_SIZE]; // Use the correct buffer size
            InputStream inputStream = bluetoothSocket.getInputStream();
          //  Log.d(TAG, "Starting to read audio data from Bluetooth");

            while (true) {
                int bytesRead = inputStream.read(packet); // Read a packet of data

                if (bytesRead > 0) { // Ensure data is being read
      //              Log.d(TAG, "Received " + bytesRead + " bytes");
                        pi.onBlueData(packet,bytesRead);
                    // Write packet data to AudioTrack for playback
                 //   int writtenBytes = audioTrack.write(packet, 0, bytesRead);
                    /*if (writtenBytes != bytesRead) {
             //           Log.w(TAG, "Mismatch in written bytes: expected " + bytesRead + ", wrote " + writtenBytes);
                    }*/
                } else {
      //              Log.w(TAG, "No data received or end of stream reached.");
                    Thread.sleep(10); // Sleep for a short time to prevent high CPU usage
                }
            }
        } catch (IOException | InterruptedException e) {
         //   Log.e(TAG, "IOException occurred during Bluetooth audio streaming", e);
            e.printStackTrace();
        }
    

    }

    private void startAudioStream(BluetoothSocket bluetoothSocket) {
    
        try {
            // Define audio settings
            final int SAMPLE_RATE = 16000; // 16 kHz
         //   final int BUFFER_SIZE = 1024;
            final int BUFFER_SIZE = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            // Create AudioAttributes
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA) // Usage as media playback
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Content as music
                    .build();
            // Initialize AudioTrack with AudioAttributes
            audioTrack = new AudioTrack(
                    audioAttributes,
                    new AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .build(),
                    BUFFER_SIZE,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );

            // Check if AudioTrack is initialized successfully
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
              //  Log.d(TAG, "AudioTrack initialized successfully");
                audioTrack.play();
            } else {
              //  Log.e(TAG, "AudioTrack initialization failed");
                return; // Exit if initialization fails
            }

            // Prepare to read from Bluetooth and play audio
            byte[] packet = new byte[BUFFER_SIZE]; // Use the correct buffer size
            InputStream inputStream = bluetoothSocket.getInputStream();
          //  Log.d(TAG, "Starting to read audio data from Bluetooth");

            while (true) {
                int bytesRead = inputStream.read(packet); // Read a packet of data

                if (bytesRead > 0) { // Ensure data is being read
      //              Log.d(TAG, "Received " + bytesRead + " bytes");
                        pi.onBlueData(packet,bytesRead);
                    // Write packet data to AudioTrack for playback
                    int writtenBytes = audioTrack.write(packet, 0, bytesRead);
                    /*if (writtenBytes != bytesRead) {
             //           Log.w(TAG, "Mismatch in written bytes: expected " + bytesRead + ", wrote " + writtenBytes);
                    }*/
                } else {
      //              Log.w(TAG, "No data received or end of stream reached.");
                    Thread.sleep(10); // Sleep for a short time to prevent high CPU usage
                }
            }
        } catch (IOException | InterruptedException e) {
         //   Log.e(TAG, "IOException occurred during Bluetooth audio streaming", e);
            e.printStackTrace();
        }
    
}
    public void setListner(PublicInterface p){
        pi = p;
    }

    private final BroadcastReceiver bondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED) {
                    MainActivity.log("Device bonded",context);
                } else if (state == BluetoothDevice.BOND_NONE) {
                    MainActivity.log("Error in bonding",context);
                }
            }
        }
    };

}
