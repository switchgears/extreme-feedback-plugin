package org.jenkinsci.plugins.extremefeedback;

import com.google.common.eventbus.Subscribe;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.model.JenkinsEvent;
import org.jenkinsci.plugins.extremefeedback.model.States;
import org.jenkinsci.plugins.extremefeedback.model.UdpMessageSender;
import org.jenkinsci.plugins.extremefeedback.model.XfEventMessage;


/**
 * Event message handler for sending UDP messages.
 * Listens to the event bus and sends udp messages to the lamp accordingly.
 *
 * Created by Aske Olsson - 2013-11-15
 */

public class EventMessageHandler {

    private static final int PORT = 39418;
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
        JSONObject json = JSONObject.fromObject(event);
        String message = convertJson(json);
        if ( !message.isEmpty()) {
            sendMessage(json.getString("macAddress"), message);
        }
    }

    public static String convertJson(JSONObject json) {
        String type = json.getString("type");
        String message = "";
        if (type.equals(XfEventMessage.Type.buzzer.toString())) {
            message = buildAlarmNotification();

        } else if (type.equals(XfEventMessage.Type.color.toString())) {
            message = buildColorNotification(json.getString("color"), json.getString("flashing"));

        } else if (type.equals(XfEventMessage.Type.soundalarm.toString())) {
            message = buildSfxNotification(json.getString("color"));

        } else if (type.equals(XfEventMessage.Type.lcdtext.toString())) {
            message = buildLCDNotification(json.getString("text"));
        }
        return message;
    }

    private static String buildColorNotification(String color, String flashing) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("color", color);
        gitgear.put("action", flashing.equals("true") ? States.Action.FLASHING : States.Action.SOLID);
        return gitgear.toString();
    }

    private static String buildAlarmNotification() {
        JSONObject gitgear = new JSONObject();
        gitgear.put("siren", "NA");
        gitgear.put("action", "ON");
        return gitgear.toString();
    }

    private static String buildSfxNotification(String color) {
        JSONObject gitgear = new JSONObject();
        gitgear.put("soundeffect", "NA");
        gitgear.put("color", color);
        return gitgear.toString();
    }

    private static String buildLCDNotification(String lcdText) {
        JSONObject displayText = new JSONObject();
        displayText.put("lcd_text", lcdText);
        return displayText.toString();
    }

    private void sendMessage(String macAddress, String msg){
        byte[] data = msg.getBytes();
        Lamps plugin = Lamps.getInstance();
        String ipAddress = plugin.getLampByMacAddress(macAddress);
        UdpMessageSender.send(ipAddress, PORT, data);
    }
}
