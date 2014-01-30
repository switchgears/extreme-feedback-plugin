package org.jenkinsci.plugins.extremefeedback;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;

import java.io.IOException;
import java.util.Set;

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
            if (lamp.isAggregate() && !plugin.isBuilding(lamp)) {
                plugin.updateAggregateStatus(lamp);
            }
            else {
                // figure out the last job that ran for the lamp
                String lastJob = plugin.getLastJob(lamp.getJobs());
                plugin.updateJobStatus(lamp, lastJob);
            }
        }
    }

    @Override
    public long getRecurrencePeriod() {
        // Run every 5 minutes
        return 5*60*1000;
    }
}