package org.safehaus.kiskis.mgmt.impl.spark.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.spark.Commands;
import org.safehaus.kiskis.mgmt.impl.spark.SparkImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
    private final ProductOperation po;
    private final String lxcHostname;
    private final boolean master;

    public StopNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname, boolean master) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        this.master = master;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (node == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
            return;
        }

        if (!config.getAllNodes().contains(node)) {
            po.addLogFailed(String.format("Node %s does not belong to this cluster", lxcHostname));
            return;
        }

        if (master && !config.getMasterNode().equals(node)) {
            po.addLogFailed(String.format("Node %s is not a master node\nOperation aborted", node.getHostname()));
            return;
        } else if (!master && !config.getSlaveNodes().contains(node)) {
            po.addLogFailed(String.format("Node %s is not a slave node\nOperation aborted", node.getHostname()));
            return;
        }

        po.addLog(String.format("Stopping %s on %s...", master ? "master" : "slave", node.getHostname()));

        Command stopCommand;
        if (master) {
            stopCommand = Commands.getStopMasterCommand(node);
        } else {
            stopCommand = Commands.getStopSlaveCommand(node);
        }
        manager.getCommandRunner().runCommand(stopCommand);

        if (stopCommand.hasSucceeded()) {
            po.addLogDone(String.format("Node %s stopped", node.getHostname()));
        } else {
            po.addLogFailed(String.format("Stopping %s failed, %s", node.getHostname(), stopCommand.getAllErrors()));
        }
    }
}
