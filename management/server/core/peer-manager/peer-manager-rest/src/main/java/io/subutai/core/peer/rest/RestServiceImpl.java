package io.subutai.core.peer.rest;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private PeerManager peerManager;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected RestUtil restUtil = new RestUtil();


    public RestServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public Response getLocalPeerInfo()
    {
        try
        {
            PeerInfo selfInfo = peerManager.getLocalPeerInfo();
            return Response.ok( jsonUtil.to( selfInfo ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating local peer info #getLocalPeerInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerManager.getLocalPeer().getPeerInfo();
    }


    @Override
    public List<PeerInfo> getRegisteredPeers()
    {
        return peerManager.getPeerInfos();
    }


    @Override
    public Response getPeerPolicy( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( peerId );
            if ( peerPolicy == null )
            {
                return Response.ok().build();
            }
            else
            {
                return Response.ok( jsonUtil.to( peerPolicy ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting peer policy #getPeerPolicy", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( String peer )
    {
        try
        {
            PeerInfo p = jsonUtil.from( peer, PeerInfo.class );
            p.setIp( getRequestIp() );
            p.setName( String.format( "Peer %s", p.getId() ) );
            peerManager.update( p );

            return Response.ok( jsonUtil.to( p ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating peer #updatePeer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    protected String getRequestIp()
    {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getRemoteAddr();
    }


    //*************************************************************************************
    @Override
    public void destroyContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.destroyContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error destroying container #destroyContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void startContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.startContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error starting container #startContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void stopContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.stopContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error stopping container #stopContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        return peerManager.getContainerState( containerId );
    }


    @Override
    public Response getTemplate( final String templateName )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Template result = localPeer.getTemplate( templateName );
            return Response.ok( jsonUtil.to( result ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting template #getTemplate", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    //*********** Quota functions ***************


    @Override
    public Response getAvailableRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available ram quota #getAvailableRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available cpu quota #getAvailableCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getAvailableDiskQuota(
                    jsonUtil.<DiskPartition>from( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available disk quota #getAvailableDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( ContainerId containerId, int pid )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( pid > 0 );

            return peerManager.getProcessResourceUsage( containerId, pid );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting processing resource usage #getProcessResourceUsage", e );
            throw new WebApplicationException(
                    Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build() );
        }
    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota #getRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota info #getRamQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final String ramQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setRamQuota( jsonUtil.<RamQuota>from( ramQuota, new TypeToken<RamQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota #getCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota info #getCpuQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu quota #setCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuSet( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getCpuSet() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu set #getCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final String containerId, final String cpuSet )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setCpuSet( jsonUtil.<Set<Integer>>from( cpuSet, new TypeToken<Set<Integer>>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu set #setCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getDiskQuota(
                    JsonUtil.<DiskPartition>fromJson( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting disk quota #getDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setDiskQuota( jsonUtil.<DiskQuota>from( diskQuota, new TypeToken<DiskQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public void setDefaultGateway( final ContainerGateway gateway )
    {
        Preconditions.checkNotNull( gateway, "Gateway is null" );

        try
        {
            peerManager.setDefaultGateway( gateway );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting default gateway #setDefaultGateway", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response getContainerHostInfoById( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();


            return Response.ok( jsonUtil.to( localPeer.getContainerHostInfoById( containerId ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container host info by id #getContainerHostInfoById", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Collection<Vni> getReservedVnis()
    {
        try
        {
            return peerManager.getReservedVnis();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting reserved vnis #getReservedVnis", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Collection<Gateway> getGateways()
    {
        try
        {
            return peerManager.getGateways();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting gateways #getGateways", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response createGateway( final String gatewayIp, final int vlan )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.createGateway( gatewayIp, vlan );

            return Response.ok().status( Response.Status.CREATED ).build();
        }
        catch ( Exception ex )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( ex ).build();
        }
    }


    @Override
    public PublicKeyContainer createEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        try
        {
            return peerManager.createEnvironmentKeyPair( environmentId );
        }
        catch ( Exception ex )
        {
            throw new WebApplicationException( ex );
        }
    }


    @Override
    public void removeEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        try
        {
            peerManager.removeEnvironmentKeyPair( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Vni reserveVni( final Vni vni )
    {
        try
        {
            return peerManager.reserveVni( vni );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error reserving vni #reserveVni", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response setupTunnels( final String peerIps, final String environmentId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            int vlan = localPeer
                    .setupTunnels( jsonUtil.<Map<String, String>>from( peerIps, new TypeToken<Map<String, String>>()
                    {}.getType() ), environmentId );

            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting up tunnels #setupTunnels", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public HostInterfaces getNetworkInterfaces()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return localPeer.getInterfaces();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public ResourceHostMetrics getResources()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return localPeer.getResourceHostMetrics();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void addToTunnel( final N2NConfig config )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        try
        {
            localPeer.setupN2NConnection( config );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void removeN2NConnection( final EnvironmentId environmentId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.removeN2NConnection( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void cleanupNetwork( final EnvironmentId environmentId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.cleanupEnvironmentNetworkSettings( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    //    @Override
    //    public Response getRegisteredPeerInfo( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            PeerInfo peerInfo = peerManager.getPeer( peerId ).getPeerInfo();
    //            return Response.ok( jsonUtil.to( peerInfo ) ).build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error getting peer info #getRegisteredPeerInfo", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }
    //
    //
    //    @Override
    //    public Response ping()
    //    {
    //        return Response.ok().build();
    //    }


    //    @Override
    //    public Response processRegistrationRequest( PeerInfo remotePeerInfo )
    //    {
    //        try
    //        {
    //            if ( peerManager.getPeerInfo( remotePeerInfo.getId() ) != null )
    //            {
    //                return Response.status( Response.Status.CONFLICT )
    //                               .entity( String.format( "%s already registered", remotePeerInfo.getName() ) )
    // .build();
    //            }
    //            else
    //            {
    //                peerManager.register( remotePeerInfo );
    //
    //                return Response.ok().build();
    //            }
    //        }
    //        catch ( PeerException e )
    //        {
    //            LOGGER.error( "Error processing register request #processRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }
    // @Override
    //    public Response processRegistrationRequest( PeerInfo peerInfo )
    //    {
    //        try
    //        {
    //            // ******* Convert HexString to Byte Array ****** Decrypt data
    //            EncryptionTool encTool = securityManager.getEncryptionTool();
    //            KeyManager keyManager = securityManager.getKeyManager();
    //
    //            byte[] data = HexUtil.hexStringToByteArray( peerInfo.getId() );
    //            data = encTool.decrypt( data );
    //            //************************************************
    //
    //            PeerInfo p = jsonUtil.from( new String( data ), PeerInfo.class );
    //
    //            if ( peerManager.getPeerInfo( p.getId() ) != null )
    //            {
    //                return Response.status( Response.Status.CONFLICT )
    //                               .entity( String.format( "%s already registered", p.getName() ) ).build();
    //            }
    //            else
    //            {
    //                //Encrypt Local Peer
    //                PGPPublicKey pkey = keyManager.getRemoteHostPublicKey( p.getId(), p.getIp() );
    //                PeerInfo localPeer = peerManager.getLocalPeerInfo();
    //
    //                if ( pkey != null )
    //                {
    //                    localPeer.setKeyPhrase( p.getKeyPhrase() );
    //                    String jsonData = jsonUtil.to( localPeer );
    //                    data = encTool.encrypt( jsonData.getBytes(), pkey, false );
    //
    //                    // Save to DB
    //                    p.setStatus( PeerStatus.REQUESTED );
    //                    p.setName( String.format( "Peer %s", p.getId() ) );
    //                    peerManager.register( p );
    //
    //                    return Response.ok( HexUtil.byteArrayToHexString( data ) ).build();
    //                }
    //                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error processing register request #processRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response doRegistrationRequest( final String peerIp )
    //    {
    //        try
    //        {
    //            String baseUrl =
    //                    String.format( "https://%s:%s/rest/v1/peer/register", peerIp, ChannelSettings
    // .SECURE_PORT_X1 );
    //            WebClient client = restUtil.getTrustedWebClient( baseUrl, provider );
    //            client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );
    //            Form form = new Form();
    //            form.set( "peer", jsonUtil.to( peerManager.getLocalPeerInfo() ) );
    //
    //            //TODO: refactor to return POJO
    //            Response response = client.post( peerManager.getLocalPeerInfo() );
    //
    //
    //            //            Response response = client.path( "peer/register" ).form( form );
    //            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
    //            {
    //                return registerPeerCert( response );
    //            }
    //            else if ( response.getStatus() == Response.Status.CONFLICT.getStatusCode() )
    //            {
    //                String reason = response.readEntity( String.class );
    //                LOGGER.warn( reason );
    //                return Response.serverError().entity( reason ).build();
    //            }
    //            else
    //            {
    //                LOGGER.warn( "Response for registering peer: " + response.toString() );
    //                return Response.serverError().build();
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "error sending request", e );
    //            return Response.serverError().entity( e.toString() ).build();
    //        }
    //    }


    //    protected Response registerPeerCert( final Response response )
    //    {
    //        try
    //        {
    //            String responseString = response.readEntity( String.class );
    //            LOGGER.info( response.toString() );
    //            PeerInfo remotePeerInfo = jsonUtil.from( responseString, new TypeToken<PeerInfo>()
    //            {}.getType() );
    //            if ( remotePeerInfo != null )
    //            {
    //                remotePeerInfo.setStatus( PeerStatus.REQUEST_SENT );
    //                try
    //                {
    //                    peerManager.register( remotePeerInfo );
    //                }
    //                catch ( PeerException e )
    //                {
    //                    LOGGER.error( "Couldn't register peer", e );
    //                }
    //            }
    //            return Response.ok().build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error registering peer certificate #registerPeerCert", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response unregisterPeer( String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            boolean result = peerManager.unregister( peerId );
    //            if ( result )
    //            {
    //                //************ Delete Trust SSL Cert **************************************
    //                securityManager.getKeyStoreManager().removeCertFromTrusted( ChannelSettings.SECURE_PORT_X2,
    // peerId );
    //                httpContextManager.reloadTrustStore();
    //                //***********************************************************************
    //
    //
    //                return Response.ok( "Successfully unregistered peer: " + peerId ).build();
    //            }
    //            else
    //            {
    //                return Response.status( Response.Status.NOT_FOUND ).build();
    //            }
    //        }
    //        catch ( Exception pe )
    //        {
    //            LOGGER.error( "Error unregistering peer #unregisterPeer", pe );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( pe.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response rejectForRegistrationRequest( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            PeerInfo p = peerManager.getPeerInfo( peerId );
    //            p.setStatus( PeerStatus.REJECTED );
    //            peerManager.update( p );
    //
    //            return Response.noContent().build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error rejecting registration request #rejectForRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }


    //    @Override
    //    public Response removeRegistrationRequest( final String peerId )
    //    {
    //        try
    //        {
    //            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );
    //
    //            UUID id = UUID.fromString( peerId );
    //            peerManager.unregister( id.toString() );
    //            return Response.status( Response.Status.NO_CONTENT ).build();
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error removing registration request #removeRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    //        }
    //    }


    /* *************************************************************
     * Get Public key and save it in the local KeyServer
     *
     * TODO remove this method if not used
     */
    //    private String getRemotePeerPublicKey( String peerId, final String ip )
    //    {
    //        String baseUrl = String.format( "https://%s:%s/cxf", ip, ChannelSettings.SECURE_PORT_X1 );
    //        WebClient client = RestUtil.createTrustedWebClient( baseUrl, provider );
    //        client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );
    //
    //        try
    //        {
    //            Response response = client.path( "security/keyman/getpublickeyring" ).query( "hostid", "" ).get();
    //
    //            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
    //            {
    //                // Get Remote peer Public Key and save in the local keystore
    //                String publicKeyring = response.readEntity( String.class );
    //
    //                securityManager.getKeyManager().savePublicKeyRing( peerId, ( short ) 3, publicKeyring );
    //            }
    //            return peerId;
    //        }
    //        catch ( Exception ex )
    //        {
    //            return "";
    //        }
    //    }


    //    @Override
    //    public Response approveForRegistrationRequest( final String approvedPeer, final String certHEX )
    //    {
    //        try
    //        {
    //            // ******* Convert HexString to Byte Array ****** Decrypt data
    //            EncryptionTool encTool = securityManager.getEncryptionTool();
    //            KeyManager keyManager = securityManager.getKeyManager();
    //
    //            byte data[] = HexUtil.hexStringToByteArray( approvedPeer );
    //            byte cert[] = HexUtil.hexStringToByteArray( certHEX );
    //
    //            data = encTool.decrypt( data );
    //            cert = encTool.decrypt( cert );
    //            //*************************************************************
    //
    //            PeerInfo p = jsonUtil.from( new String( data ), PeerInfo.class );
    //
    //            if ( p.getKeyPhrase().equals( ( peerManager.getPeerInfo( p.getId() ).getKeyPhrase() ) ) )
    //            {
    //                p.setStatus( PeerStatus.APPROVED );
    //                peerManager.update( p );
    //
    //                //adding remote repository
    //                ManagementHost managementHost = peerManager.getLocalPeer().getManagementHost();
    //                managementHost.addRepository( p.getIp() );
    //
    //                //************ Save Trust SSL Cert **************************************
    //                String rootCertPx2 = new String( cert );
    //
    //                securityManager.getKeyStoreManager()
    //                               .importCertAsTrusted( ChannelSettings.SECURE_PORT_X2, p.getId(), rootCertPx2 );
    //                //***********************************************************************
    //
    //                //************ Export Current Cert **************************************
    //                String localPeerCert =
    //                        securityManager.getKeyStoreManager().exportCertificate( ChannelSettings.SECURE_PORT_X2,
    // "" );
    //
    //                httpContextManager.reloadTrustStore();
    //                //***********************************************************************
    //
    //
    //                PGPPublicKey pkey = keyManager.getPublicKey( p.getId() ); //Get PublicKey from KeyServer
    //                byte certRes[] = encTool.encrypt( localPeerCert.getBytes(), pkey, false );
    //
    //                return Response.ok( HexUtil.byteArrayToHexString( certRes ) ).build();
    //            }
    //            else
    //            {
    //                return Response.status( Response.Status.FORBIDDEN ).build();
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error approving registration request #approveForRegistrationRequest", e );
    //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
    //        }
    //    }
}