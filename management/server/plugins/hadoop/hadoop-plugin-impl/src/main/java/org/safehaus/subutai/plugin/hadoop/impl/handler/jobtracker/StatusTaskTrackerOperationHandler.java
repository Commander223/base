package org.safehaus.subutai.plugin.hadoop.impl.handler.jobtracker;


import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;


public class StatusTaskTrackerOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    private String lxcHostName;


    public StatusTaskTrackerOperationHandler( HadoopImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostName = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Checking TaskTracker in %s", clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( hadoopClusterConfig.getNameNode() == null ) {
            productOperation.addLogFailed( String.format( "TaskTracker on %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostName );
        if ( node == null ) {
            productOperation.addLogFailed( "TaskTracker is not connected" );
            return;
        }
        Command statusCommand = Commands.getJobTrackerCommand( node, "status" );
        manager.getCommandRunner().runCommand( statusCommand );

        AgentResult result = statusCommand.getResults().get( node.getUuid() );

        NodeState nodeState = NodeState.UNKNOWN;
        if ( statusCommand.hasCompleted() ) {
            if ( result.getStdOut() != null && result.getStdOut().contains( "TaskTracker" ) ) {
                String[] array = result.getStdOut().split( "\n" );

                for ( String status : array ) {
                    if ( status.contains( "TaskTracker" ) ) {
                        String temp = status.replaceAll( "TaskTracker is ", "" );
                        if ( temp.toLowerCase().contains( "not" ) ) {
                            nodeState = NodeState.STOPPED;
                        }
                        else {
                            nodeState = NodeState.RUNNING;
                        }
                    }
                }
            }
        }

        if ( NodeState.UNKNOWN.equals( nodeState ) ) {
            productOperation.addLogFailed( String.format( "Failed to check status of %s", node.getHostname() ) );
        }
        else {
            productOperation.addLogDone( String.format( "TaskTracker of %s is %s", node.getHostname(), nodeState ) );
        }
    }
}
