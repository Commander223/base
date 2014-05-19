package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Iterator;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.*;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.*;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class InstallHandler extends AbstractOperationHandler<FlumeImpl> {

    private final Config config;
    private final ProductOperation po;

    public InstallHandler(FlumeImpl manager, Config config) {
        super(manager, config.getClusterName());
        this.config = config;
        this.po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                "Install Flume cluster " + config.getClusterName());
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        if(config == null) {
            po.addLogFailed("Invalid configuration");
            return;
        }
        if(manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed("Cluster already exists: " + clusterName);
            return;
        }

        //check if node agent is connected
        for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
            Agent node = it.next();
            if(manager.getAgentManager().getAgentByHostname(node.getHostname()) != null)
                continue;
            po.addLog(String.format(
                    "Node %s is not connected. Omitting this node from installation",
                    node.getHostname()));
            it.remove();
        }
        if(config.getNodes().isEmpty()) {
            po.addLogFailed("No nodes eligible for installation. Operation aborted");
            return;
        }

        po.addLog("Checking prerequisites...");
        //check installed ksks packages
        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.STATUS)),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);
        if(!cmd.hasSucceeded()) {
            po.addLogFailed("Failed to check installed packages");
            return;
        }

        for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
            Agent node = it.next();
            AgentResult result = cmd.getResults().get(node.getUuid());

            if(result.getStdOut().contains("ksks-flume")) {
                po.addLog(String.format(
                        "Node %s already has Flume installed. Omitting this node from installation",
                        node.getHostname()));
                it.remove();
            } else if(!result.getStdOut().contains("ksks-hadoop")) {
                po.addLog(String.format(
                        "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname()));
                it.remove();
            }
        }

        if(config.getNodes().isEmpty()) {
            po.addLogFailed("No nodes eligible for installation. Operation aborted");
            return;
        }

        po.addLog("Updating db...");
        boolean b = manager.getDbManager().saveInfo(Config.PRODUCT_KEY,
                config.getClusterName(), config);
        if(!b) {
            po.addLogFailed("Failed to save cluster info!");
            return;
        }
        po.addLog("Cluster info successfully saved\nInstalling Flume...");

        String s = Commands.make(CommandType.INSTALL);
        cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(s).withTimeout(90),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasSucceeded())
            po.addLogDone("Installation succeeded\nDone");
        else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Installation failed");
        }

    }

}
