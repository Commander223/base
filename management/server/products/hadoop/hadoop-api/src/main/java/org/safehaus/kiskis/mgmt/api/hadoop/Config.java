package org.safehaus.kiskis.mgmt.api.hadoop;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daralbaev on 02.04.14.
 */
public class Config {
    public static final String PRODUCT_KEY = "Hadoop";
    public static final int
            NAME_NODE_PORT = 8020,
            JOB_TRACKER_PORT = 9000;

    private String clusterName, domainName;
    private Agent nameNode, jobTracker, secondaryNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor, countOfSlaveNodes;

    public Config() {
        domainName = "intra.lan";
        dataNodes = new ArrayList<Agent>();
        taskTrackers = new ArrayList<Agent>();
    }

    public List<Agent> getAllNodes() {
        Set<Agent> allAgents = new HashSet<Agent>();
        if (dataNodes != null) {
            allAgents.addAll(dataNodes);
        }
        if (taskTrackers != null) {
            allAgents.addAll(taskTrackers);
        }

        if (nameNode != null) {
            allAgents.add(nameNode);
        }
        if (jobTracker != null) {
            allAgents.add(jobTracker);
        }
        if (secondaryNameNode != null) {
            allAgents.add(secondaryNameNode);
        }

        return new ArrayList<Agent>(allAgents);
    }

    public List<Agent> getAllSlaveNodes() {
        Set<Agent> allAgents = new HashSet<Agent>();
        if (dataNodes != null) {
            allAgents.addAll(dataNodes);
        }
        if (taskTrackers != null) {
            allAgents.addAll(taskTrackers);
        }

        return new ArrayList<Agent>(allAgents);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public Agent getNameNode() {
        return nameNode;
    }

    public void setNameNode(Agent nameNode) {
        this.nameNode = nameNode;
    }

    public Agent getJobTracker() {
        return jobTracker;
    }

    public void setJobTracker(Agent jobTracker) {
        this.jobTracker = jobTracker;
    }

    public Agent getSecondaryNameNode() {
        return secondaryNameNode;
    }

    public void setSecondaryNameNode(Agent secondaryNameNode) {
        this.secondaryNameNode = secondaryNameNode;
    }

    public List<Agent> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<Agent> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public List<Agent> getTaskTrackers() {
        return taskTrackers;
    }

    public void setTaskTrackers(List<Agent> taskTrackers) {
        this.taskTrackers = taskTrackers;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Integer getCountOfSlaveNodes() {
        return countOfSlaveNodes;
    }

    public void setCountOfSlaveNodes(Integer countOfSlaveNodes) {
        this.countOfSlaveNodes = countOfSlaveNodes;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", nameNode=" + nameNode +
                ", jobTracker=" + jobTracker +
                ", secondaryNameNode=" + secondaryNameNode +
                ", dataNodes=" + dataNodes +
                ", taskTrackers=" + taskTrackers +
                ", replicationFactor=" + replicationFactor +
                ", countOfSlaveNodes=" + countOfSlaveNodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (clusterName != null ? !clusterName.equals(config.clusterName) : config.clusterName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clusterName != null ? clusterName.hashCode() : 0;
    }
}
