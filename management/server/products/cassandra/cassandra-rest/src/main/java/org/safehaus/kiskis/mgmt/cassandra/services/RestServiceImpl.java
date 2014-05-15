package org.safehaus.kiskis.mgmt.cassandra.services;

import org.safehaus.kiskis.mgmt.api.cassandra.Cassandra;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;

import java.util.List;
import java.util.UUID;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Cassandra cassandraManager;

    public void setCassandraManager(Cassandra cassandraManager) {
        this.cassandraManager = cassandraManager;
    }

    public Cassandra getCassandraManager() {
        return cassandraManager;
    }

    public RestServiceImpl() {
    }

    @Override
    public String install(String clusterName, String domainName, String numberOfNodes, String numberOfSeeds) {
        Config config = new Config();
        config.setClusterName(clusterName);
        config.setDomainName(domainName);
        config.setNumberOfNodes(Integer.parseInt(numberOfNodes));
        config.setNumberOfSeeds(Integer.parseInt(numberOfSeeds));

        UUID uuid = cassandraManager.installCluster(config);
        return uuid.toString();
    }

    @Override
    public String uninstall(String clusterName) {
        UUID uuid = cassandraManager.uninstallCluster(clusterName);
        return uuid.toString();
    }

}