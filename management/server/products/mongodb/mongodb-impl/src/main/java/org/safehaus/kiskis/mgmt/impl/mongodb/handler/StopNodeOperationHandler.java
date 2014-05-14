package org.safehaus.kiskis.mgmt.impl.mongodb.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.mongodb.MongoImpl;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public StopNodeOperationHandler(MongoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
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
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }

        po.addLog("Stopping node...");
        Command stopNodeCommand = Commands.getStopNodeCommand(Util.wrapAgentToSet(node));
        manager.getCommandRunner().runCommand(stopNodeCommand);

        if (stopNodeCommand.hasSucceeded()) {
            po.addLogDone(String.format("Node on %s stopped", lxcHostname));
        } else {
            po.addLogFailed(String.format("Failed to stop node %s. %s",
                    lxcHostname,
                    stopNodeCommand.getAllErrors()
            ));
        }
    }
}
