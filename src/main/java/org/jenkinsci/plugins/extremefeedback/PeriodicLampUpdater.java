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
            plugin.updateLampStatus(lamp);
        }
    }

    @Override
    public long getRecurrencePeriod() {
        // Run every minute
        return 1*60*1000;
    }
}