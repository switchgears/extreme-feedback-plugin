package org.jenkinsci.plugins.extremefeedback;

import com.google.common.eventbus.Subscribe;
import hudson.Extension;
import hudson.cli.CLICommand;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.extremefeedback.model.JenkinsEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Extension
public class EventStreamCommand extends CLICommand {

    private Queue<String> events = new ConcurrentLinkedDeque<String>();

    public EventStreamCommand() {
        super();
        Lamps plugin = Jenkins.getInstance().getPlugin(Lamps.class);
        plugin.getEventBus().register(this);
    }

    @Override
    public String getShortDescription() {
        return "xf-event-stream";
    }

    @Override
    public String getName() {
        return "xf-events";
    }

    @Subscribe
    public void listenEvents(JenkinsEvent event) {
        events.add(event.getJson());
    }

    @Override
    protected int run() throws Exception {
        boolean flag = true;
        while(flag) {
            String event = events.poll();
            if (event != null) {
                try {
                    stdout.println(event);
                    stdout.flush();
                } catch (Exception e) {
                    flag = false;
                }
            }

            if (events.isEmpty()) {
                Thread.sleep(100);
            }
        }
        return 0;
    }
}
