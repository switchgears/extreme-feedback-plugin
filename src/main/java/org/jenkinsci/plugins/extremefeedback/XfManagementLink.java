package org.jenkinsci.plugins.extremefeedback;

import hudson.Extension;
import hudson.model.ManagementLink;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.extremefeedback.model.Lamp;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.Collection;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
@Extension
public class XfManagementLink extends ManagementLink {

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
        return "Manage the <a href=\"http://www.extremefeedback.com\">ExtremeFeedback.com</a> lamps.";
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
    public boolean changeLampName(String macAddress, String name) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        Set<Lamp> lamps = plugin.findLamps();
        for (Lamp lamp : lamps) {
            if (lamp.getMacAddress().equals(macAddress)) {
                lamp.setName(name);
                return true;
            }
        }
        return false;
    }

    @JavaScriptMethod
    public Set<Lamp> getLamps() {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        return plugin.getLamps();
    }

    @JavaScriptMethod
    public Collection<String> getProjects() {
        return jenkins.getJobNames();
    }

    @JavaScriptMethod
    public void addProjectToLamp(String projectName, String macAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        for (Lamp lamp : plugin.getLamps()) {
            if (lamp.getMacAddress().equals(macAddress)) {
                lamp.addJob(projectName);
            }
        }
    }

    @JavaScriptMethod
    public void removeProjectFromLamp(String projectName, String macAddress) {
        Lamps plugin = jenkins.getPlugin(Lamps.class);
        for (Lamp lamp : plugin.getLamps()) {
            if (lamp.getMacAddress().equals(macAddress)) {
                lamp.removeJob(projectName);
            }
        }
    }

}
