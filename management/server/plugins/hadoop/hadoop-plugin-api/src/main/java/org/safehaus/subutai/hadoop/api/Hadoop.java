package org.safehaus.subutai.hadoop.api;

import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import java.util.UUID;

/**
 * Created by daralbaev on 02.04.14.
 */
public interface Hadoop extends ApiBase<Config> {

	public UUID startNameNode(Config config);

	public UUID stopNameNode(Config config);

	public UUID restartNameNode(Config config);

	public UUID statusNameNode(Config config);

	public UUID statusSecondaryNameNode(Config config);

	public UUID statusDataNode(Agent agent);

	public UUID startJobTracker(Config config);

	public UUID stopJobTracker(Config config);

	public UUID restartJobTracker(Config config);

	public UUID statusJobTracker(Config config);

	public UUID statusTaskTracker(Agent agent);

	public UUID addNode(String clusterName);

	public UUID blockDataNode(Config config, Agent agent);

	public UUID blockTaskTracker(Config config, Agent agent);

	public UUID unblockDataNode(Config config, Agent agent);

	public UUID unblockTaskTracker(Config config, Agent agent);

	public ClusterSetupStrategy getClusterSetupStrategy(ProductOperation po, Config config);
}
