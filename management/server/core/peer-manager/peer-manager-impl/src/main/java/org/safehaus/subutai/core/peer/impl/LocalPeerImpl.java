package org.safehaus.subutai.core.peer.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.BindHostException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.api.SubutaiHost;
import org.safehaus.subutai.core.peer.api.SubutaiInitException;
import org.safehaus.subutai.core.peer.impl.command.CommandResultImpl;
import org.safehaus.subutai.core.peer.impl.command.TempResponseConverter;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, ResponseListener
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    private static final String SOURCE_MANAGEMENT_HOST = "MANAGEMENT_HOST";
    private static final String SOURCE_RESOURCE_HOST = "RESOURCE_HOST";
    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60; // 5 min
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    //    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private CommunicationManager communicationManager;
    private PeerDAO peerDAO;
    private ManagementHost managementHost;
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private ConcurrentMap<String, AtomicInteger> sequences;

    private Set<RequestListener> requestListeners;


    public LocalPeerImpl( PeerManager peerManager, AgentManager agentManager, TemplateRegistry templateRegistry,
                          PeerDAO peerDao, CommunicationManager communicationManager, CommandRunner commandRunner,
                          QuotaManager quotaManager, StrategyManager strategyManager,
                          Set<RequestListener> requestListeners )

    {
        this.agentManager = agentManager;
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        //        this.containerManager = containerManager;
        this.templateRegistry = templateRegistry;
        this.peerDAO = peerDao;
        this.communicationManager = communicationManager;
        this.commandRunner = commandRunner;
        this.quotaManager = quotaManager;
        this.requestListeners = requestListeners;
    }


    @Override
    public void init()
    {
        List<ManagementHost> r1 = peerDAO.getInfo( SOURCE_MANAGEMENT_HOST, ManagementHost.class );
        if ( r1.size() > 0 )
        {
            managementHost = r1.get( 0 );
            managementHost.resetHeartbeat();
        }

        resourceHosts = Sets.newHashSet( peerDAO.getInfo( SOURCE_RESOURCE_HOST, ResourceHost.class ) );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            resourceHost.resetHeartbeat();
        }
        communicationManager.addListener( this );
        sequences = new ConcurrentHashMap<>();
    }


    @Override
    public void shutdown()
    {
        communicationManager.removeListener( this );
    }


    @Override
    public UUID getId()
    {
        return peerManager.getLocalPeerInfo().getId();
    }


    @Override
    public String getName()
    {
        return peerManager.getLocalPeerInfo().getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return peerManager.getLocalPeerInfo().getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerManager.getLocalPeerInfo();
    }


    @Override
    public ContainerHost createContainer( final String hostName, final String templateName, final String cloneName,
                                          final UUID envId ) throws PeerException
    {
        ResourceHost resourceHost = getResourceHostByName( hostName );
        ContainerHost containerHost = resourceHost
                .createContainer( getId(), envId, Lists.newArrayList( getTemplate( templateName ) ), cloneName );

        resourceHost.addContainerHost( containerHost );
        peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString(), resourceHost );
        return containerHost;
    }


    //    public ContainerHost createContainer( final ResourceHost resourceHost, final UUID creatorPeerId,
    //                                          final UUID environmentId, final List<Template> templates,
    //                                          final String containerName ) throws PeerException
    //    {
    //        return resourceHost.createContainer( creatorPeerId, environmentId, templates, containerName );
    //    }


    //    @Override
    //    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
    //                                                final List<Template> templates, final int quantity,
    //                                                final String strategyId, final List<Criteria> criteria )
    //            throws ContainerCreateException
    //    {
    //        Set<ContainerHost> result = new HashSet<>();
    //        try
    //        {
    //            for ( Template t : templates )
    //            {
    //                if ( t.isRemote() )
    //                {
    //                    tryToRegister( t );
    //                }
    //            }
    //            String templateName = templates.get( templates.size() - 1 ).getTemplateName();
    //            Set<Agent> agents = containerManager.clone( environmentId, templateName, quantity, strategyId,
    // criteria );
    //
    //
    //            for ( Agent agent : agents )
    //            {
    //                ResourceHost resourceHost = getResourceHostByName( agent.getParentHostName() );
    //                ContainerHost containerHost = new ContainerHost( agent, getId(), environmentId );
    //                containerHost.setParentAgent( resourceHost.getAgent() );
    //                containerHost.setCreatorPeerId( creatorPeerId );
    //                containerHost.setTemplateName( templateName );
    //                containerHost.updateHeartbeat();
    //                resourceHost.addContainerHost( containerHost );
    //                result.add( containerHost );
    //                peerDAO.saveInfo( SOURCE_MANAGEMENT, managementHost.getId().toString(), managementHost );
    //            }
    //        }
    //        catch ( PeerException | RegistryException e )
    //        {
    //            throw new ContainerCreateException( e.toString() );
    //        }
    //        return result;
    //    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria,
                                                String nodeGroupName ) throws PeerException
    {
        Set<ContainerHost> result = new HashSet<>();
        try
        {
            for ( Template t : templates )
            {
                if ( t.isRemote() )
                {
                    tryToRegister( t );
                }
            }
            String templateName = templates.get( templates.size() - 1 ).getTemplateName();


            List<ServerMetric> serverMetricMap = new ArrayList<>();
            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                if ( resourceHost.isConnected() )
                {
                    serverMetricMap.add( resourceHost.getMetric() );
                }
            }
            Map<ServerMetric, Integer> slots;
            try
            {
                slots = strategyManager.getPlacementDistribution( serverMetricMap, quantity, strategyId, criteria );
            }
            catch ( StrategyException e )
            {
                throw new PeerException( e.getMessage() );
            }

            Set<String> existingContainerNames = getContainerNames();

            // clone specified number of instances and store their names
            Map<ResourceHost, Set<String>> cloneNames = new HashMap<>();

            for ( Map.Entry<ServerMetric, Integer> e : slots.entrySet() )
            {
                Set<String> hostCloneNames = new HashSet<>();
                for ( int i = 0; i < e.getValue(); i++ )
                {
                    String newContainerName = nextHostName( templateName, existingContainerNames );
                    hostCloneNames.add( newContainerName );
                }
                ResourceHost resourceHost = getResourceHostByName( e.getKey().getHostname() );
                cloneNames.put( resourceHost, hostCloneNames );
            }

            for ( final Map.Entry<ResourceHost, Set<String>> e : cloneNames.entrySet() )
            {
                ResourceHost rh = e.getKey();
                Set<String> clones = e.getValue();
                ResourceHost resourceHost = getResourceHostByName( rh.getHostname() );
                for ( String cloneName : clones )
                {
                    ContainerHost containerHost =
                            resourceHost.createContainer( creatorPeerId, environmentId, templates, cloneName );
                    containerHost.setNodeGroupName( nodeGroupName );
                    resourceHost.createContainer( creatorPeerId, environmentId, templates, cloneName );
                    resourceHost.addContainerHost( containerHost );
                    result.add( containerHost );
                    peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString(), resourceHost );
                }
            }
        }
        catch ( RegistryException e )
        {
            throw new PeerException( e.toString() );
        }
        return result;
    }


    private String nextHostName( String templateName, Set<String> existingNames )
    {
        AtomicInteger i = sequences.putIfAbsent( templateName, new AtomicInteger() );
        if ( i == null )
        {
            i = sequences.get( templateName );
        }
        while ( true )
        {
            String suffix = String.valueOf( i.incrementAndGet() );
            int prefixLen = MAX_LXC_NAME - suffix.length();
            String name = ( templateName.length() > prefixLen ? templateName.substring( 0, prefixLen ) : templateName )
                    + suffix;
            if ( !existingNames.contains( name ) )
            {
                return name;
            }
        }
    }


    private Set<String> getContainerNames() throws PeerException
    {
        Set<String> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                result.add( containerHost.getHostname() );
            }
        }
        return result;
    }


    private void tryToRegister( final Template template ) throws RegistryException
    {
        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            templateRegistry.registerTemplate( template );
        }
    }


    @Override
    public ContainerHost getContainerHostByName( String hostname )
    {
        ContainerHost result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            result = iterator.next().getContainerHostByName( hostname );
        }
        return result;
    }


    @Override
    public ResourceHost getResourceHostByName( String hostname )
    {
        ResourceHost result = null;
        Iterator iterator = getResourceHosts().iterator();

        while ( result == null && iterator.hasNext() )
        {
            ResourceHost host = ( ResourceHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
    }


    @Override
    public Host bindHost( UUID id ) throws PeerException
    {
        Host result = null;
        ManagementHost managementHost = getManagementHost();
        if ( managementHost != null && managementHost.getId().equals( id ) )
        {
            result = managementHost;
        }
        else
        {
            Iterator<ResourceHost> iterator = getResourceHosts().iterator();
            while ( result == null && iterator.hasNext() )
            {
                ResourceHost rh = iterator.next();
                if ( rh.getId().equals( id ) )
                {
                    result = rh;
                }
                else
                {
                    result = rh.getContainerHostById( id );
                }
            }
        }
        if ( result == null )
        {
            throw new BindHostException( id );
        }
        return result;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.startContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.RUNNING );
            }
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getResourceHostByName( containerHost.getParentHostname() );
        try
        {
            if ( resourceHost.stopContainerHost( containerHost ) )
            {
                containerHost.setState( ContainerState.STOPPED );
            }
        }
        catch ( CommandException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        Host result = bindHost( containerHost.getId() );
        if ( result == null )
        {
            throw new PeerException( "Container Host not found." );
        }

        try
        {
            ResourceHost resourceHost = getResourceHostByName( containerHost.getAgent().getParentHostName() );
            resourceHost.destroyContainerHost( containerHost );
            resourceHost.removeContainerHost( result );
            peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString(), resourceHost );
        }
        catch ( ResourceHostException e )
        {
            throw new PeerException( e.toString() );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public boolean isConnected( final Host ahost ) throws PeerException
    {
        Host host = bindHost( ahost.getId() );
        if ( host instanceof ContainerHost )
        {
            boolean b = checkHeartbeat( host.getLastHeartbeat() );
            return ContainerState.RUNNING.equals( ( ( ContainerHost ) host ).getState() ) && b;
        }
        else
        {
            return checkHeartbeat( host.getLastHeartbeat() );
        }
    }


    @Override
    public String getQuota( ContainerHost host, final QuotaEnum quota ) throws PeerException
    {
        try
        {
            return quotaManager.getQuota( host.getHostname(), quota, host.getParentAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
        }
    }


    @Override
    public void setQuota( ContainerHost host, final QuotaEnum quota, final String value ) throws PeerException
    {
        try
        {
            quotaManager.setQuota( host.getHostname(), quota, value, host.getParentAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
        }
    }


    private boolean checkHeartbeat( long lastHeartbeat )
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) < HOST_INACTIVE_TIME;
    }


    @Override
    public ManagementHost getManagementHost() throws PeerException
    {
        return managementHost;
    }


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        return resourceHosts;
    }


    @Override
    public List<String> getTemplates()
    {
        List<Template> templates = templateRegistry.getAllTemplates();

        List<String> result = new ArrayList<>();
        for ( Template template : templates )
        {
            result.add( template.getTemplateName() );
        }
        return result;
    }


    public void addResourceHost( final ResourceHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Resource host could not be null." );
        }
        resourceHosts.add( host );
    }


    @Override
    public void onResponse( final Response response )
    {
        if ( response == null || response.getType() == null )
        {
            return;
        }

        if ( response.getType().equals( ResponseType.REGISTRATION_REQUEST ) || response.getType().equals(
                ResponseType.HEARTBEAT_RESPONSE ) )
        {
            if ( response.getHostname().equals( "management" ) )
            {
                if ( managementHost == null )
                {
                    managementHost = new ManagementHost( PeerUtils.buildAgent( response ), getId() );
                    managementHost.setParentAgent( NullAgent.getInstance() );
                    try
                    {
                        managementHost.init();
                    }
                    catch ( SubutaiInitException e )
                    {
                        LOG.error( e.toString() );
                    }
                }
                managementHost.updateHeartbeat();
                peerDAO.saveInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId().toString(), managementHost );
                return;
            }

            if ( response.getHostname().startsWith( "py" ) )
            {
                ResourceHost host = getResourceHostByName( response.getHostname() );
                if ( host == null )
                {
                    host = new ResourceHost( PeerUtils.buildAgent( response ), getId() );
                    host.setParentAgent( NullAgent.getInstance() );
                    addResourceHost( host );
                }
                host.updateHeartbeat();
                peerDAO.saveInfo( SOURCE_RESOURCE_HOST, host.getId().toString(), host );
                return;
            }

            try
            {
                SubutaiHost host = ( SubutaiHost ) bindHost( response.getUuid() );
                host.updateHeartbeat();
            }
            catch ( PeerException p )
            {
                LOG.warn( p.toString() );
            }
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host )
            throws PeerException, CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host aHost,
                                  final CommandCallback callback ) throws PeerException, CommandException
    {
        Host host = bindHost( aHost.getId() );
        if ( !host.isConnected() )
        {
            throw new PeerException( "Host disconnected." );
        }
        Agent agent = host.getAgent();
        Command command = commandRunner.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.execute( new org.safehaus.subutai.core.command.api.command.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {
                if ( callback != null )
                {
                    //TODO after migration to command executor pass response without conversion
                    callback.onResponse( TempResponseConverter.convertResponse( response ),
                            new CommandResultImpl( agentResult.getExitCode(), agentResult.getStdOut(),
                                    agentResult.getStdErr(), command.getCommandStatus() ) );
                    if ( callback.isStopped() )
                    {
                        stop();
                    }
                }
            }
        } );

        AgentResult agentResult = command.getResults().get( agent.getUuid() );

        if ( agentResult != null )
        {
            return new CommandResultImpl( agentResult.getExitCode(), agentResult.getStdOut(), agentResult.getStdErr(),
                    command.getCommandStatus() );
        }
        else
        {
            return new CommandResultImpl( null, null, null, CommandStatus.TIMEOUT );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host aHost, final CommandCallback callback )
            throws PeerException, CommandException
    {
        Host host = bindHost( aHost.getId() );
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }
        final Agent agent = host.getAgent();
        Command command = commandRunner.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.executeAsync( new org.safehaus.subutai.core.command.api.command.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {
                if ( callback != null )
                {
                    //TODO after migration to command executor pass response without conversion
                    callback.onResponse( TempResponseConverter.convertResponse( response ),
                            new CommandResultImpl( agentResult.getExitCode(), agentResult.getStdOut(),
                                    agentResult.getStdErr(), command.getCommandStatus() ) );
                    if ( callback.isStopped() )
                    {
                        stop();
                    }
                }
            }
        } );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host )
            throws PeerException, CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    @Override
    public void clean()
    {
        if ( managementHost != null && managementHost.getId() != null )
        {
            peerDAO.deleteInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId().toString() );
            managementHost = null;
        }

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            peerDAO.deleteInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString() );
        }
        resourceHosts.clear();
    }


    @Override
    public Template getTemplate( final String templateName )
    {
        return templateRegistry.getTemplate( templateName );
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return true;
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int timeout,
                                 final Class<V> responseType ) throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, timeout, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int timeout ) throws PeerException
    {
        sendRequestInternal( request, recipient, timeout, null );
    }


    private <T, V> V sendRequestInternal( final T request, final String recipient, final int timeout,
                                          final Class<V> responseType ) throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeout > 0, "Timeout must be greater than 0" );


        for ( RequestListener requestListener : requestListeners )
        {
            if ( recipient.equalsIgnoreCase( requestListener.getRecipient() ) )
            {
                try
                {
                    Object response = requestListener.onRequest( new Payload( request, getId() ) );

                    if ( response != null && responseType != null )
                    {
                        return responseType.cast( response );
                    }
                }
                catch ( Exception e )
                {
                    throw new PeerException( e );
                }
            }
        }

        return null;
    }


    @Override
    public Agent waitForAgent( final String containerName, final int timeout )
    {
        return agentManager.waitForRegistration( containerName, timeout );
    }
}

