package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "message", description = "send message to a specified peer and prints response" )
public class MessageCommand extends OsgiCommandSupport
{
    @Argument( index = 0, name = "peer id", multiValued = false, required = true, description = "Id of target peer" )
    private String peerId;
    @Argument( index = 1, name = "message", multiValued = false, required = true,  description = "Message to send" )
    private String message;

    private final PeerManager peerManager;


    public MessageCommand( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Peer targetPeer = peerManager.getPeer( UUID.fromString( peerId ) );

        System.out.println( targetPeer.sendRequest( message, "ECHO_LISTENER", 5, String.class ) );

        return null;
    }
}
