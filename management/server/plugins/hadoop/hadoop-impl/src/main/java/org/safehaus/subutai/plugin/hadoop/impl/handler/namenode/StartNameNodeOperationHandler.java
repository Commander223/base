package org.safehaus.subutai.plugin.hadoop.impl.handler.namenode;


import java.util.Iterator;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


public class StartNameNodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public StartNameNodeOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Starting NameNode in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );
        Commands commands = new Commands( hadoopClusterConfig );

        if ( hadoopClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        if ( hadoopClusterConfig.getNameNode() == null )
        {
            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return;
        }

        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getAgent().getUuid().equals( hadoopClusterConfig.getNameNode().getAgent().getUuid() ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", host.getId() ) );
            return;
        }

        try
        {
            CommandResult result = host.execute( new RequestBuilder( commands.getStartNameNodeCommand() ) );
            logStatusResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }
    }


    private void logStatusResults( TrackerOperation po, CommandResult result )
    {
        NodeState nodeState = NodeState.UNKNOWN;


        if ( result.getStdOut().contains( "NameNode" ) )
        {
            String[] array = result.getStdOut().split( "\n" );

            for ( String status : array )
            {
                if ( status.contains( "NameNode" ) )
                {
                    String temp = status.replaceAll(
                            Pattern.quote( "!(SecondaryNameNode is not running on this " + "machine)" ), "" ).
                                                replaceAll( "NameNode is ", "" );
                    if ( temp.toLowerCase().contains( "not" ) )
                    {
                        nodeState = NodeState.STOPPED;
                    }
                    else
                    {
                        nodeState = NodeState.RUNNING;
                    }
                }
            }
        }

        if ( NodeState.RUNNING.equals( nodeState ) )
        {
            trackerOperation.addLogDone( String.format( "NameNode on started" ) );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Failed to start NameNode)" ) );
        }
    }
}
