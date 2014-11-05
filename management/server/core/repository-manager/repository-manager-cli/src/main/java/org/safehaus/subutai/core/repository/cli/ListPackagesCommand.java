package org.safehaus.subutai.core.repository.cli;


import java.util.Set;

import org.safehaus.subutai.core.repository.api.PackageInfo;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "repo", name = "list", description = "Lists packages containing 'term'" )
public class ListPackagesCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListPackagesCommand.class.getName() );

    @Argument( index = 0, name = "term", required = true, multiValued = false,
            description = "term to search" )
    String term;

    private final RepositoryManager repositoryManager;


    public ListPackagesCommand( final RepositoryManager repositoryManager ) throws RepositoryException
    {

        Preconditions.checkNotNull( repositoryManager, "Repo manager is null" );

        this.repositoryManager = repositoryManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            Set<PackageInfo> packages = repositoryManager.listPackages( term );

            for ( PackageInfo packageInfo : packages )
            {
                System.out.println( packageInfo.toString() );
            }
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in listPackages", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
