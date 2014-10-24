package org.safehaus.subutai.plugin.solr.impl.handler.mock;


import javax.sql.DataSource;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.Commands;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;

import static org.mockito.Mockito.mock;


public class SolrImplMock extends SolrImpl
{

    private SolrClusterConfig clusterSolrClusterConfig = null;


    public SolrImplMock()
    {
        super( mock( DataSource.class ) );
        setCommandRunner( mock( CommandRunner.class ) );
        setAgentManager( mock( AgentManager.class ) );
        setTracker( new TrackerMock() );
    }


    public SolrImplMock setCommands( Commands commands )
    {
        this.commands = commands;
        return this;
    }


    public SolrImplMock setClusterSolrClusterConfig( SolrClusterConfig clusterSolrClusterConfig )
    {
        this.clusterSolrClusterConfig = clusterSolrClusterConfig;
        return this;
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName )
    {
        return clusterSolrClusterConfig;
    }
}
