package org.jenkinsci.plugins.extremefeedback;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import hudson.Plugin;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.jenkinsci.plugins.extremefeedback.model.LampFinderCallable;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.difference;

public class Lamps extends Plugin {

    Set<Lamp> lamps = Sets.newTreeSet();
    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    @Override
    public void start() throws Exception {
        load();
    }

    public Set<Lamp> findLamps() {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        ListenableFuture<Set<Lamp>> listenableFuture = service.submit(new LampFinderCallable());
        Futures.addCallback(listenableFuture, new FutureCallback<Set<Lamp>>() {
            public void onSuccess(Set<Lamp> l) {
                if (lamps.isEmpty()){
                    lamps = l;
                }
                else {
                    Set<Lamp> diff = difference(l, lamps);
                    for (Lamp lamp : diff) {
                        LOGGER.log(Level.INFO, "New lamp found: " + lamp.getMacAddress());
                        lamps.add(lamp);
                    }
                }
            }

            public void onFailure(Throwable throwable) {
                LOGGER.warning(throwable.getMessage());
            }
        });

        while(!listenableFuture.isDone()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }

        return lamps;
    }

    public Set<Lamp> getLamps() {
        return lamps;
    }

    public Set<String> getJobs() {
        Set<String> jobs = Sets.newHashSet();
        for (Lamp lamp : lamps) {
            jobs.addAll(lamp.getJobs());
        }
        return jobs;
    }

    public Set<String> getIpsContainingJob(String jobName) {
        Set<String> ipAddresses = Sets.newHashSet();
        for (Lamp lamp : lamps) {
            if (lamp.getJobs().contains(jobName)) {
                ipAddresses.add(lamp.getIpAddress());
            }
        }
        return ipAddresses;
    }
}
