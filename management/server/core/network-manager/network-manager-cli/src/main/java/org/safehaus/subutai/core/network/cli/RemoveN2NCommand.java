package org.safehaus.subutai.core.network.cli;


import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "remove-n2n", description = "Removes N2N connection" )
public class RemoveN2NCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveN2NCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "tap interface", required = true, multiValued = false,
            description = "tap interface" )
    String tapInterfaceName;
    @Argument( index = 1, name = "community name", required = true, multiValued = false,
            description = "community name" )
    String communityName;


    public RemoveN2NCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.removeN2NConnection( tapInterfaceName, communityName );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveN2NCommand", e );
        }

        return null;
    }
}
