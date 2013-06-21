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
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.jenkinsci.plugins.extremefeedback.model.UdpMessageSender;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Extension
public class XfRunListener extends RunListener<AbstractBuild> {

    private enum Color { GREEN, YELLOW, RED }
    private enum Action { SOLID, FLASHING }

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
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(run.getParent().getName());
            for (Lamp lamp : activeLamps) {
                sendColorNotification(lamp.getIpAddress(), resultColorMap.get(result), Action.SOLID);
                if (resultColorMap.get(result).equals(Color.RED) && lamp.isNoisy()) {
                    try {
                        Thread.sleep(1000);
                        sendAlarmNotification(lamp.getIpAddress());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onStarted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Jenkins.getInstance().getPlugin(Lamps.class);
        Set<String> jobs = plugin.getJobs();

        if (jobs.contains(run.getParent().getName())) {
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(run.getParent().getName());
            Run previousBuild = run.getPreviousBuild();
            if (previousBuild == null) {
                for (Lamp lamp : activeLamps) {
                    sendColorNotification(lamp.getIpAddress(), Color.GREEN, Action.SOLID);
                }
            } else {
                for (Lamp lamp : activeLamps) {
                    sendColorNotification(lamp.getIpAddress(), resultColorMap.get(previousBuild.getResult()), Action.FLASHING);
                }
            }
        }
    }

    private void sendColorNotification(String ipAddress, Color color, Action action) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("color", color);
        gitgear.put("action", action);
        byte[] data = gitgear.toString(2).getBytes();
        int port = 39418;
        UdpMessageSender.send(ipAddress, port, data);
    }

    private void sendAlarmNotification(String ipAddress) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("siren", "NA");
        gitgear.put("action", "ON");
        byte[] data = gitgear.toString(2).getBytes();
        int port = 39418;
        UdpMessageSender.send(ipAddress, port, data);
    }

}
