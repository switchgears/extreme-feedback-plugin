package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Set;

public class Lamp implements Comparable<Lamp>, Serializable {

    private static final long serialVersionUID = 6746373847474387387L;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.macAddress);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Lamp) {
            Lamp that = (Lamp) object;
            return Objects.equal(this.getMacAddress(), that.getMacAddress());
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("MAC", macAddress).toString();
    }
}
