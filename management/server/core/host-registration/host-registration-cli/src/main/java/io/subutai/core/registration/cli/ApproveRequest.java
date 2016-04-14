package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;


@Command( scope = "node", name = "approve", description = "approve new registration request" )
public class ApproveRequest extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "requestId", multiValued = false, required = true, description = "Request Id" )
    private String requestId;

    private RegistrationManager registrationManager;


    public ApproveRequest( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        registrationManager.approveRequest( requestId );

        System.out.println( registrationManager.getRequest( requestId ).toString() );

        return null;
    }
}
