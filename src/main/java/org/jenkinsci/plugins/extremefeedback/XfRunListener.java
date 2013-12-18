package org.jenkinsci.plugins.extremefeedback;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

@Extension
public class XfRunListener extends RunListener<AbstractBuild> {

    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");


    @Override
    public void onCompleted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Lamps.getInstance();
        Set<String> jobs = plugin.getJobs();
        String jobName = run.getParent().getFullName();
        XfEventMessage xfEventMessage = new XfEventMessage();

        if (jobs.contains(jobName)) {
            Result result = run.getResult();
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(jobName);
            for (Lamp lamp : activeLamps) {
                Result lampResult = result;

                xfEventMessage.sendColorMessage(lamp, lampResult, States.Action.SOLID);

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
                    if (blame.isEmpty()) {
                        blame.add("Somebody");
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
                xfEventMessage.sendLCDMessage(lamp, infoMsg.toString());

                if (lamp.isSfx()) {
                    try {
                        Thread.sleep(1000);
                        xfEventMessage.sendSfxMessage(lamp, lampResult);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (States.resultColorMap.get(lampResult).equals(States.Color.RED) && lamp.isNoisy()) {
                    try {
                        Thread.sleep(1000);
                        xfEventMessage.sendBuzzerMessage(lamp);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (lamp.isAggregate()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    plugin.updateAggregateStatus(lamp);
                }
            }
        }
    }

    @Override
    public void onStarted(AbstractBuild run, TaskListener listener) {
        Lamps plugin = Lamps.getInstance();
        Set<String> jobs = plugin.getJobs();
        String jobName = run.getParent().getName();
        XfEventMessage xfEventMessage = new XfEventMessage();

        if (jobs.contains(jobName)) {
            Set<Lamp> activeLamps = plugin.getLampsContainingJob(jobName);
            Run previousBuild = run.getPreviousBuild();
            if (previousBuild == null) {
                for (Lamp lamp : activeLamps) {
                    xfEventMessage.sendColorMessage(lamp, Result.SUCCESS, States.Action.SOLID);
                    xfEventMessage.sendLCDMessage(lamp, jobName + ' ' + run.getDisplayName() + "\nStarted");

                }
            } else {
                for (Lamp lamp : activeLamps) {
                    xfEventMessage.sendColorMessage(lamp, previousBuild.getResult(), States.Action.FLASHING);
                    xfEventMessage.sendLCDMessage(lamp, jobName + ' ' + run.getDisplayName() + "\nStarted");
                }
            }
        }
    }
}
