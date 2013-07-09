package org.jenkinsci.plugins.extremefeedback.model;

public class JenkinsEvent {
    private final String json;

    public JenkinsEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
