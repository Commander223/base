package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.hive.CommandType;
import org.safehaus.subutai.impl.hive.Commands;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.impl.hive.Product;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;

public class RestartHandler extends AbstractHandler {

	private final String hostname;

	public RestartHandler(HiveImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_KEY, "Restart node " + hostname);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster '%s' does not exist",
					clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed(String.format("Node '%s' is not connected", hostname));
			return;
		}

		String s = Commands.make(CommandType.RESTART, Product.HIVE);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s).withTimeout(90),
				new HashSet<>(Arrays.asList(agent)));
		manager.getCommandRunner().runCommand(cmd);

		AgentResult res = cmd.getResults().get(agent.getUuid());
		po.addLog(res.getStdOut());
		po.addLog(res.getStdErr());

		boolean ok = cmd.hasSucceeded();

		// if server node, restart Derby as well
		if (ok && agent.equals(config.getServer())) {

			s = Commands.make(CommandType.RESTART, Product.DERBY);
			cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(s).withTimeout(90),
					new HashSet<>(Arrays.asList(agent)));
			manager.getCommandRunner().runCommand(cmd);

			res = cmd.getResults().get(agent.getUuid());
			po.addLog(res.getStdOut());
			po.addLog(res.getStdErr());

			ok = cmd.hasSucceeded();
		}

		if (ok) po.addLogDone("Done");
		else po.addLogFailed(null);

	}

}
