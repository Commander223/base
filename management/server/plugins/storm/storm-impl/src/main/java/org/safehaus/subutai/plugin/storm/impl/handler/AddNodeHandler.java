package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.Set;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.impl.StormSetupStrategyDefault;

public class AddNodeHandler extends AbstractHandler {

    public AddNodeHandler(StormImpl manager, String clusterName) {
        super(manager, clusterName);
        this.productOperation = manager.getTracker().createProductOperation(
                StormConfig.PRODUCT_NAME, "Add node to cluster " + clusterName);
    }

    @Override
    public void run() {
        ProductOperation po = productOperation;
        StormConfig config = manager.getCluster(clusterName);
        if(config == null) {
            po.addLogFailed(String.format("Cluster '%s' does not exist", clusterName));
            return;
        }

        Agent agent;
        po.addLog("Creating container for new node...");
        try {
            Set<Agent> set = manager.getContainerManager().clone(
                    StormConfig.TEMPLATE_NAME_WORKER, 1, null);
            if(set == null || set.isEmpty())
                throw new LxcCreateException("returned value is null");
            agent = set.iterator().next();
            po.addLog("Container created. Hostname is " + agent.getHostname());
        } catch(LxcCreateException ex) {
            po.addLogFailed("Failed to create container: " + ex.getMessage());
            return;
        }

        // add node to collection and do configuration
        config.getSupervisors().add(agent);

        try {
            new StormSetupStrategyDefault(manager, config, null, po).configure();
            po.addLog("Node successfully configured");
        } catch(ClusterSetupException ex) {
            String m = "Failed to configure node";
            manager.getLogger().error(m, ex);
            po.addLogFailed(m + ": " + ex.getMessage());
            return;
        }

        try {
            manager.getPluginDao().saveInfo(StormConfig.PRODUCT_NAME,
                    clusterName, config);
            po.addLogDone("Cluster info successfully saved");
        } catch(DBException ex) {
            manager.getLogger().error("Failed to save in db", ex);
            po.addLogFailed("Failed to save cluster info");
        }
    }

}
