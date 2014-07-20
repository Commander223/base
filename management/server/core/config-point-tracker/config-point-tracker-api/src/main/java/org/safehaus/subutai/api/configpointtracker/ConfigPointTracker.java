package org.safehaus.subutai.api.configpointtracker;


import java.util.Set;


public interface ConfigPointTracker {

    public void add( String templateName, String ... configPaths );

    public Set<String> get( String templateName );

}
