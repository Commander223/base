package org.safehaus.kiskis.mgmt.impl.lucene.handler;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.impl.lucene.Commands;
import org.safehaus.kiskis.mgmt.impl.lucene.LuceneImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.Agent;


public class UninstallOperationHandler extends AbstractOperationHandler<LuceneImpl> {


    public UninstallOperationHandler( LuceneImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getNodes() ) {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                productOperation.addLogFailed(
                        String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        productOperation.addLog( "Uninstalling Lucene..." );
        Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            for ( AgentResult result : uninstallCommand.getResults().values() ) {
                Agent agent = manager.getAgentManager().getAgentByUUID( result.getAgentUUID() );

                if ( result.getExitCode() != null && result.getExitCode() == 0 ) {
                    if ( result.getStdOut().contains( "Package ksks-lucene is not installed, so not removed" ) ) {
                        productOperation.addLog( String.format( "Lucene is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                    else {
                        productOperation.addLog( String.format( "Lucene is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                }
                else {
                    productOperation.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                }
            }

            productOperation.addLog( "Updating db..." );

            if ( manager.getDbManager().deleteInfo( Config.PRODUCT_KEY, config.getClusterName() ) ) {
                productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            else {
                productOperation.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
        }
        else {
            productOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
