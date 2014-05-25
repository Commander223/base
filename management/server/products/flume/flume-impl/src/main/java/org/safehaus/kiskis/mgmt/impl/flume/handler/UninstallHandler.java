package org.safehaus.kiskis.mgmt.impl.flume.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.CommandType;
import org.safehaus.kiskis.mgmt.impl.flume.Commands;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

public class UninstallHandler extends AbstractOperationHandler<FlumeImpl> {

    public UninstallHandler(FlumeImpl manager, String clusterName) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                Config.PRODUCT_KEY, "Destroy cluster " + clusterName);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        Config config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed("Cluster does not exist: " + clusterName);
            return;
        }

        po.addLog("Uninstalling Flume...");

        Command cmd = manager.getCommandRunner().createCommand(
                new RequestBuilder(Commands.make(CommandType.PURGE)),
                config.getNodes());
        manager.getCommandRunner().runCommand(cmd);

        if(cmd.hasCompleted()) {
            for(Agent agent : config.getNodes()) {
                AgentResult result = cmd.getResults().get(agent.getUuid());
                if(result.getExitCode() != null && result.getExitCode() == 0)
                    if(result.getStdOut().contains("Flume is not installed"))
                        po.addLog("Flume not installed on " + agent.getHostname());
                    else
                        po.addLog(String.format("Flume removed from node %s",
                                agent.getHostname()));
                else
                    po.addLog(String.format("Error on node %s: %s",
                            agent.getHostname(), result.getStdErr()));
            }

            po.addLog("Updating db...");
            if(manager.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName()))
                po.addLogDone("Cluster info deleted from DB\nDone");
            else
                po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
        } else {
            po.addLog(cmd.getAllErrors());
            po.addLogFailed("Uninstallation failed");
        }
    }

}
