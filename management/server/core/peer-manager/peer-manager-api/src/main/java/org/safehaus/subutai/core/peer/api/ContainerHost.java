package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerState;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;


/**
 * ContainerHost class.
 */
public class ContainerHost extends SubutaiHost
{
    private UUID environmentId;
    private UUID creatorPeerId;
    private String templateName;
    private String templateArch;
    private ContainerState state = ContainerState.UNKNOWN;
    private String nodeGroupName;


    public ContainerHost( final Agent agent, UUID peerId, UUID environmentId )
    {
        super( agent, peerId );
        this.environmentId = environmentId;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public UUID getCreatorPeerId()
    {
        return creatorPeerId;
    }


    public void setCreatorPeerId( final UUID creatorPeerId )
    {
        this.creatorPeerId = creatorPeerId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    public ContainerState getState()
    {
        return state;
    }


    public void setState( final ContainerState state )
    {
        this.state = state;
    }


    public void updateHeartbeat()
    {
        lastHeartbeat = System.currentTimeMillis();
        setState( ContainerState.RUNNING );
    }


    public String getQuota( final QuotaEnum quota ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getQuota( this, quota );
    }


    public void setQuota( final QuotaEnum quota, final String value ) throws PeerException
    {
        Peer peer = getPeer();
        peer.setQuota( this, quota, value );
    }

    public Template getTemplate() throws PeerException {
        Peer peer = getPeer( this.getPeerId() );
        return peer.getTemplate(this);
    }
}
