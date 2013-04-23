package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.collect.Sets;

import java.util.Set;

public class Lamp implements Comparable<Lamp> {
    private String ipAddress;
    private String macAddress;
    private String name;
    private Set<String> jobs = Sets.newHashSet();

    public Lamp(String macAddress, String ipAddress) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getJobs() {
        return jobs;
    }

    public void setJobs(Set<String> jobs) {
        this.jobs = jobs;
    }

    public void addJobs(Set<String> jobs) {
        this.jobs.addAll(jobs);
    }

    public int compareTo(Lamp o) {
        return this.macAddress.compareTo(o.getMacAddress());
    }

    public void addJob(String job) {
        this.jobs.add(job);
    }

    public void removeJob(String job) {
        this.jobs.remove(job);
    }
}
