package org.jenkinsci.plugins.extremefeedback.model;

import hudson.model.Result;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremefeedback.Lamps;


/**
 * Send messages on the event bus
 *
 * Created by Aske Olsson - 2013-11-12
 */

public class XfEventMessage {

    public enum Type {buzzer, color, soundalarm, lcdtext}

    public void sendBuzzerMessage(Lamp lamp) {
        String eventMessage = buildBuzzerJson(lamp);
        sendEventMessage(eventMessage);
    }

    public void sendColorMessage(Lamp lamp, Result result, States.Action action) {
        String eventMessage = buildColorJson(States.resultColorMap.get(result).toString(), lamp, action.equals(States.Action.FLASHING));
        sendEventMessage(eventMessage);
    }

    public void sendSfxMessage(Lamp lamp, Result result) {
        String eventMessage = buildSfxJson(States.resultColorMap.get(result).toString(), lamp);
        sendEventMessage(eventMessage);
    }

    public void sendLCDMessage(Lamp lamp, String lcdText) {
        String eventMessage = buildLCDJson(lamp, lcdText);
        sendEventMessage(eventMessage);
    }


    private String buildBuzzerJson(Lamp lamp) {
        JSONObject jsonBuzzer = new JSONObject();
        jsonBuzzer.accumulate("macAddress", lamp.getMacAddress());
        jsonBuzzer.accumulate("type", Type.buzzer);
        return jsonBuzzer.toString();
    }

    private String buildColorJson(String color, Lamp lamp, boolean flashing) {
        JSONObject jsonColor = new JSONObject();
        jsonColor.accumulate("macAddress", lamp.getMacAddress());
        jsonColor.accumulate("type", Type.color);
        jsonColor.accumulate("color", color);
        jsonColor.accumulate("flashing", flashing);
        return jsonColor.toString();
    }

    private String buildSfxJson(String color, Lamp lamp) {
        JSONObject jsonSfx = new JSONObject();
        jsonSfx.accumulate("macAddress", lamp.getMacAddress());
        jsonSfx.accumulate("type", Type.soundalarm);
        jsonSfx.accumulate("color", color);
        return jsonSfx.toString();
    }

    private String buildLCDJson(Lamp lamp, String lcdText) {
        JSONObject jsonLcd = new JSONObject();
        jsonLcd.accumulate("macAddress", lamp.getMacAddress());
        jsonLcd.accumulate("type", Type.lcdtext);
        jsonLcd.accumulate("text", lcdText);
        return jsonLcd.toString();
    }

    private void sendEventMessage(String message) {
        Lamps plugin = Lamps.getInstance();
        plugin.getEventBus().post(new JenkinsEvent(message));
    }

}
