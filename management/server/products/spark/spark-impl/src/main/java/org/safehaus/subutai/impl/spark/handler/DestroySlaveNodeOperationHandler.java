package org.safehaus.subutai.impl.spark.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.impl.spark.Commands;
import org.safehaus.subutai.impl.spark.SparkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dilshat on 5/7/14.
 */
public class DestroySlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
	private final ProductOperation po;
	private final String lxcHostname;

	public DestroySlaveNodeOperationHandler(SparkImpl manager, String clusterName, String lxcHostname) {
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
		final Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
		if (agent == null) {
			po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
			return;
		}

		if (config.getSlaveNodes().size() == 1) {
			po.addLogFailed("This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted");
			return;
		}

		//check if node is in the cluster
		if (!config.getSlaveNodes().contains(agent)) {
			po.addLogFailed(String.format("Node %s does not belong to this cluster\nOperation aborted", agent.getHostname()));
			return;
		}

		po.addLog("Unregistering slave from master...");

		if (manager.getAgentManager().getAgentByHostname(config.getMasterNode().getHostname()) != null) {

			Command clearSlavesCommand = Commands.getClearSlaveCommand(agent, config.getMasterNode());
			manager.getCommandRunner().runCommand(clearSlavesCommand);

			if (clearSlavesCommand.hasSucceeded()) {
				po.addLog("Successfully unregistered slave from master\nRestarting master...");

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
					po.addLog("Master restarted successfully");
				} else {
					po.addLog(String.format("Master restart failed, %s, skipping...", restartMasterCommand.getAllErrors()));
				}
			} else {
				po.addLog(String.format("Failed to unregister slave from master: %s, skipping...",
						clearSlavesCommand.getAllErrors()));
			}
		} else {
			po.addLog("Failed to unregister slave from master: Master is not connected, skipping...");
		}

		boolean uninstall = !agent.equals(config.getMasterNode());

		if (uninstall) {
			po.addLog("Uninstalling Spark...");

			Command uninstallCommand = Commands.getUninstallCommand(Util.wrapAgentToSet(agent));
			manager.getCommandRunner().runCommand(uninstallCommand);

			if (uninstallCommand.hasCompleted()) {
				AgentResult result = uninstallCommand.getResults().get(agent.getUuid());
				if (result.getExitCode() != null && result.getExitCode() == 0) {
					if (result.getStdOut().contains("Package ksks-spark is not installed, so not removed")) {
						po.addLog(String.format("Spark is not installed, so not removed on node %s",
								agent.getHostname()));
					} else {
						po.addLog(String.format("Spark is removed from node %s",
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
		} else {
			po.addLog("Stopping slave...");

			Command stopSlaveCommand = Commands.getStopSlaveCommand(agent);
			manager.getCommandRunner().runCommand(stopSlaveCommand);

			if (stopSlaveCommand.hasSucceeded()) {
				po.addLog("Slave stopped successfully");
			} else {
				po.addLog(String.format("Failed to stop slave, %s, skipping...", stopSlaveCommand.getAllErrors()));
			}
		}

		config.getSlaveNodes().remove(agent);
		po.addLog("Updating db...");

		if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
			po.addLogDone("Cluster info updated in DB\nDone");
		} else {
			po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
		}
	}
}
