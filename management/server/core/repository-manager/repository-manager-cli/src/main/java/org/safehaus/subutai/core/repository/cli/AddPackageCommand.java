package org.safehaus.subutai.core.repository.cli;


import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "repo", name = "add", description = "Adds package to repository by path" )
public class AddPackageCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( AddPackageCommand.class.getName() );

    @Argument( index = 0, name = "package path", required = true, multiValued = false,
            description = "absolute path to package" )
    String packagePath;

    private final RepositoryManager repositoryManager;


    public AddPackageCommand( final RepositoryManager repositoryManager ) throws RepositoryException
    {

        Preconditions.checkNotNull( repositoryManager, "Repo manager is null" );

        this.repositoryManager = repositoryManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            repositoryManager.addPackageByPath( packagePath );
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in addPackageByPath", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
