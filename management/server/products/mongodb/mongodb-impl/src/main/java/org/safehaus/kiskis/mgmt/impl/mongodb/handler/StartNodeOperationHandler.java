package org.safehaus.kiskis.mgmt.impl.mongodb.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.mongodb.MongoImpl;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public StartNodeOperationHandler(MongoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostname, clusterName));
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

        Command startNodeCommand;
        NodeType nodeType = config.getNodeType(node);

        if (nodeType == NodeType.CONFIG_NODE) {
            startNodeCommand = Commands.getStartConfigServerCommand(config.getCfgSrvPort(), Util.wrapAgentToSet(node));
        } else if (nodeType == NodeType.DATA_NODE) {
            startNodeCommand = Commands.getStartDataNodeCommand(config.getDataNodePort(), Util.wrapAgentToSet(node));
        } else {
            startNodeCommand = Commands.getStartRouterCommand(
                    config.getRouterPort(), config.getCfgSrvPort(),
                    config.getDomainName(), config.getConfigServers(),
                    Util.wrapAgentToSet(node));
        }
        po.addLog("Starting node...");
        manager.getCommandRunner().runCommand(startNodeCommand, new CommandCallback() {

            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                if (agentResult.getStdOut().contains("child process started successfully, parent exiting")) {

                    command.setData(NodeState.RUNNING);

                    stop();
                }
            }

        });

        if (NodeState.RUNNING.equals(startNodeCommand.getData())) {
            po.addLogDone(String.format("Node on %s started", lxcHostname));
        } else {
            po.addLogFailed(String.format("Failed to start node %s. %s",
                    lxcHostname,
                    startNodeCommand.getAllErrors()
            ));
        }
    }
}
