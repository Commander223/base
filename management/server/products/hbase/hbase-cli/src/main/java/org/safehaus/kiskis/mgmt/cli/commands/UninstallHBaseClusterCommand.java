package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "uninstall-cluster", description = "Command to uninstall HBase cluster")
public class UninstallHBaseClusterCommand extends OsgiCommandSupport {

    private HBase hbaseManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    @Argument(index = 0, name = "clusterName", required = true, multiValued = false, description = "Delete cluster")
    String clusterName;

    protected Object doExecute() {

        UUID uuid = hbaseManager.uninstallCluster(clusterName);
        tracker.printOperationLog(HBaseConfig.PRODUCT_KEY, uuid, 30000);
        return null;

    }
}
