package org.jenkinsci.plugins.extremefeedback;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.JenkinsEvent;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ZeroMQMessageHandler {
    private static final Multimap<String, String> messages = ArrayListMultimap.create();
    private static final ZeroMQMessageHandler instance = new ZeroMQMessageHandler();
    private boolean isStarted = false;

    private static final char SEPARATOR = ((char)007);

    private final static Logger LOGGER = Logger.getLogger(ZeroMQMessageHandler.class.getName());

    private ZeroMQMessageHandler() {}

    public static ZeroMQMessageHandler getInstance() {
        return instance;
    }

    public void start() {
        if (!isStarted) {
            LOGGER.info("ZeroMQ message handler started");
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
                            String request = responder.recvStr().trim();
                            String message = "";
                            synchronized (messages) {
                                List<String> converted = Lists.newArrayList();
                                for (String req : messages.get(request)) {
                                    converted.add(EventMessageHandler.convertJson(JSONObject.fromObject(req)));
                                }
                                messages.removeAll(request);
                                message = Joiner.on(SEPARATOR).join(converted);
                            }
                            responder.send(message);
                            if (!message.isEmpty()) {
                            }
                            if (message.isEmpty() && !(plugin.getLamps().contains(new Lamp(request)))) {
                                LOGGER.info("Creating lamp with Mac address " + request);
                                plugin.addLampByMacAddress(request);
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
        LOGGER.info("Event received for " + jsonObject.getString("macAddress"));
        synchronized (messages) {
            messages.put(jsonObject.getString("macAddress"), json);
        }
    }

}
