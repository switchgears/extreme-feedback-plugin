package org.jenkinsci.plugins.extremefeedback;

import com.google.common.eventbus.Subscribe;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.*;

import java.util.logging.Logger;

/**
 * Event message handler for sending UDP messages.
 * Listens to the event bus and sends udp messages to the lamp accordingly.
 *
 * Created by Aske Olsson - 2013-11-15
 */

public class EventMessageHandler {

    private static final int PORT = 39418;
    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");
    private boolean isStarted = false;

    private EventMessageHandler() {}

    public void start() {
        if (!isStarted) {
            isStarted = true;
            Lamps plugin = Lamps.getInstance();
            plugin.getEventBus().register(this);
        }
    }

    private static class EventMessageObject {
        public static final EventMessageHandler INSTANCE = new EventMessageHandler();
    }

    public static EventMessageHandler getInstance() {
        return EventMessageObject.INSTANCE;
    }

    @Subscribe
    public void listenEvents(JenkinsEvent event) {
        handleEvent(event.getJson());
    }

    private void handleEvent(String event) {
        if (event.endsWith(",")) {
            event = event.substring(0, event.length() - 1);
        }
        JSONObject json = JSONObject.fromObject(event);
        String message = convertJson(json);
        if ( !message.isEmpty()) {
            sendMessage(json.getString("macAddress"), message);
        }
    }

    public static String convertJson(JSONObject json) {
        String type = json.getString("type");
        String name = json.getString("name");
        String message = "";
        if (type.equals(XfEventMessage.Type.buzzer.toString())) {
            message = buildAlarmNotification(name);

        } else if (type.equals(XfEventMessage.Type.color.toString())) {
            message = buildColorNotification(json.getString("color"), json.getString("flashing"), name);

        } else if (type.equals(XfEventMessage.Type.soundalarm.toString())) {
            message = buildSfxNotification(json.getString("color"), name);

        } else if (type.equals(XfEventMessage.Type.lcdtext.toString())) {
            message = buildLCDNotification(json.getString("text"), name);
        }
        return message;
    }

    private static String buildColorNotification(String color, String flashing, String name) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("Lamp", name);
        gitgear.put("color", color);
        gitgear.put("action", flashing.equals("true") ? States.Action.FLASHING : States.Action.SOLID);
        return gitgear.toString();
    }

    private static String buildAlarmNotification(String name) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("Lamp", name);
        gitgear.put("siren", "NA");
        gitgear.put("action", "ON");
        return gitgear.toString();
    }

    private static String buildSfxNotification(String color, String name) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("Lamp", name);
        gitgear.put("soundeffect", "NA");
        gitgear.put("color", color);
        return gitgear.toString();
    }

    private static String buildLCDNotification(String lcdText, String name) {
        JSONObject displayText = new JSONObject();
        displayText.put("Lamp", name);
        displayText.put("lcd_text", lcdText);
        return displayText.toString();
    }

    private void sendMessage(String macAddress, String msg){
        byte[] data = msg.getBytes();
        Lamps plugin = Lamps.getInstance();
        Lamp lamp = plugin.getLampByMacAddress(macAddress);
        if (!lamp.isInactive()) {
            String ipAddress = lamp.getIpAddress();
            LOGGER.info("[XFD] sending message to: " + ipAddress + " message: " + msg);
            UdpMessageSender.send(ipAddress, PORT, data);
        }
    }
}
