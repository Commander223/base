package io.subutai.common.peer;


import java.util.List;
import java.util.Map;
import java.util.Set;

import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.TemplateKurjun;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{

    /**
     * Adds remote apt repository to local apt sources
     */
    void addRepository( final String ip ) throws PeerException;

    /**
     * Removes remote apt repository from local apt sources
     */
    void removeRepository( final String host, final String ip ) throws PeerException;

    /**
     * Sets up tunnels to remote peers in the context of environment
     *
     * @param peerIps - remote peer ips
     * @param environmentId -  context environment
     */
    int setupTunnels( Map<String, String> peerIps, String environmentId ) throws PeerException;

    Vni findVniByEnvironmentId( String environmentId ) throws PeerException;

    /**
     * Returns reserved vnis
     */
    Set<Vni> getReservedVnis() throws PeerException;


    /**
     * Reserves VNI
     */
    Vni reserveVni( Vni vni ) throws PeerException;


    /**
     * Returns all existing gateways
     */
    Set<Gateway> getGateways() throws PeerException;

    //    /**
    //     * Create a gateway
    //     */
    //    void createGateway( String gatewayIp, int vlan ) throws PeerException;

    /**
     * Removes a gateway
     */
    void removeGateway( int vlan ) throws PeerException;

    /**
     * Cleans up environment networking settings. This method is called when an environment is being destroyed to clean
     * up its settings on the local peer.
     */
    void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException;


    /**
     * Removes a tunnel to remote peer
     */
    void removeTunnel( String tunnelIp );


    /**
     * Returns external IP of mgmt host
     */
    String getExternalIp();


    void setupP2PConnection( P2PConfig config ) throws PeerException;

//    /**
//     * Returns reverse proxy environment domain assigned to vlan if any
//     *
//     * @param vlan - vlan id
//     *
//     * @return - domain or null if no domain assigned to the vlan
//     */
//    String getVlanDomain( int vlan ) throws PeerException;
//
//    /**
//     * Removes domain from vlan if any
//     *
//     * @param vlan - vlan id
//     */
//    void removeVlanDomain( int vlan ) throws PeerException;



//    /**
//     * Assigns reverse proxy environment domain  to vlan
//     *
//     * @param vlan - vlan id
//     * @param domain - domain to assign
//     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
//     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
//     */
//    void setVlanDomain( int vlan, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy,
//                        String sslCertPath ) throws PeerException;




    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    public Host bindHost( String id ) throws HostNotFoundException;

    public Host bindHost( ContainerId id ) throws HostNotFoundException;

    //    ContainerHost bindHost( ContainerId containerId ) throws HostNotFoundException;

    /**
     * Returns implementation of ResourceHost interface.
     *
     * @param hostname name of the resource host
     */

    /**
     * Returns resource host instance by its hostname
     */
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns resource host instance by its id
     */
    public ResourceHost getResourceHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    public ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    public ResourceHost getResourceHostByContainerId( String hostId ) throws HostNotFoundException;


    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostname name of the container
     */

    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostId ID of the container
     */
    public ContainerHost getContainerHostById( String hostId ) throws HostNotFoundException;

    //    Quota getQuota( ContainerHost host, QuotaType quota ) throws PeerException;

    //    void setQuota( ContainerHost host, Quota quota ) throws PeerException;

    /**
     * Returns instance of management host
     */
    public Host getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    public Set<ResourceHost> getResourceHosts();


    /**
     * Creates container on the local peer
     *
     * @param resourceHost - target resource host where to host container
     * @param template - source template from which to clone container
     * @param containerName - container name
     */
    //    public ContainerHost createContainer( final ResourceHost resourceHost, final Template template,
    //                                          final String containerName, final ContainerQuota containerQuota )
    //            throws PeerException;


    /**
     * Returns container group by container id
     *
     * @param containerId - id of container
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if container is created not as a part of environment
     */
    //    public ContainerGroup findContainerGroupByContainerId( String containerId ) throws
    // ContainerGroupNotFoundException;

    /**
     * Returns container group by environment id
     *
     * @param environmentId - id of environment
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if group is not found
     */
    //    public ContainerGroup findContainerGroupByEnvironmentId( String environmentId )
    //            throws ContainerGroupNotFoundException;

    /**
     * Returns set of container groups by owner id
     *
     * //     * @param ownerId - id of owner
     *
     * @return - set of {@code ContainerGroup}
     */
    //    public Set<ContainerGroup> findContainerGroupsByOwnerId( String ownerId );
    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    public Set<RequestListener> getRequestListeners();


    /**
     * Returns domain assigned to vni if any
     *
     * @param vni - vni
     *
     * @return - domain or null if no domain assigned to the vni
     */
    public String getVniDomain( Long vni ) throws PeerException;

    /**
     * Removes domain from vni if any
     *
     * @param vni -vni
     */
    public void removeVniDomain( Long vni ) throws PeerException;

    /**
     * Assigns domain to vni
     *
     * @param vni - vni
     * @param domain -  domain to assign
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
     */

    public void setVniDomain( Long vni, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                              String sslCertPath ) throws PeerException;


//    int findTunnel( String tunnelIp, Set<Tunnel> tunnels );
    /**
     * Returns true if hostIp is added to domain by vni
     *
     * @param hostIp - ip of host to check
     * @param vni - vni
     */
    public boolean isIpInVniDomain( String hostIp, Long vni ) throws PeerException;

    public void addIpToVniDomain( String hostIp, Long vni ) throws PeerException;

    public void removeIpFromVniDomain( String hostIp, Long vni ) throws PeerException;

    void setPeerInfo( PeerInfo peerInfo );

    Set<ContainerHost> findContainersByEnvironmentId( final String environmentId );

    Set<ContainerHost> findContainersByOwnerId( final String ownerId );

    List<P2PConfig> setupP2PConnection( String environmentId, Set<Peer> peers ) throws PeerException;

    void addToTunnel( P2PConfig config ) throws PeerException;

    List<TemplateKurjun> getTemplates();

    TemplateKurjun getTemplateByName( String templateName );

    ContainerHost findContainerById( ContainerId containerId );

    int setupContainerSsh( String containerHostId, int sshIdleTimeout ) throws PeerException;

    List<ContainerHost> getPeerContainers( String peerId );
}
