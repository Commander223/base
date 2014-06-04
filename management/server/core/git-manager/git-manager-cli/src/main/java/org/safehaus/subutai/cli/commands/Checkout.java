package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Checkouts a remote branch (or creates a local branch)
 */
@Command( scope = "git", name = "checkout", description = "Checkout remote branch/create local branch" )
public class Checkout extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 2, name = "branch name", required = true, multiValued = false,
            description = "branch name to switch to or create" )
    String branchName;
    @Argument( index = 3, name = "create branch", required = false, multiValued = false,
            description = "create branch (true/false = default)" )
    boolean create;
    private AgentManager agentManager;
    private GitManager gitManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setGitManager( final GitManager gitManager ) { this.gitManager = gitManager; }


    protected Object doExecute() {

        Agent agent = agentManager.getAgentByHostname( hostname );

        try {
            gitManager.checkout( agent, repoPath, branchName, create );
        }
        catch ( GitException e ) {
            System.out.println( e );
        }

        return null;
    }
}
