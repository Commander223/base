package org.safehaus.subutai.impl.filetracker;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.api.fstracker.FileTracker;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Needed mostly for testing FileTracker
 */
@Command( scope = "fs-tracker", name = "test" )
public class CliTest extends OsgiCommandSupport implements ResponseListener {

    private static final String CONFIG_POINTS[] = {
        "/etc",
        "/etc/ksks-agent"
    };

    private AgentManager agentManager;

    private FileTracker fileTracker;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setFileTracker( FileTracker fileTracker ) {
        this.fileTracker = fileTracker;
    }


    protected Object doExecute() {

        fileTracker.addListener( this );

        Agent agent = getAgent();

        fileTracker.createConfigPoints( agent, CONFIG_POINTS );

//        fileTracker.removeConfigPoints( agent, CONFIG_POINTS );

//        fileTracker.listConfigPoints( agent );

        return null;
    }


    @Override
    public void onResponse( Response response ) {
        System.out.println( "Response: " + response );
    }


    private Agent getAgent() {

        for ( Agent agent: agentManager.getAgents() ) {
            if ( "management".equals( agent.getHostname() ) ) {
                return agent;
            }
        }

        return null;
    }

}
