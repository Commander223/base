/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.mongodb.common;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.AgentRequestBuilder;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    // INSTALLATION COMMANDS ===================================================
    //    public static Command getInstallCommand( Set<Agent> agents ) {
    //        return createCommand( "Install Mongo",
    //                new RequestBuilder( "sleep 10 ; /usr/bin/apt-get --force-yes --assume-yes install ksks-mongo" )
    //                        .withTimeout( 360 ), agents );
    //    }


    public static Command getSetReplicaSetNameCommand( String replicaSetName, Set<Agent> agents ) {
        return createCommand( "Set replica set name", new RequestBuilder(
                String.format( "/bin/sed -i 's/# replSet = setname/replSet = %s/1' '%s'", replicaSetName,
                        Constants.DATA_NODE_CONF_FILE ) ).withTimeout( 30 ), agents );
    }


    public static Command getAddIpHostToEtcHostsCommand( String domainName, Set<Agent> agents ) {
        Set<AgentRequestBuilder> requestBuilders = new HashSet<>();

        for ( Agent agent : agents ) {
            StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
            StringBuilder appendHosts = new StringBuilder();
            for ( Agent otherAgent : agents ) {
                if ( agent != otherAgent ) {
                    String ip = Util.getAgentIpByMask( otherAgent, Common.IP_MASK );
                    String hostname = otherAgent.getHostname();
                    cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
                    appendHosts.append( "/bin/echo '" ).
                            append( ip ).append( " " ).
                                       append( hostname ).append( "." ).append( domainName ).
                                       append( " " ).append( hostname ).
                                       append( "' >> '/etc/hosts'; " );
                }
            }
            if ( cleanHosts.length() > 0 ) {
                //drop pipe | symbol
                cleanHosts.setLength( cleanHosts.length() - 1 );
                cleanHosts.insert( 0, "egrep -v '" );
                cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
                appendHosts.insert( 0, cleanHosts );
            }

            appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( agent.getHostname() )
                       .append( "' >> '/etc/hosts';" );

            requestBuilders.add( ( AgentRequestBuilder ) new AgentRequestBuilder( agent, appendHosts.toString() )
                    .withTimeout( 30 ) );
        }

        return createCommand( "Add ip-host pair to /etc/hosts", requestBuilders );
    }


    public static Command getRegisterSecondaryNodeWithPrimaryCommand( Agent secondaryNodeAgent, int dataNodePort,
                                                                      String domainName, Agent primaryNodeAgent ) {

        return createCommand( "Register node with replica", new RequestBuilder(
                String.format( "mongo --port %s --eval \"%s\"", dataNodePort,
                        "rs.add('" + secondaryNodeAgent.getHostname() + "." + domainName + ":" + dataNodePort
                                + "');" ) ).withTimeout( 90 ), Util.wrapAgentToSet( primaryNodeAgent ) );
    }


    public static Command getRegisterSecondaryNodesWithPrimaryCommand( Set<Agent> dataNodes, int dataNodePort,
                                                                       String domainName ) {

        StringBuilder secondaryStr = new StringBuilder();
        Iterator<Agent> it = dataNodes.iterator();
        Agent primaryNodeAgent = it.next();
        while ( it.hasNext() ) {
            Agent secondaryNodeAgent = it.next();
            secondaryStr.append( "rs.add('" ).
                    append( secondaryNodeAgent.getHostname() ).append( "." ).append( domainName ).
                                append( ":" ).append( dataNodePort ).append( "');" );
        }

        return createCommand( "Initiate replica set", new RequestBuilder( String.format(
                        "mongo --port %s --eval \"rs.initiate();\" ; sleep 30 ; mongo --port %s --eval \"%s\"",
                        dataNodePort, dataNodePort, secondaryStr.toString() ) ).withTimeout( 180 ),
                Util.wrapAgentToSet( primaryNodeAgent ) );
    }


    public static Command getUnregisterSecondaryNodeFromPrimaryCommand( Agent primaryNodeAgent, int dataNodePort,
                                                                        Agent removeNode, String domainName ) {
        return createCommand( "Unregister node from replica", new RequestBuilder(
                        String.format( "mongo --port %s --eval \"rs.remove('%s.%s:%s');\"", dataNodePort,
                                removeNode.getHostname(), domainName, dataNodePort ) ).withTimeout( 30 ),
                Util.wrapAgentToSet( primaryNodeAgent ) );
    }


    public static Command getRegisterReplicaWithRouterCommand( Set<Agent> dataNodes, int routerPort, int dataNodePort,
                                                               String domainName, String replicaSetName,
                                                               Agent router ) {
        StringBuilder shard = new StringBuilder();
        for ( Agent agent : dataNodes ) {
            shard.append( "sh.addShard('" ).append( replicaSetName ).
                    append( "/" ).append( agent.getHostname() ).append( "." ).append( domainName ).
                         append( ":" ).append( dataNodePort ).append( "');" );
        }

        return createCommand( "Register replica with router", new RequestBuilder(
                String.format( "sleep 30 ; mongo --port %s --eval \"%s\"", routerPort, shard.toString() ) )
                .withTimeout( 120 ), Util.wrapAgentToSet( router ) );
    }


    // LIFECYCLE COMMANDS =======================================================
    public static Command getStartConfigServerCommand( int cfgSrvPort, Set<Agent> configServers ) {
        return createCommand( "Start config server(s)", new RequestBuilder( String.format(
                "/bin/mkdir -p %s ; mongod --configsvr --dbpath %s --port %s --fork --logpath %s/mongodb.log",
                Constants.CONFIG_DIR, Constants.CONFIG_DIR, cfgSrvPort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC ), configServers );
    }


    public static Command getStartRouterCommand( int routerPort, int cfgSrvPort, String domainName,
                                                 Set<Agent> configServers, Set<Agent> routers ) {

        StringBuilder configServersArg = new StringBuilder();
        for ( Agent agent : configServers ) {
            configServersArg.append( agent.getHostname() ).append( "." ).append( domainName ).
                    append( ":" ).append( cfgSrvPort ).append( "," );
        }
        //drop comma
        if ( configServersArg.length() > 0 ) {
            configServersArg.setLength( configServersArg.length() - 1 );
        }

        return createCommand( "Start router(s)", new RequestBuilder(
                String.format( "mongos --configdb %s --port %s --fork --logpath %s/mongodb.log",
                        configServersArg.toString(), routerPort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_ROUTER_TIMEOUT_SEC ), routers );
    }


    public static Command getStartDataNodeCommand( int dataNodePort, Set<Agent> dataNodes ) {
        return createCommand( "Start data node(s)", new RequestBuilder(
                String.format( "mongod --config %s --port %s --fork --logpath %s/mongodb.log",
                        Constants.DATA_NODE_CONF_FILE, dataNodePort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_DATE_NODE_TIMEOUT_SEC ), dataNodes );
    }


    public static Command getCheckInstanceRunningCommand( Agent node, String domainName, int port ) {
        return createCommand( "Check node(s)", new RequestBuilder(
                String.format( "mongo --host %s.%s --port %s", node.getHostname(), domainName, port ) )
                .withTimeout( Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC ), Util.wrapAgentToSet( node ) );
    }


    public static Command getStopNodeCommand( Set<Agent> nodes ) {
        return createCommand( "Stop node(s)",
                new RequestBuilder( "/usr/bin/pkill -2 mongo" ).withTimeout( Timeouts.STOP_NODE_TIMEOUT_SEC ), nodes );
    }


    public static Command getFindPrimaryNodeCommand( Agent secondaryNode, int dataNodePort ) {
        return createCommand( "Find primary node",
                new RequestBuilder( String.format( "/bin/echo 'db.isMaster()' | mongo --port %s", dataNodePort ) )
                        .withTimeout( 30 ), Util.wrapAgentToSet( secondaryNode ) );
    }


    public static List<Command> getInstallationCommands( MongoClusterConfig config ) {
        List<Command> commands = new ArrayList<Command>();

        //        commands.add( getInstallCommand( config.getAllNodes() ) );
        //
        //        commands.add( getStopNodeCommand( config.getAllNodes() ) );

        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), config.getAllNodes() ) );

        commands.add( getSetReplicaSetNameCommand( config.getReplicaSetName(), config.getDataNodes() ) );

        Command startConfigServersCommand =
                getStartConfigServerCommand( config.getCfgSrvPort(), config.getConfigServers() );
        startConfigServersCommand.setData( CommandType.START_CONFIG_SERVERS );
        commands.add( startConfigServersCommand );

        Command startRoutersCommand =
                getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(), config.getDomainName(),
                        config.getConfigServers(), config.getRouterServers() );
        startRoutersCommand.setData( CommandType.START_ROUTERS );
        commands.add( startRoutersCommand );

        Command startDataNodesCommand = getStartDataNodeCommand( config.getDataNodePort(), config.getDataNodes() );
        startDataNodesCommand.setData( CommandType.START_DATA_NODES );
        commands.add( startDataNodesCommand );

        commands.add( getRegisterSecondaryNodesWithPrimaryCommand( config.getDataNodes(), config.getDataNodePort(),
                config.getDomainName() ) );

        commands.add( getRegisterReplicaWithRouterCommand( config.getDataNodes(), config.getRouterPort(),
                config.getDataNodePort(), config.getDomainName(), config.getReplicaSetName(),
                config.getRouterServers().iterator().next() ) );

        return commands;
    }


    public static List<Command> getAddRouterCommands( MongoClusterConfig config, Agent newRouterAgent ) {

        List<Command> commands = new ArrayList<Command>();

        Set<Agent> clusterMembers = new HashSet<Agent>( config.getAllNodes() );
        clusterMembers.add( newRouterAgent );

        //        commands.add( getInstallCommand( Util.wrapAgentToSet( newRouterAgent ) ) );

        //        commands.add( getStopNodeCommand( Util.wrapAgentToSet( newRouterAgent ) ) );

        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), clusterMembers ) );

        Command startRoutersCommand =
                getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(), config.getDomainName(),
                        config.getConfigServers(), Util.wrapAgentToSet( newRouterAgent ) );

        startRoutersCommand.setData( CommandType.START_ROUTERS );

        commands.add( startRoutersCommand );

        return commands;
    }


    public static List<Command> getAddDataNodeCommands( MongoClusterConfig config, Agent newDataNodeAgent ) {

        List<Command> commands = new ArrayList<Command>();

        Set<Agent> clusterMembers = new HashSet<Agent>( config.getAllNodes() );
        clusterMembers.add( newDataNodeAgent );

        //        commands.add( getInstallCommand( Util.wrapAgentToSet( newDataNodeAgent ) ) );

        //        commands.add( getStopNodeCommand( Util.wrapAgentToSet( newDataNodeAgent ) ) );

        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), clusterMembers ) );

        commands.add(
                getSetReplicaSetNameCommand( config.getReplicaSetName(), Util.wrapAgentToSet( newDataNodeAgent ) ) );

        Command startDataNodesCommand =
                getStartDataNodeCommand( config.getDataNodePort(), Util.wrapAgentToSet( newDataNodeAgent ) );
        startDataNodesCommand.setData( CommandType.START_DATA_NODES );
        commands.add( startDataNodesCommand );

        Command findPrimaryNodeCommand =
                getFindPrimaryNodeCommand( config.getDataNodes().iterator().next(), config.getDataNodePort() );
        findPrimaryNodeCommand.setData( CommandType.FIND_PRIMARY_NODE );
        commands.add( findPrimaryNodeCommand );

        return commands;
    }
}
