package org.jenkinsci.plugins.extremefeedback;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import hudson.Plugin;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.jenkinsci.plugins.extremefeedback.model.LampFinderCallable;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Lamps extends Plugin {

    Set<Lamp> lamps = Sets.newTreeSet();
    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    public Set<Lamp> findLamps() {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        ListenableFuture<Set<Lamp>> listenableFuture = service.submit(new LampFinderCallable());
        Futures.addCallback(listenableFuture, new FutureCallback<Set<Lamp>>() {
            public void onSuccess(Set<Lamp> l) {
                lamps = l;
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
}
