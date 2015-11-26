package io.subutai.core.environment.rest.ui;


import java.util.List;

import io.subutai.common.peer.ContainerType;
import io.subutai.common.host.ContainerHostState;


/**
 * Trimmed container for REST
 */
public class ContainerJson
{
    private String id;
    private String environmentId;
    private String hostname;
    private ContainerHostState state;
    private String ip;
    private String templateName;
    private ContainerType type;
    private List<String> tags;



    public ContainerJson( final String id, final String environmentId, final String hostname,
                          final ContainerHostState state, final String ip, final String templateName,
                          final ContainerType type, final List<String> tags )
    {
        this.id = id;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.state = state;
        this.ip = ip;
        this.templateName = templateName;
        this.type = type;
        this.tags = tags;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public ContainerHostState getState()
    {
        return state;
    }


    public void setState( final ContainerHostState state )
    {
        this.state = state;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public ContainerType getType()
    {
        return type;
    }


    public void setType( final ContainerType type )
    {
        this.type = type;
    }


    public List<String> getTags()
    {
        return tags;
    }


    public void setTags( final List<String> tags )
    {
        this.tags = tags;
    }
}
