package org.jenkinsci.plugins.extremefeedback.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class UdpMessageSender {

    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    public static void send(String ipAddress, int port, byte[] data) {
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), port);
            socket.send(packet);
        } catch (UnknownHostException e) {
            LOGGER.severe(e.getMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}