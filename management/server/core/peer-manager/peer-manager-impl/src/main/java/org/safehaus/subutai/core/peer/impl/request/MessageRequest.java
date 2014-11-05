package org.safehaus.subutai.core.peer.impl.request;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Payload;


public class MessageRequest
{
    private UUID id;
    private Payload payload;
    private String recipient;


    public MessageRequest( final Payload payload, final String recipient )
    {
        this.id = UUID.randomUUID();
        this.payload = payload;
        this.recipient = recipient;
    }


    public UUID getId()
    {
        return id;
    }


    public Payload getPayload()
    {
        return payload;
    }


    public String getRecipient()
    {
        return recipient;
    }
}
