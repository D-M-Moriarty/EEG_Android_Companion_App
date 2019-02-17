package com.github.pwittchen.neurosky.app;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

class TelloController {

    private String[] mCmdArray;
    private UDP_Server udpServer;
    private UDP_Client udpClient;
    static boolean DRONE_SOCKET_ACTIVE = false;
    private boolean exceptionErrorInetAddress = false;
    static boolean EXCEPTION_ERROR_CLIENT = false;
    static boolean EXCEPTION_ERROR_SERVER = false;
    static InetAddress DRONE_ADDRESS;
    static final int LOCAL_PORT = 9000;
    static final int DRONE_BUFFER_SIZE = 1518;
    static final int DRONE_PORT = 8889;
    static DatagramSocket UPD_SOCKET = null;

    TelloController() {
         try {
            DRONE_ADDRESS = InetAddress.getByName("192.168.10.1");
        } catch (UnknownHostException e) {
             exceptionErrorInetAddress = true;
        }

        if(!exceptionErrorInetAddress) {
            udpServer = new UDP_Server();
            udpServer.runUdpServer();
            udpClient = new UDP_Client();
        }
        udpClient.Message = "command";
        udpClient.sendMessage();
    }

    String[] getmCmdArray() {
        return mCmdArray;
    }

    void setmCmdArray(String[] mCmdArray) {
        this.mCmdArray = mCmdArray;
    }

    void setUdpClientMessage(String message) {
        this.udpClient.Message = message;
    }

    void udpClientSendMessage() {
        this.udpClient.sendMessage();
    }

    void stopUdpServer() {
        this.udpServer.stop_UDP_Server();
    }

    boolean isExceptionErrorInetAddress() {
        return exceptionErrorInetAddress;
    }

}
