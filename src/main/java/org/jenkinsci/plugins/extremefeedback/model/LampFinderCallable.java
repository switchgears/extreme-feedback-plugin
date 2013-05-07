package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LampFinderCallable implements Callable<Set<Lamp>> {

    Set<Lamp> lamps = Sets.newTreeSet();
    private static final int PORT = 19418;
    private static final byte[] MESSAGE = "EXTREMEFEEDBACK.COM".getBytes();
    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    public Set<Lamp> call() throws Exception {
        // Send
        MulticastSocket mcs = new MulticastSocket(PORT+1);
        InetAddress inetAddress = InetAddress.getByName("239.77.124.213");
        mcs.send(new DatagramPacket(MESSAGE, MESSAGE.length, inetAddress, PORT));
        // Receive
        mcs.joinGroup(inetAddress);
        mcs.setSoTimeout(5000);
        boolean exit = false;
        while(!exit) {
            byte[] buf = new byte[1000];
            DatagramPacket receiver = new DatagramPacket(buf, buf.length);
            try {
                mcs.receive(receiver);
                String data = new String(receiver.getData());
                LOGGER.log(Level.INFO, data);
                if(data.startsWith("MAC=")) {
                    String macAddress = Iterables.getLast(Splitter.on("=").trimResults().split(data));
                    macAddress = StringUtils.chomp(StringUtils.trim(macAddress));
                    String ipAddress = receiver.getAddress().getHostAddress();
                    LOGGER.log(Level.INFO, "MAC Address: " + macAddress + " IP  Address: " + ipAddress);
                    Lamp lamp = new Lamp(macAddress, ipAddress);
                    lamps.add(lamp);
                }
            } catch (SocketTimeoutException e) {
                exit = true;
            }
        }
        mcs.leaveGroup(inetAddress);
        return lamps;
    }


}
