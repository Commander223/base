package org.safehaus.kiskis.mgmt.impl.presto.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.presto.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.presto.Commands;
import org.safehaus.kiskis.mgmt.impl.presto.PrestoImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class DestroyWorkerNodeOperationHandler extends AbstractOperationHandler<PrestoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public DestroyWorkerNodeOperationHandler(PrestoImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (agent == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        if (config.getWorkers().size() == 1) {
            po.addLogFailed("This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted");
            return;
        }

        //check if node is in the cluster
        if (!config.getWorkers().contains(agent)) {
            po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", agent.getHostname()));
            return;
        }

        po.addLog("Uninstalling Presto...");

        Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
        manager.getCommandRunner().runCommand(uninstallCommand);

        if (uninstallCommand.hasCompleted()) {
            AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
            if (result.getExitCode() != null && result.getExitCode() == 0) {
                if (result.getStdOut().contains("Package ksks-presto is not installed, so not removed")) {
                    po.addLog(String.format("Presto is not installed, so not removed on node %s",
                            agent.getHostname()));
                } else {
                    po.addLog(String.format("Presto is removed from node %s",
                            agent.getHostname()));
                }
            } else {
                po.addLog(String.format("Error %s on node %s", result.getStdErr(),
                        agent.getHostname()));
            }

        } else {
            po.addLogFailed(String.format("Uninstallation failed, %s", uninstallCommand.getAllErrors()));
            return;
        }

        config.getWorkers().remove(agent);
        po.addLog("Updating db...");

        if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLogDone("Cluster info updated in DB\nDone");
        } else {
            po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
        }
    }
}
