package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static ConnectionFactory factory;

    //Set the notification bar message style
    private void setNotification(String msg) {
        //String title = msg.split("\\;")[0];
        //String content = msg.split("\\;")[1];
        int i = 0;
        //Click on the notification bar to jump to the page
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //Create notification message management class
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //The high version requires channels, many people can't pop up the message notification, just because this is not added
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Only channels are needed on Android O
            NotificationChannel notificationChannel = new NotificationChannel("channelid1","channelname",NotificationManager.IMPORTANCE_HIGH);
            //If you use IMPORTANCE_NOENE here, you need to open the channel in the system settings for the notification to pop up normally
            manager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelid1")//Create notification message instance
                .setContentTitle("Intruder")
                .setContentText("Watch Out!!")
                .setWhen(System.currentTimeMillis())//The notification bar shows the time
                .setSmallIcon(R.mipmap.ic_launcher)//Small icon in the notification bar
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//The notification bar drop-down is an icon
                .setContentIntent(pendingIntent)//Click the notification bar to jump to the page
                .setPriority(NotificationCompat.PRIORITY_MAX)//Set the notification message priority
                .setAutoCancel(true)//Set the notification message to disappear automatically after clicking the notification bar message
                // .setSound(Uri.fromFile(new File("/system/MP3/music.mp3"))) // notification bar message prompt tone
                .setVibrate(new long[]{0, 1000, 1000, 1000}) // Notification bar message vibration
                .setLights(Color.GREEN, 1000, 2000) //Notice bar message flashes (lights for one second and then lights for two seconds)
                .setDefaults(NotificationCompat.DEFAULT_ALL); //The notification bar notification sound, vibration, flashing lights, etc. are set as default
        //Short text
        Notification notification = builder.build();
        //Constant.TYPE1 is the message identifier of the notification bar, each id is different
        manager.notify(i, notification);
    }
    
    final Handler incomingMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("msg");
            setNotification(message);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        factory = new ConnectionFactory();
        //The following parameters should be changed according to the actual
        String uri = "amqps://vdwwsmtz:8t1_gM675-qlXqIZ70C4lA5gQyZTTnpR@crow.rmq.cloudamqp.com/vdwwsmtz";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }

        //Open the consumer thread
        MqUtil.subscribe(incomingMessageHandler,factory);
    }

}