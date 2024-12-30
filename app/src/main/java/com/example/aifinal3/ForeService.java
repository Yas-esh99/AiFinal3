package com.example.aifinal3;

import android.app.Notification;
import android.app.Service;
import android.os.Handler;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForeService extends Service implements PublicInterface {
    
    private static Handler uiHand;
    private BlueClass blueClass;
    private Stt stt;
    private ExecutorService executorService;
    private ExecutorService executorService2;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        NotificationHelper.createNotificationChannel(this);
        Notification notify = NotificationHelper.createNotification(this);
        
        startForeground(1,notify);
        
        MainActivity.log("Foreground service started suck",this);
        
        executorService = Executors.newSingleThreadExecutor();
        executorService2 = Executors.newSingleThreadExecutor();
        
        stt = new Stt(this);
        stt.setListener(this);
        blueClass = BluetoothHelper.getBlueclass();
        blueClass.setListner(this);
        if(blueClass != null) {
        	blueClass.setContext(ForeService.this);
        } else {
            blueClass = new BlueClass(this);
        }
        
        executorService.submit(() -> {
            blueClass.connect();
        });
        
        //BluetoothHelper.setBlueClass(this);
    }
 /*   public static void setHandler(Handler h){
         h = uiHand;
    }
    private void upadeStat(String s){
        if(uiHand != null){
            uiHand.post(() -> {
                Intent broadIntent = new Intent("com.example.aifinal3.s");
                    broadIntent.putExtra("status",s);
                    sendBroadcast(broadIntent);
                    
            })
        }
    }
    */
    
    @Override
    public void onBlueData(byte[] data, int read) {
        executorService2.submit(() -> {
            stt.processPcmData(data,read);
        });
        
    }
    @Override
    public void onSttdata(String text) {
        MainActivity.log(text,this);
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        blueClass.disconnectEsp();
        stt.cleanup();
    }
    
}
