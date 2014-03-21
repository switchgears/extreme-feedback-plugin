package org.jenkinsci.plugins.extremefeedback;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.JenkinsEvent;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.zeromq.ZMQ;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZeroMQMessageHandler {
    private ConcurrentMap<String, String> latestMessages = Maps.newConcurrentMap();
    private static final ZeroMQMessageHandler instance = new ZeroMQMessageHandler();
    private boolean isStarted = false;

    private ZeroMQMessageHandler() {}

    public static ZeroMQMessageHandler getInstance() {
        return instance;
    }

    public void start() {
        if (!isStarted) {
            isStarted = true;
            final Lamps plugin = Lamps.getInstance();
            plugin.getEventBus().register(this);
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            try {
                executorService.submit(new Runnable() {
                    public void run() {
                        ZMQ.Context context = ZMQ.context(1);
                        ZMQ.Socket responder = context.socket(ZMQ.REP);
                        responder.bind("tcp://*:61616");

                        while (!Thread.currentThread().isInterrupted()) {
                            String request = responder.recvStr();
                            String message = Strings.nullToEmpty(latestMessages.get(request));
                            responder.send(message);
                            if (message.isEmpty() && plugin.getLamps().contains(new Lamp(request))) {
                                plugin.getLampByMacAddress(request);
                            }
                        }
                    }
                });
            } finally {
                executorService.shutdown();
            }
        }
    }

    @Subscribe
    public void listenEvents(JenkinsEvent event) {
        String json = event.getJson();
        JSONObject jsonObject = JSONObject.fromObject(json);
        latestMessages.put(jsonObject.getString("macAddress"), json);
    }
}
