package org.safehaus.subutai.core.peer.impl.request;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Payload;


public class MessageResponse
{
    private UUID requestId;
    private Payload payload;
    private String exception;


    public MessageResponse( final UUID requestId, final Payload payload, final String exception )
    {
        this.requestId = requestId;
        this.payload = payload;
        this.exception = exception;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Payload getPayload()
    {
        return payload;
    }


    public String getException()
    {
        return exception;
    }
}
