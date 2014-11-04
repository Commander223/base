package org.safehaus.subutai.core.peer.api;


import java.util.UUID;


/**
 * Holds info about peer
 */
public class PeerInfo
{
    private String ip = "127.0.0.1";

    private PeerStatus status;

    private String name;
    private UUID id;
    private UUID ownerId;
    //TODO implement setting of port
    private int port = 8181;


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public UUID getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final UUID ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public PeerStatus getStatus()
    {
        return status;
    }


    public void setStatus( final PeerStatus status )
    {
        this.status = status;
    }


    public int getPort()
    {
        return port;
    }
}