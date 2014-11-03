package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Peer interface
 */
public interface Peer
{

    public UUID getId();

    public String getName();

    public UUID getOwnerId();

    public PeerInfo getPeerInfo();

    public Set<ContainerHost> getContainerHostsByEnvironmentId( UUID environmentId ) throws PeerException;

    public Set<ContainerHost> createContainers( UUID creatorPeerId, UUID environmentId, List<Template> templates,
                                                int quantity, String strategyId, List<Criteria> criteria )
            throws ContainerCreateException;

    public void startContainer( ContainerHost containerHost ) throws PeerException;

    public void stopContainer( ContainerHost containerHost ) throws PeerException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public boolean isConnected( Host host ) throws PeerException;

    public CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, Host host, CommandCallback callback )
            throws CommandException;

    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException;

    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException;

    public boolean isLocal();

    public String getQuota( ContainerHost host, QuotaEnum quota ) throws PeerException;

    public void setQuota( ContainerHost host, QuotaEnum quota, String value ) throws PeerException;

    public Template getTemplate( ContainerHost containerHost ) throws PeerException;

    public boolean isOnline() throws PeerException;

    public <T, V> V sendRequest( T request, String recipient, int timeout, Class<V> responseType ) throws PeerException;

    public <T> void sendRequest( T request, String recipient, int timeout ) throws PeerException;
}
