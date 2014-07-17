package org.safehaus.subutai.api.fstracker;


import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;

public interface FileTracker {

    public void addListener( ResponseListener listener );

    public void removeListener( ResponseListener listener );

    public void createConfigPoints( Agent agent, String configPoints[] );

    public void removeConfigPoints( Agent agent, String configPoints[] );

    public String[] listConfigPoints( Agent agent );

}
