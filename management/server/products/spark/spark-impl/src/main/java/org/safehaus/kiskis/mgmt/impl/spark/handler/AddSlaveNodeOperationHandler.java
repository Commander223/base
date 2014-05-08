package org.safehaus.kiskis.mgmt.impl.spark.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.spark.Commands;
import org.safehaus.kiskis.mgmt.impl.spark.SparkImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dilshat on 5/7/14.
 */
public class AddSlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public AddSlaveNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node %s to %s", lxcHostname, clusterName));
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

        if (manager.getAgentManager().getAgentByHostname(config.getMasterNode().getHostname()) == null) {
            po.addLogFailed(String.format("Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname()));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (agent == null) {
            po.addLogFailed(String.format("New node %s is not connected\nOperation aborted", lxcHostname));
            return;
        }

        //check if node is in the cluster
        if (config.getSlaveNodes().contains(agent)) {
            po.addLogFailed(String.format("Node %s already belongs to this cluster\nOperation aborted", agent.getHostname()));
            return;
        }

        po.addLog("Checking prerequisites...");

        boolean install = !agent.equals(config.getMasterNode());

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand(Util.wrapAgentToSet(agent));
        manager.getCommandRunner().runCommand(checkInstalledCommand);

        if (!checkInstalledCommand.hasCompleted()) {
            po.addLogFailed("Failed to check presence of installed ksks packages\nOperation aborted");
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get(agent.getUuid());

        if (result.getStdOut().contains("ksks-spark") && install) {
            po.addLogFailed(String.format("Node %s already has Spark installed\nOperation aborted", lxcHostname));
            return;
        } else if (!result.getStdOut().contains("ksks-hadoop")) {
            po.addLogFailed(String.format("Node %s has no Hadoop installation\nOperation aborted", lxcHostname));
            return;
        }

        config.getSlaveNodes().add(agent);
        po.addLog("Updating db...");
        //save to db
        if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLog("Cluster info updated in DB");
            //install spark            

            if (install) {
                po.addLog("Installing Spark...");
                Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(agent));
                manager.getCommandRunner().runCommand(installCommand);

                if (installCommand.hasSucceeded()) {
                    po.addLog("Installation succeeded");
                } else {
                    po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                    return;
                }
            }

            po.addLog("Setting master IP on slave...");
            Command setMasterIPCommand = Commands.getSetMasterIPCommand(config.getMasterNode(), Util.wrapAgentToSet(agent));
            manager.getCommandRunner().runCommand(setMasterIPCommand);

            if (setMasterIPCommand.hasSucceeded()) {
                po.addLog("Master IP successfully set\nRegistering slave with master...");

                Command addSlaveCommand = Commands.getAddSlaveCommand(agent, config.getMasterNode());
                manager.getCommandRunner().runCommand(addSlaveCommand);

                if (addSlaveCommand.hasSucceeded()) {
                    po.addLog("Registration succeeded\nRestarting master...");

                    Command restartMasterCommand = Commands.getRestartMasterCommand(config.getMasterNode());
                    final AtomicBoolean ok = new AtomicBoolean();
                    manager.getCommandRunner().runCommand(restartMasterCommand, new CommandCallback() {

                        @Override
                        public void onResponse(Response response, AgentResult agentResult, Command command) {
                            if (agentResult.getStdOut().contains("starting")) {
                                ok.set(true);
                                stop();
                            }
                        }

                    });

                    if (ok.get()) {
                        po.addLog("Master restarted successfully\nStarting Spark on new node...");

                        Command startSlaveCommand = Commands.getStartSlaveCommand(agent);
                        ok.set(false);
                        manager.getCommandRunner().runCommand(startSlaveCommand, new CommandCallback() {

                            @Override
                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                if (agentResult.getStdOut().contains("starting")) {
                                    ok.set(true);
                                    stop();
                                }
                            }

                        });

                        if (ok.get()) {
                            po.addLogDone("Spark started successfully\nDone");
                        } else {
                            po.addLogFailed(String.format("Failed to start Spark, %s", startSlaveCommand.getAllErrors()));
                        }

                    } else {
                        po.addLogFailed(String.format("Master restart failed, %s", restartMasterCommand.getAllErrors()));
                    }

                } else {
                    po.addLogFailed(String.format("Registration failed, %s", addSlaveCommand.getAllErrors()));
                }
            } else {
                po.addLogFailed(String.format("Failed to set master IP, %s", setMasterIPCommand.getAllErrors()));
            }

        } else {
            po.addLogFailed("Could not update cluster info in DB! Please see logs\nOperation aborted");
        }
    }
}
