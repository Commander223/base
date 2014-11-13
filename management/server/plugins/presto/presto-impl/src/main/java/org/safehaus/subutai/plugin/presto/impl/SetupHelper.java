package org.safehaus.subutai.plugin.presto.impl;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;

import com.google.common.base.Preconditions;


public class SetupHelper
{

    final TrackerOperation po;
    final PrestoImpl manager;
    final PrestoClusterConfig config;


    public SetupHelper( TrackerOperation po, PrestoImpl manager, PrestoClusterConfig config )
    {

        Preconditions.checkNotNull( config, "Presto cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( manager, "Presto manager is null" );

        this.po = po;
        this.manager = manager;
        this.config = config;
    }


    void checkConnected() throws ClusterSetupException
    {

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        if ( getCoordinatorHost().getAgent() == null )
        {
            throw new ClusterSetupException( "Coordinator node is not connected" );
        }

        for ( ContainerHost host : environment.getHostsByIds( config.getWorkers() ) )
        {
            if ( host.getAgent() == null )
            {
                throw new ClusterSetupException( "Not all worker nodes are connected" );
            }
        }
    }


    public void configureAsCoordinator( ContainerHost host ) throws ClusterSetupException, CommandException
    {
        po.addLog( "Configuring coordinator..." );

          CommandResult result = host.execute( manager.getCommands().getSetCoordinatorCommand( getCoordinatorHost()) );
          processResult( host, result );

    }


    public void configureAsWorker(Set<ContainerHost> workerHosts) throws ClusterSetupException
    {
        po.addLog( "Configuring worker(s)..." );

        for( ContainerHost host : workerHosts )
        {
            CommandResult result = null;
            try
            {
                result = host.execute( manager.getCommands().getSetCoordinatorCommand( host ) );
                processResult( host, result );
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException(
                        String.format( "Failed to configure workers Presto node(s): %s", result.getStdErr() ) );
            }

        }
    }


    public void startNodes( final Set<ContainerHost> set ) throws ClusterSetupException
    {
        po.addLog( "Starting Presto node(s)..." );
        for(ContainerHost host : set)
        {
            CommandResult result = null;
            try
            {
                result = host.execute( manager.getCommands().getStartCommand() );
                processResult( host, result );
            }
            catch ( CommandException e )
            {
                throw new ClusterSetupException(
                        String.format( "Failed to start Presto node(s): %s", result.getStdErr() ) );
            }
            po.addLogDone( "Presto node(s) started successfully\nDone" );
        }
    }
    public void processResult( ContainerHost host, CommandResult result ) throws ClusterSetupException
    {

        if ( !result.hasSucceeded() )
        {
            throw new ClusterSetupException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
    }
    public ContainerHost getCoordinatorHost()
    {
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost host = environment.getContainerHostByUUID( config.getCoordinatorNode() );
        return host;
    }
}
