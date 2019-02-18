package com.darren.fyp;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDP_Client {

    public String Message;

    public void sendMessage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if(TelloController.UPD_SOCKET == null) {
                        TelloController.UPD_SOCKET = new DatagramSocket(TelloController.LOCAL_PORT);
                    }
                    DatagramPacket dp;
                    dp = new DatagramPacket(Message.getBytes(),
                                            Message.length(),
                            TelloController.DRONE_ADDRESS,
                            TelloController.DRONE_PORT);

                    TelloController.UPD_SOCKET.send(dp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    TelloController.DRONE_SOCKET_ACTIVE = false;
                    TelloController.EXCEPTION_ERROR_CLIENT = true;
                    }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}