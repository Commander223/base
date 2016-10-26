package io.subutai.core.hostregistry.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "host", name = "resource-host", description = "Prints details about resource host" )
public class GetResourceHostInfoCommand extends SubutaiShellCommandSupport
{
    private final HostRegistry hostRegistry;

    @Argument( index = 0, name = "hostname or id", required = true, multiValued = false, description = "resource host "
            + "hostname or id" )
    String identifier;


    public GetResourceHostInfoCommand( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            ResourceHostInfo resourceHostInfo;

            try
            {
                resourceHostInfo = hostRegistry.getResourceHostInfoById( identifier );
            }
            catch ( HostDisconnectedException e )
            {

                resourceHostInfo = hostRegistry.getResourceHostInfoByHostname( identifier );
            }

            System.out.println( resourceHostInfo );
        }
        catch ( HostDisconnectedException e )
        {
            System.out.println( "Host is not connected" );
            log.error( "Error in GetResourceHostInfoCommand", e );
        }

        return null;
    }
}
