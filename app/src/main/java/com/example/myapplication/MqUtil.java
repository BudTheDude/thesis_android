package com.example.myapplication;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import com.rabbitmq.client.*;

import java.io.IOException;

public class MqUtil {
    static Thread subscribeThread;
    private static String mMessage;
    private final static String EXCHANGE = "orderInfo33";

    public static void subscribe(final Handler handler, ConnectionFactory factory) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                while (true) {
                try {
                    //Use the previous settings to establish a connection
                    Connection connection = factory.newConnection();
                    //Create a channel
                    Channel channel = connection.createChannel();
                    //Only send one at a time, get one after processing one
                    channel.basicQos(1);
                    channel.exchangeDeclare(EXCHANGE,"direct",true);//Here we should change according to the actual

                    channel.queueBind("hello", EXCHANGE, "hello");//This should be changed according to the actual situation
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
                            mMessage = new String(body, "UTF-8");
                            System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + mMessage + "'");
                            // It is more efficient to get the msg object from the message pool
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", mMessage);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    };
                    channel.basicConsume("hello", true, consumer);
//

                } catch (Exception e1) {
                    Log.d("", "Connection broken: " + e1.getClass().getName());
                    try {
                        Thread.sleep(2000); //sleep and then try again
                    } catch (InterruptedException e) {
//                            break;
                    }
                }
                Log.i("1111111111111111111111", "run: ");
//                }
            }
        });
        subscribeThread.start();
    }

}