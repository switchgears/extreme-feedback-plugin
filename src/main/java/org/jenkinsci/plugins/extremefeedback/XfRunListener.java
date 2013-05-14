package org.jenkinsci.plugins.extremefeedback;

import com.google.common.collect.Maps;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Extension
public class XfRunListener extends RunListener<AbstractBuild> {

    private enum Color { GREEN, YELLOW, RED }
    private enum Action { ON, OFF, BLINK }

    private final static Map<Result, Color> resultColorMap;
    static {
        Map<Result, Color> map = Maps.newHashMap();
        map.put(Result.ABORTED, Color.RED);
        map.put(Result.FAILURE, Color.RED);
        map.put(Result.NOT_BUILT, Color.RED);
        map.put(Result.UNSTABLE, Color.YELLOW);
        map.put(Result.SUCCESS, Color.GREEN);
        resultColorMap = Collections.unmodifiableMap(map);
    }

    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    @Override
    public void onCompleted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Jenkins.getInstance().getPlugin(Lamps.class);
        Set<String> jobs = plugin.getJobs();

        if (jobs.contains(run.getParent().getName())) {
            Result result = run.getResult();
            Set<String> ipAddresses = plugin.getIpsContainingJob(run.getParent().getName());
            for (String ipAddress : ipAddresses) {
                sendNotification(ipAddress, resultColorMap.get(result), Action.ON);
            }
        }
    }

    @Override
    public void onStarted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Jenkins.getInstance().getPlugin(Lamps.class);
        Set<String> jobs = plugin.getJobs();

        if (jobs.contains(run.getParent().getName())) {
            Set<String> ipAddresses = plugin.getIpsContainingJob(run.getParent().getName());
            Run previousBuild = run.getPreviousBuild();
            if (previousBuild == null) {
                for (String ipAddress : ipAddresses) {
                    sendNotification(ipAddress, Color.GREEN, Action.ON);
                }
            } else {
                for (String ipAddress : ipAddresses) {
                    sendNotification(ipAddress, resultColorMap.get(previousBuild.getResult()), Action.ON);
                }
            }
        }
    }

    private void sendNotification(String ipAddress, Color color, Action action) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("color", color);
        gitgear.put("action", action);
        byte[] data = gitgear.toString(2).getBytes();

        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), 39418);
            socket.send(packet);
        } catch (UnknownHostException e) {
            LOGGER.severe(e.getMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

}
