package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.configuration.manager.api.TextInjector;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "configuration", name = "cat", description = "Executes cat command on given host" )
public class CatCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "Agent hostname" )
    String hostname;
    @Argument( index = 1, name = "pathToFile", required = true, multiValued = false, description = "Path to file" )
    String pathToFile;
    private AgentManager agentManager;
    private TextInjector textInjector;


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setTextInjector( final TextInjector textInjector ) {
        this.textInjector = textInjector;
    }


    protected Object doExecute() {

        Agent agent = agentManager.getAgentByHostname( hostname );
        String fileContent = textInjector.catFile( agent, pathToFile );
        System.out.println(fileContent);


        //        System.out.println( sb.toString() );
        return null;
    }
}
