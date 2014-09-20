package org.safehaus.subutai.core.filetracker.api;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ResponseListener;


public interface FileTracker
{

    public void addListener( ResponseListener listener );

    public void removeListener( ResponseListener listener );

    public void createConfigPoints( Agent agent, String configPoints[] );

    public void removeConfigPoints( Agent agent, String configPoints[] );

    public String[] listConfigPoints( Agent agent );
}
