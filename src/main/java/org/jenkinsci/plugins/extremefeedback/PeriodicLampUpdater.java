package org.jenkinsci.plugins.extremefeedback;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;

import java.io.IOException;
import java.util.*;

/**
 * Update Lamps periodically
 *
 * Created by Aske Olsson - 2013-11-20
 */
@Extension
public class PeriodicLampUpdater extends AsyncPeriodicWork {

    public PeriodicLampUpdater() {
        super("extreme-feedback");
    }

    @Override
    protected void execute(TaskListener taskListener) throws IOException, InterruptedException {
        Lamps plugin = Lamps.getInstance();
        Set<Lamp> lamps = plugin.getLamps();
        for (Lamp lamp : lamps) {
            if (lamp.isAggregate() && !building(lamp)) {
                plugin.updateAggregateStatus(lamp);
            }
            else {
                // figure out the last job that ran for the lamp
                String lastJob = lastJob(lamp.getJobs());
                plugin.updateJobStatus(lamp, lastJob);
            }
        }
    }

    // Find the job that ran last time
    private String lastJob(Set<String> lampJobs) {
        Date newest = new Date(getInitialDelay());
        String project = "";
        for (String lampJob : lampJobs) {
            TopLevelItem item = Jenkins.getInstance().getItem(lampJob);
            if (item instanceof AbstractProject) {
                AbstractProject job = (AbstractProject) item;
                Run current = job.getLastBuild();
                Date date = current.getTime();
                if (date.after(newest)) {
                    newest = date;
                    project = job.getFullName();
                }
            }
        }
        return project;
    }

    private boolean building(Lamp lamp) {
        for (String lampJob : lamp.getJobs()) {
            TopLevelItem item = Jenkins.getInstance().getItem(lampJob);
            if (item instanceof AbstractProject) {
                AbstractProject job = (AbstractProject) item;
                if (job.isBuilding()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long getRecurrencePeriod() {
        // Run every 5 minutes
        return 5*60*1000;
    }
}