package org.safehaus.subutai.cli.commands;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitBranch;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays branches
 */
@Command( scope = "git", name = "list-branches", description = "List local/remote branches" )
public class ListBranches extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 2, name = "remote", required = false, multiValued = false, description = "list remote branches (true/false)" )
    boolean remote;
    private AgentManager agentManager;
    private GitManager gitManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setGitManager( final GitManager gitManager ) { this.gitManager = gitManager; }


    protected Object doExecute() {

        Agent agent = agentManager.getAgentByHostname( hostname );

        try {
            List<GitBranch> branches = gitManager.listBranches( agent, repoPath, remote );
            for ( GitBranch branch : branches ) {
                System.out.println( branch );
            }
        }
        catch ( GitException e ) {
            System.out.println( e );
        }

        return null;
    }
}
