package io.subutai.common.peer;


/**
 * Remote peer interface
 */
public interface RemotePeer extends Peer
{
    PeerInfo check() throws PeerException;

    void excludePeerFromEnvironment( String environmentId, String peerId ) throws PeerException;
}
