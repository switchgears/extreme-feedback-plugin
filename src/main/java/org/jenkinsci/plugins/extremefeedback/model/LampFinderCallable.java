package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LampFinderCallable implements Callable<TreeSet<Lamp>> {

    private static final int PORT = 19418;
    private static final byte[] MESSAGE = "GITGEAR.COM".getBytes();
    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    public TreeSet<Lamp> call() throws Exception {
        TreeSet<Lamp> lamps = Sets.newTreeSet();
        // Send
        LOGGER.log(Level.INFO, "Broadcasting looking for lamps");

        // Setup the sender
        MulticastSocket mcsSender = new MulticastSocket(PORT);
        InetAddress inetAddress = InetAddress.getByName("239.77.124.213");
        mcsSender.joinGroup(inetAddress);
        mcsSender.setSoTimeout(2000);

        // Setup the receiver
        MulticastSocket mcsReceiver = new MulticastSocket(PORT+1);
        mcsReceiver.joinGroup(inetAddress);
        mcsReceiver.setSoTimeout(2000);
        byte[] buf = new byte[1000];
        DatagramPacket receiver = new DatagramPacket(buf, buf.length, inetAddress, PORT+1);

        // Send
        mcsSender.send(new DatagramPacket(MESSAGE, MESSAGE.length, inetAddress, PORT));

        // Receive
        boolean exit = false;
        while(!exit) {
            try {
                mcsReceiver.receive(receiver);
                String data = new String(receiver.getData());
                LOGGER.log(Level.INFO, data);
                if (data.startsWith("MAC=")) {
                    String macAddress = extractMacAddress(data);
                    String ipAddress = receiver.getAddress().getHostAddress();
                    LOGGER.log(Level.INFO, "MAC Address: " + macAddress + " IP  Address: " + ipAddress);
                    Lamp lamp = new Lamp(macAddress, ipAddress);
                    lamps.add(lamp);
                }
            } catch (SocketTimeoutException e) {
                exit = true;
                LOGGER.log(Level.INFO, "Time is out!");
            }
        }
        mcsSender.leaveGroup(inetAddress);
        mcsSender.close();
        mcsReceiver.leaveGroup(inetAddress);
        mcsReceiver.close();
        return lamps;
    }

    @VisibleForTesting
    public static String extractMacAddress(String data) {
        Iterable<String> splitted = Splitter.on("=").trimResults(
                CharMatcher.noneOf("0123456789abfcefABCDEF")
        ).split(data);
        return Iterables.get(splitted, 1);
    }


}
