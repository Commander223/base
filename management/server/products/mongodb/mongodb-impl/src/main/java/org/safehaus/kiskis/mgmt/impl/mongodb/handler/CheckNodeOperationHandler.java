package org.safehaus.kiskis.mgmt.impl.mongodb.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.mongodb.MongoImpl;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public CheckNodeOperationHandler(MongoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Checking state of %s in %s", lxcHostname, clusterName));
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
        po.addLog("Checking node...");
        Command checkNodeCommand = Commands.getCheckInstanceRunningCommand(node, config.getDomainName(), config.getNodePort(node));
        manager.getCommandRunner().runCommand(checkNodeCommand);

        if (checkNodeCommand.hasCompleted()) {
            AgentResult agentResult = checkNodeCommand.getResults().get(node.getUuid());
            if (agentResult != null) {
                if (agentResult.getStdOut().contains("couldn't connect to server")) {
                    po.addLogDone(String.format("Node on %s is %s", lxcHostname, NodeState.STOPPED));
                } else if (agentResult.getStdOut().contains("connecting to")) {
                    po.addLogDone(String.format("Node on %s is %s", lxcHostname, NodeState.RUNNING));
                } else {
                    po.addLogFailed(String.format("Node on %s is not found", lxcHostname));
                }
                return;
            }
        }
        po.addLogFailed(String.format("Error checking status of node %s : %s", node.getHostname(), checkNodeCommand.getAllErrors()));
    }
}
