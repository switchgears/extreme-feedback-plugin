package org.jenkinsci.plugins.extremefeedback;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.JenkinsEvent;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.jenkinsci.plugins.extremefeedback.model.States;
import org.jenkinsci.plugins.extremefeedback.model.UdpMessageSender;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

@Extension
public class XfRunListener extends RunListener<AbstractBuild> {

    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");


    @Override
    public void onCompleted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Jenkins.getInstance().getPlugin(Lamps.class);
        Set<String> jobs = plugin.getJobs();
        String jobName = run.getParent().getName();

        if (jobs.contains(jobName)) {
            Result result = run.getResult();
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(jobName);
            for (Lamp lamp : activeLamps) {
                Result lampResult = result;

                String jsonColor = buildColorJson(States.resultColorMap.get(lampResult).toString(), lamp, false);
                plugin.getEventBus().post(new JenkinsEvent(jsonColor));
                sendColorNotification(lamp.getIpAddress(), States.resultColorMap.get(lampResult), States.Action.SOLID);

                if (lamp.isAggregate()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (String lampJob : lamp.getJobs()) {
                        TopLevelItem item = Jenkins.getInstance().getItem(lampJob);
                        if (item instanceof AbstractProject) {
                            AbstractProject job = (AbstractProject) item;
                            Result lastResult;
                            if (job.getLastBuild() != null) {
                                lastResult = job.getLastBuild().getResult();
                            } else {
                                lastResult = Result.SUCCESS;
                            }

                            if (lastResult.isWorseThan(lampResult)) {
                                lampResult = lastResult;
                            }
                        }
                    }
                }

                jsonColor = buildColorJson(States.resultColorMap.get(lampResult).toString(), lamp, false);
                plugin.getEventBus().post(new JenkinsEvent(jsonColor));
                sendColorNotification(lamp.getIpAddress(), States.resultColorMap.get(lampResult), States.Action.SOLID);

                // Create Notification for LCD
                StringBuilder infoMsg = new StringBuilder(64);
                infoMsg.append(jobName).append(' ').append(run.getDisplayName()).append('\n');
                if (Result.FAILURE.equals(result)) {
                    ArrayList<String> blame = Lists.newArrayList();
                    if (lamp.isBlame()) {
                        Set<User> culprits = run.getCulprits();
                        for (User user : culprits) {
                            blame.add(user.getDisplayName());
                        }
                    }
                    infoMsg.insert(0, Joiner.on(", ").join(blame) + " broke the build: ");
                    infoMsg.append(result.toString());
                    listener.getLogger().println("[XFD] Updating Lamp display: " + infoMsg.toString());
                } else if (Result.ABORTED.equals(result)) {
                    String causeMsg = "BUILD ABORTED";
                    infoMsg.append(causeMsg);
                    listener.getLogger().println("[XFD] Updating Lamp display: " + infoMsg.toString());
                } else {
                    infoMsg.append(result.toString());
                }
                sendLCDTextNotification(lamp.getIpAddress(), infoMsg.toString());

                if (lamp.isSfx()) {
                    try {
                        Thread.sleep(1000);
                        String jsonSfx = buildSfxJson(States.resultColorMap.get(lampResult).toString(), lamp);
                        plugin.getEventBus().post(new JenkinsEvent(jsonSfx));

                        sendSfxNotification(lamp.getIpAddress(), States.resultColorMap.get(lampResult));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (States.resultColorMap.get(lampResult).equals(States.Color.RED) && lamp.isNoisy()) {
                    try {
                        Thread.sleep(1000);
                        String jsonBuzzer = buildBuzzerJson(lamp);
                        plugin.getEventBus().post(new JenkinsEvent(jsonBuzzer));

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
        String jobName = run.getParent().getName();

        if (jobs.contains(jobName)) {
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(jobName);
            Run previousBuild = run.getPreviousBuild();
            if (previousBuild == null) {
                for (Lamp lamp : activeLamps) {
                    String jsonColor = buildColorJson(States.Color.GREEN.toString(), lamp, false);
                    plugin.getEventBus().post(new JenkinsEvent(jsonColor));

                    sendColorNotification(lamp.getIpAddress(), States.Color.GREEN, States.Action.SOLID);
                    sendLCDTextNotification(lamp.getIpAddress(), jobName + ' ' + run.getDisplayName() + "\nStarted");

                }
            } else {
                for (Lamp lamp : activeLamps) {
                    String jsonColor = buildColorJson(States.resultColorMap.get(previousBuild.getResult()).toString(), lamp, true);
                    plugin.getEventBus().post(new JenkinsEvent(jsonColor));

                    sendColorNotification(lamp.getIpAddress(), States.resultColorMap.get(previousBuild.getResult()), States.Action.FLASHING);
                    sendLCDTextNotification(lamp.getIpAddress(), jobName + ' ' + run.getDisplayName() + "\nStarted");

                }
            }
        }
    }

    private String buildBuzzerJson(Lamp lamp) {
        JSONObject jsonBuzzer = new JSONObject();
        jsonBuzzer.accumulate("macAddress", lamp.getMacAddress());
        jsonBuzzer.accumulate("type", "buzzer");
        return jsonBuzzer.toString() + ",";
    }

    private String buildColorJson(String color, Lamp lamp, boolean flashing) {
        JSONObject jsonColor = new JSONObject();
        jsonColor.accumulate("macAddress", lamp.getMacAddress());
        jsonColor.accumulate("type", "color");
        jsonColor.accumulate("color", color);
        jsonColor.accumulate("flashing", flashing);
        return jsonColor.toString() + ",";
    }

    private String buildSfxJson(String color, Lamp lamp) {
        JSONObject jsonSfx = new JSONObject();
        jsonSfx.accumulate("macAddress", lamp.getMacAddress());
        jsonSfx.accumulate("type", "soundalarm");
        jsonSfx.accumulate("color", color);
        return jsonSfx.toString() + ",";
    }

    private void sendColorNotification(String ipAddress, States.Color color, States.Action action) {
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

    private void sendSfxNotification(String ipAddress, States.Color color) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("soundeffect", "NA");
        gitgear.put("color", color);
        byte[] data = gitgear.toString(2).getBytes();
        int port = 39418;
        UdpMessageSender.send(ipAddress, port, data);
    }

    private void sendLCDTextNotification(String ipAddress, String lcdText) {
        JSONObject displayText = new JSONObject();
        displayText.put("lcd_text", lcdText);
        byte[] data = displayText.toString(2).getBytes();
        int port = 39418;
        UdpMessageSender.send(ipAddress, port, data);
    }


}
