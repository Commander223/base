package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "unregister")
public class UnregisterCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "uuid", multiValued = false, description = "Peer UUID")
    private String uuid;

    private PeerManager peerManager;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        boolean result = peerManager.unregister( uuid );
        System.out.println( result );
        return null;
    }
}
