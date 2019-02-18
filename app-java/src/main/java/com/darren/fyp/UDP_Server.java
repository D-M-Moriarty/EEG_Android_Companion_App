package com.darren.fyp;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static com.darren.fyp.MainActivity.TEXT_RESPONSE;

public class UDP_Server {

    private boolean serverActive = true;
    private String droneResponse;

//    @SuppressLint("NewApi")
    public void runUdpServer()
    {
        new AsyncTask<Void, Void, Void>() {
           @Override
            protected Void doInBackground(Void... params) {
                byte[] lMsg = new byte[TelloController.DRONE_BUFFER_SIZE];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);

                try {
                    if(TelloController.UPD_SOCKET == null) {
                        TelloController.UPD_SOCKET = new DatagramSocket(TelloController.LOCAL_PORT);
                    }
                    while(serverActive) {
                        TelloController.UPD_SOCKET.receive(dp);
                        droneResponse = new String(lMsg, 0, dp.getLength());
                        publishProgress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TelloController.EXCEPTION_ERROR_SERVER = true;
                } finally {
                    if (TelloController.UPD_SOCKET != null) {
                        TelloController.UPD_SOCKET.close();
                    }
                }
                return null;
            }

            protected void onProgressUpdate(Void... progress) {
                TelloController.DRONE_SOCKET_ACTIVE = true;
                TEXT_RESPONSE = droneResponse.trim();
            }

            protected void onPostExecute(Void result) {
                TelloController.DRONE_SOCKET_ACTIVE = false;
                TEXT_RESPONSE = "Error. UDP server loop ended unexpectedly!";
                super.onPostExecute(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop_UDP_Server()
    {
        serverActive = false;
    }
}