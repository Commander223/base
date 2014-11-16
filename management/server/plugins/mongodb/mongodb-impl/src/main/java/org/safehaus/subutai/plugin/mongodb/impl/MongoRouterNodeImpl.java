package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;

import com.google.common.base.Preconditions;


public class MongoRouterNodeImpl extends MongoNodeImpl implements MongoRouterNode
{
    Set<MongoConfigNode> configServers;
    int cfgSrvPort;


    public MongoRouterNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, final String domainName,
                                final int port, final int cfgSrvPort )
    {
        super( agent, peerId, environmentId, domainName, port );
        this.cfgSrvPort = cfgSrvPort;
    }


    @Override
    public void setConfigServers( Set<MongoConfigNode> configServers )
    {
        this.configServers = configServers;
    }


    @Override
    public void start() throws MongoException
    {
        Preconditions.checkNotNull( configServers, "Config servers is null" );
        CommandDef commandDef = Commands.getStartRouterCommandLine( port, cfgSrvPort, domainName, configServers );
        try
        {
            final AtomicBoolean commandOk = new AtomicBoolean();
            execute( commandDef.build(), new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    if ( response.getStdOut().contains( "child process started successfully, parent exiting" ) )
                    {
                        commandOk.set( true );
                        stop();
                    }
                }
            } );

            if ( !commandOk.get() )
            {
                throw new CommandException( "Could not start mongo router instance." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not start mongo router node:" );
        }
    }


    @Override
    public void registerDataNodesWithReplica( final Set<MongoDataNode> dataNodes, final String replicaName )
            throws MongoException
    {
        CommandDef cmd = Commands.getRegisterReplicaWithRouterCommandLine( this, dataNodes, replicaName );
        try
        {
            CommandResult commandResult = execute( cmd.build() );
            if ( !commandResult.hasSucceeded() )
            {
                throw new MongoException( "Could not register data nodes." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not register data nodes." );
        }
    }
}
