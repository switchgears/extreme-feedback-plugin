package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

public class LampFinderCallable implements Callable<Set<Lamp>> {

    Set<Lamp> lamps = Sets.newTreeSet();
    private static final int PORT = 19418;
    private static final byte[] MESSAGE = "EXTREMEFEEDBACK.COM".getBytes();

    public Set<Lamp> call() throws Exception {
        // Send
        MulticastSocket mcs = new MulticastSocket(PORT+1);
        InetAddress inetAddress = InetAddress.getByName("255.255.255.255");
        mcs.send(new DatagramPacket(MESSAGE, MESSAGE.length, inetAddress, PORT));

        // Receive
        mcs.setSoTimeout(5000);
        boolean exit = false;
        while(!exit) {
            byte[] buf = new byte[1000];
            DatagramPacket receiver = new DatagramPacket(buf, buf.length);
            try {
                mcs.receive(receiver);
                String data = Arrays.toString(receiver.getData());
                if(data.startsWith("MAC=")) {
                    String macAddress = Iterables.getLast(Splitter.on("=").trimResults().split(data));
                    String ipAddress = receiver.getAddress().getHostAddress();
                    Lamp lamp = new Lamp(macAddress, ipAddress);
                    lamps.add(lamp);
                }
            } catch (SocketTimeoutException e) {
                exit = true;
            }
        }

        return lamps;
    }


}
