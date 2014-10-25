package org.safehaus.subutai.core.metric.cli;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * ResourceHostMetricsCommand
 */
@Command( scope = "monitor", name = "container-host-metrics", description = "Lists container host metrics" )
public class ContainerHostMetricsCommand extends OsgiCommandSupport
{
    @Argument( index = 0, name = "environment id", required = true, multiValued = false,
            description = "environment id (uuid)" )
    String environmentIdString;

    private static final Logger LOG = LoggerFactory.getLogger( ContainerHostMetricsCommand.class.getName() );

    private final Monitor monitor;
    private final EnvironmentManager environmentManager;


    public ContainerHostMetricsCommand( final Monitor monitor, final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        UUID environmentId = UUID.fromString( environmentIdString );

        Environment environment = environmentManager.getEnvironmentByUUID( environmentId );
        if ( environment != null )
        {
            Set<ContainerHostMetric> metrics = monitor.getContainerMetrics( environment );
            for ( ContainerHostMetric metric : metrics )
            {
                System.out.println( metric );
            }
        }
        else
        {
            System.out.println( "Environment not found" );
        }


        return null;
    }
}
