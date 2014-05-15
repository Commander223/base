package org.safehaus.kiskis.mgmt.impl.presto.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.presto.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.presto.Commands;
import org.safehaus.kiskis.mgmt.impl.presto.PrestoImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dilshat on 5/7/14.
 */
public class ChangeCoordinatorNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {
    private final ProductOperation po;
    private final String newCoordinatorHostname;

    public ChangeCoordinatorNodeOperationHandler(PrestoImpl manager, String clusterName, String newCoordinatorHostname) {
        super(manager, clusterName);
        this.newCoordinatorHostname = newCoordinatorHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Changing coordinator to %s in %s", newCoordinatorHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        final Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        if (manager.getAgentManager().getAgentByHostname(config.getCoordinatorNode().getHostname()) == null) {
            po.addLogFailed(String.format("Coordinator %s is not connected\nOperation aborted", config.getCoordinatorNode().getHostname()));
            return;
        }

        Agent newCoordinator = manager.getAgentManager().getAgentByHostname(newCoordinatorHostname);
        if (newCoordinator == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", newCoordinatorHostname));
            return;
        }

        if (newCoordinator.equals(config.getCoordinatorNode())) {
            po.addLogFailed(String.format("Node %s is already a coordinator node\nOperation aborted", newCoordinatorHostname));
            return;
        }

        //check if node is in the cluster
        if (!config.getWorkers().contains(newCoordinator)) {
            po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", newCoordinatorHostname));
            return;
        }

        po.addLog("Stopping all nodes...");
        //stop all nodes
        Command stopNodesCommand = Commands.getStopCommand(config.getAllNodes());
        manager.getCommandRunner().runCommand(stopNodesCommand);

        if (stopNodesCommand.hasSucceeded()) {
            po.addLog("All nodes stopped\nConfiguring coordinator...");

            Command configureCoordinatorCommand = Commands.getSetCoordinatorCommand(newCoordinator);
            manager.getCommandRunner().runCommand(configureCoordinatorCommand);

            if (configureCoordinatorCommand.hasSucceeded()) {
                po.addLog("Coordinator configured successfully");
            } else {
                po.addLogFailed(String.format("Failed to configure coordinator, %s\nOperation aborted", configureCoordinatorCommand.getAllErrors()));
                return;
            }

            config.getWorkers().add(config.getCoordinatorNode());
            config.getWorkers().remove(newCoordinator);
            config.setCoordinatorNode(newCoordinator);

            po.addLog("Configuring workers...");

            Command configureWorkersCommand = Commands.getSetWorkerCommand(newCoordinator, config.getWorkers());
            manager.getCommandRunner().runCommand(configureWorkersCommand);

            if (configureWorkersCommand.hasSucceeded()) {
                po.addLog("Workers configured successfully\nStarting cluster...");

                Command startNodesCommand = Commands.getStartCommand(config.getAllNodes());
                final AtomicInteger okCount = new AtomicInteger();
                manager.getCommandRunner().runCommand(startNodesCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains("Started") && okCount.incrementAndGet() == config.getAllNodes().size()) {
                            stop();
                        }
                    }

                });

                if (okCount.get() == config.getAllNodes().size()) {
                    po.addLog("Cluster started successfully");
                } else {
                    po.addLog(String.format("Start of cluster failed, %s, skipping...", startNodesCommand.getAllErrors()));
                }

                po.addLog("Updating db...");
                //update db
                if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                    po.addLogDone("Cluster info updated in DB\nDone");
                } else {
                    po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                }
            } else {
                po.addLogFailed(String.format("Failed to configure workers, %s\nOperation aborted", configureWorkersCommand.getAllErrors()));
            }

        } else {
            po.addLogFailed(String.format("Failed to stop all nodes, %s", stopNodesCommand.getAllErrors()));
        }
    }
}
