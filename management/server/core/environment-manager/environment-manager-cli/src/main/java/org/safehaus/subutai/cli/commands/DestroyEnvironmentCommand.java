package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.api.manager.EnvironmentManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command(scope = "environment", name = "destroy", description = "Command to build environment",
        detailedDescription = "Command to build environment by given blueprint description")
public class DestroyEnvironmentCommand extends OsgiCommandSupport {

    EnvironmentManager environmentManager;

    @Argument(name = "environmentName", index = 0, required = true, multiValued = false,
            description = "Environment name", valueToShowInHelp = "Environment name")
    String environmentName;


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        environmentManager.destroyEnvironment( environmentName );
        return null;
    }
}
