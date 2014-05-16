package org.safehaus.kiskis.mgmt.impl.accumulo.handler;


import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.accumulo.AccumuloImpl;
import org.safehaus.kiskis.mgmt.impl.accumulo.Commands;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;


/**
 * Created by dilshat on 5/6/14.
 */
public class StartClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;


    public StartClusterOperationHandler( AccumuloImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Starting cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null ) {
            po.addLogFailed( String.format( "Master node '%s' is not connected\nOperation aborted",
                    config.getMasterNode().getHostname() ) );
            return;
        }

        po.addLog( "Starting cluster..." );

        Command startCommand = Commands.getStartCommand( config.getMasterNode() );
        manager.getCommandRunner().runCommand( startCommand );

        if ( startCommand.hasSucceeded() ) {
            po.addLogDone( "Cluster started successfully" );
        }
        else {
            po.addLogFailed(
                    String.format( "Failed to start cluster %s, %s", clusterName, startCommand.getAllErrors() ) );
        }
    }
}
