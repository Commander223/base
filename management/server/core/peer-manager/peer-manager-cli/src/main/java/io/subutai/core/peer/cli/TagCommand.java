package io.subutai.core.peer.cli;


import io.subutai.common.peer.ContainerHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Strings;

//todo move to local peer
@Command( scope = "peer", name = "tag-container" )
public class TagCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;
    @Argument( index = 0, name = "container name", multiValued = false, required = true, description = "container "
            + "name" )
    private String containerName;

    @Argument( index = 1, name = "tag", multiValued = false, required = false, description = "tag" )
    protected String tag;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        ContainerHost containerHost = localPeer.getContainerHostByName( containerName );
        if ( !Strings.isNullOrEmpty( tag ) )
        {
            containerHost.addTag( tag );
        }
        System.out.println( String.format( "Tags are %s", containerHost.getTags() ) );
        return null;
    }
}
