package org.safehaus.subutai.core.container.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles parallel containermanager creation/destruction
 */
public class ContainerActor implements Callable<ContainerInfo>
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerActor.class );
    private final ContainerInfo containerInfo;
    private final ContainerManager containerManager;
    private final ContainerAction containerAction;
    private String templateName;


    public ContainerActor( final ContainerInfo containerInfo, final ContainerManager containerManager,
                           final ContainerAction containerAction )
    {
        this.containerInfo = containerInfo;
        this.containerManager = containerManager;
        this.containerAction = containerAction;
    }


    public ContainerActor( final ContainerInfo containerInfo, final ContainerManager containerManager,
                           final ContainerAction containerAction, final String templateName )
    {
        this.containerInfo = containerInfo;
        this.containerManager = containerManager;
        this.containerAction = containerAction;
        this.templateName = templateName;
    }


    @Override
    public ContainerInfo call()
    {
        if ( containerAction == ContainerAction.CREATE )
        {

            try
            {
                containerManager.clonesCreate( containerInfo.getPhysicalAgent().getHostname(), templateName,
                        containerInfo.getLxcHostnames() );
                containerInfo.setResult( true );
            }
            catch ( LxcCreateException ignore )
            {
                LOG.trace( "ContainerActor@call: " + ignore.getMessage(), ignore );
            }
        }
        else
        {
            try
            {
                containerManager.clonesDestroy( containerInfo.getPhysicalAgent().getHostname(),
                        containerInfo.getLxcHostnames() );
                containerInfo.setResult( true );
            }
            catch ( LxcDestroyException ignore )
            {
                LOG.trace( "ContainerActor@call: " + ignore.getMessage(), ignore );
            }
        }
        return containerInfo;
    }
}
