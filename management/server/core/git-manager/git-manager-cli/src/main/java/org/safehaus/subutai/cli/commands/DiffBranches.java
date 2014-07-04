package org.safehaus.subutai.cli.commands;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.gitmanager.GitChangedFile;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Diffs branches
 */
@Command( scope = "git", name = "diff-branches", description = "Diff branches to see changed files" )
public class DiffBranches extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 2, name = "branch name 1", required = true, multiValued = false,
            description = "branch name 1" )
    String branchName1;
    @Argument( index = 3, name = "branch name 2", required = false, multiValued = false,
            description = "branch name 2 (master = default)" )
    String branchName2;
    private AgentManager agentManager;
    private GitManager gitManager;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setGitManager( final GitManager gitManager ) { this.gitManager = gitManager; }


    protected Object doExecute() {

        Agent agent = agentManager.getAgentByHostname( hostname );

        try {
            List<GitChangedFile> changedFileList = null;
            if ( branchName2 != null ) {
                changedFileList = gitManager.diffBranches( agent, repoPath, branchName1, branchName2 );
            }
            else {
                changedFileList = gitManager.diffBranches( agent, repoPath, branchName1 );
            }

            for ( GitChangedFile gf : changedFileList ) {
                System.out.println( gf );
            }
        }
        catch ( GitException e ) {
            System.out.println( e );
        }

        return null;
    }
}
