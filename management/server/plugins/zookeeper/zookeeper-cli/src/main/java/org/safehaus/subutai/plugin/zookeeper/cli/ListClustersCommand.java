package org.safehaus.subutai.plugin.zookeeper.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "zookeeper", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Zookeeper zookeeperManager;

    public Zookeeper getZookeeperManager() {
        return zookeeperManager;
    }

    public void setZookeeperManager(Zookeeper zookeeperManager) {
        this.zookeeperManager = zookeeperManager;
    }

    protected Object doExecute() {
        List<ZookeeperClusterConfig> configList = zookeeperManager.getClusters();
        if (!configList.isEmpty())
            for (ZookeeperClusterConfig config : configList) {
                System.out.println(config.getClusterName());
            }
        else System.out.println("No Zookeeper cluster");

        return null;
    }
}
