package org.safehaus.subutai.impl.lucene.handler.mock;


import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;


public class LuceneImplMock extends LuceneImpl {

    private Config clusterConfig = null;

    public LuceneImplMock() {
        super( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock() );
    }


    public LuceneImplMock setClusterConfig( Config clusterConfig ) {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public Config getCluster( String clusterName ) {
        return clusterConfig;
    }
}
