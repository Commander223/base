package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( RequestNotifier.class.getName() );

    private PeerManager peerManager;
    private Messenger messenger;
    private RequestListener listener;
    private Message message;
    private MessageRequest messageRequest;


    public RequestNotifier( final PeerManager peerManager, final Messenger messenger, final RequestListener listener,
                            final Message message, final MessageRequest messageRequest )
    {
        this.peerManager = peerManager;
        this.messenger = messenger;
        this.listener = listener;
        this.message = message;
        this.messageRequest = messageRequest;
    }


    @Override
    public void run()
    {
        //notify listener and obtain response
        MessageResponse messageResponse;
        try
        {
            Object response = listener.onRequest( messageRequest.getPayload() );
            messageResponse = new MessageResponse( messageRequest.getId(), response, null );
        }
        catch ( Exception e )
        {
            LOG.error( "Error thrown by RequestListener", e );
            messageResponse = new MessageResponse( messageRequest.getId(), null, e.getMessage() );
        }

        //send response back
        Message responseMessage = messenger.createMessage( messageResponse );
        Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );
        try
        {
            messenger.sendMessage( sourcePeer, responseMessage, RecipientType.PEER_RESPONSE_LISTENER.name(),
                    Timeouts.PEER_MESSAGE_TIMEOUT );
        }
        catch ( MessageException e )
        {
            LOG.error( "Error sending response to peer message", e );
        }
    }
}
