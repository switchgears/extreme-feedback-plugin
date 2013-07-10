package org.jenkinsci.plugins.extremefeedback;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.ManagementLink;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Extension
public class XfManagementLink extends ManagementLink {

    private static final Logger LOGGER = Logger.getLogger("jenkins.plugins.extremefeedback");

    private final Jenkins jenkins = Jenkins.getInstance();

    @Override
    public String getIconFileName() {
        return "/plugin/extreme-feedback/traffic-light.png";
    }

    public String getDisplayName() {
        return "Extreme Feedback";
    }

    @Override
    public String getDescription() {
        return "Manage the <a href=\"http://www.gitgear.com/XFD\">gitgear.com</a> Extreme Feedback Lamps.";
    }

    @Override
    public String getUrlName() {
        return "extreme-feedback";
    }

    @JavaScriptMethod
    public Set<Lamp> findLamps() {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        return plugin.findLamps();
    }

    @JavaScriptMethod
    public boolean resetLamps() {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        plugin.setLamps(new TreeSet<Lamp>());
        try {
            plugin.save();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @JavaScriptMethod
    public boolean updateLamp(Lamp lamp) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        Map<String,Lamp> lamps = plugin.getLampsAsMap();
        lamps.put(lamp.getMacAddress(), lamp);
        plugin.setLamps(Sets.newHashSet(lamps.values()));
        try {
            plugin.save();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @JavaScriptMethod
    public boolean setLampAlarm(String macAddress, boolean status) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        for (Lamp lamp : plugin.getLamps()) {
            if (lamp.getMacAddress().equals(macAddress)) {
                lamp.setNoisy(status);
                try {
                    plugin.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @JavaScriptMethod
    public Set<Lamp> addLampByIpAddress(String ipAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        Set<Lamp> before = ImmutableSet.copyOf(plugin.getLamps());
        Set<Lamp> after = ImmutableSet.copyOf(plugin.addLampByIp(ipAddress));
        if (before != after) {
            return after;
        } else {
            return null;
        }
    }

    @JavaScriptMethod
    public boolean changeLampName(String macAddress, String name) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        Set<Lamp> lamps = plugin.getLamps();
        LOGGER.log(Level.INFO, "MAC to find: " + macAddress);
        for (Lamp lamp : lamps) {
            LOGGER.log(Level.INFO, "Checking: " + lamp.getMacAddress());
            if (lamp.getMacAddress().equals(macAddress)) {
                lamp.setName(name);
                return true;
            }
        }
        LOGGER.log(Level.WARNING, "Could not find lamp: " + macAddress + ' ' + name);
        return false;
    }

    @JavaScriptMethod
    public Set<Lamp> getLamps() {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        return plugin.getLamps();
    }

    @JavaScriptMethod
    public Lamp getLamp(String macAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        for (Lamp lamp : plugin.getLamps()) {
            if (lamp.getMacAddress().equals(macAddress)) {
                return lamp;
            }
        }
        return null;
    }

    @JavaScriptMethod
    public Collection<String> getProjects() {
        return jenkins.getJobNames();
    }

    @JavaScriptMethod
    public Collection<Lamp> addProjectToLamp(String projectName, String macAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        if (jenkins.getJobNames().contains(projectName)) {
            Map<String, Lamp> lamps = plugin.getLampsAsMap();
            Lamp lamp = lamps.get(macAddress);
            lamp.addJob(projectName);
            plugin.setLamps(Sets.newHashSet(lamps.values()));
            try {
                plugin.save();
            } catch (IOException e) {
                LOGGER.severe("Could not save the Lamps plugin");
                return new HashSet<Lamp>();
            }
        }
        return plugin.getLamps();
    }

    @JavaScriptMethod
    public Collection<Lamp> removeProjectFromLamp(String projectName, String macAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        Map<String, Lamp> lamps = plugin.getLampsAsMap();
        Lamp lamp = lamps.get(macAddress);
        lamp.removeJob(projectName);
        plugin.setLamps(Sets.newHashSet(lamps.values()));
        try {
            plugin.save();
        } catch (IOException e) {
            LOGGER.severe("Could not save the Lamps plugin");
            return new HashSet<Lamp>();
        }
        return plugin.getLamps();
    }

}
