package org.jenkinsci.plugins.extremefeedback.model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public class LampConfirmCallable implements Callable<String> {
    private String ipAddress;

    public LampConfirmCallable(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String call() throws Exception {
        String sentence = "SG-PING";

        Socket clientSocket = new Socket();
        clientSocket.setSoTimeout(2000);
        clientSocket.connect(new InetSocketAddress(InetAddress.getByName(ipAddress), 19417), 2000);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToServer.writeBytes(sentence + '\n');
        String macAddress = inFromServer.readLine();
        clientSocket.close();
        return macAddress;
    }
}
