package org.safehaus.subutai.core.hostregistry.api;


import java.util.Set;


/**
 * Host info. Can contain info about resource host or management host
 */
public interface ResourceHostInfo extends HostInfo
{

    public Set<String> getMacs();


    public Set<ContainerHostInfo> getContainers();
}
