package org.safehaus.subutai.core.peer.api;


import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import com.google.common.collect.Sets;


/**
 * Resource host implementation.
 */
public class ResourceHost extends SubutaiHost
{
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "State:(\\s*)(.*)" );
    private static final Pattern LOAD_AVERAGE_PATTERN = Pattern.compile( "load average: (.*)" );
    private static final long WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS = 10000;


    Set<ContainerHost> containersHosts = Sets.newHashSet();


    public ResourceHost( final Agent agent, UUID peerId )
    {
        super( agent, peerId );
    }


    public void addContainerHost( ContainerHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Container host could not be null." );
        }
        containersHosts.add( host );
    }


    public boolean startContainerHost( final ContainerHost container ) throws CommandException
    {

        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d &", container.getHostname() ) )
                        .withTimeout( 180 );
        execute( requestBuilder );
        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.RUNNING.equals( getContainerHostState( container ) );
    }


    private ContainerState getContainerHostState( final ContainerHost container ) throws CommandException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", container.getHostname() ) )
                        .withTimeout( 30 );
        CommandResult result = execute( requestBuilder );

        String stdOut = result.getStdOut();

        Matcher m = LXC_STATE_PATTERN.matcher( stdOut );
        if ( m.find() )
        {
            return ContainerState.valueOf( m.group( 2 ) );
        }
        else
        {
            return ContainerState.UNKNOWN;
        }
    }


    public ServerMetric getMetric() throws ResourceHostException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( "free -m | grep buffers/cache ; df /lxc-data | grep /lxc-data ; uptime ; nproc" )
                        .withTimeout( 30 );
        try
        {
            CommandResult result = execute( requestBuilder );
            ServerMetric serverMetric = null;
            if ( result.hasCompleted() )
            {
                String[] metrics = result.getStdOut().split( "\n" );
                serverMetric = gatherMetrics( metrics );
                serverMetric.setAverageMetrics( gatherAvgMetrics() );
            }
            return serverMetric;
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "unable retrieve host metric", e.toString() );
        }
    }


    public Set<ContainerHost> getContainerHosts()
    {
        return containersHosts;
    }


    /**
     * Gather metrics from elastic search for a one week period
     */
    private Map<MetricType, Double> gatherAvgMetrics()
    {
        //TODO: Implement me
        return new EnumMap<>( MetricType.class );
    }


    /**
     * Gather metrics from linux commands outputs.
     */
    private ServerMetric gatherMetrics( String[] metrics )
    {
        int freeRamMb = 0;
        int freeHddMb = 0;
        int numOfProc = 0;
        double loadAvg = 0;
        double cpuLoadPercent = 100;
        // parsing only 4 metrics
        if ( metrics.length != 4 )
        {
            return null;
        }
        boolean parseOk = true;
        for ( int line = 0; parseOk && line < metrics.length; line++ )
        {
            String metric = metrics[line];
            switch ( line )
            {
                case 0:
                    //-/+ buffers/cache:       1829       5810
                    String[] ramMetric = metric.split( "\\s+" );
                    String freeRamMbStr = ramMetric[ramMetric.length - 1];
                    try
                    {
                        freeRamMb = Integer.parseInt( freeRamMbStr );
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
                case 1:
                    //lxc-data       143264768 608768 142656000   1% /lxc-data
                    String[] hddMetric = metric.split( "\\s+" );
                    if ( hddMetric.length == 6 )
                    {
                        String hddMetricKbStr = hddMetric[3];
                        try
                        {
                            freeHddMb = Integer.parseInt( hddMetricKbStr ) / 1024;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 2:
                    // 09:17:38 up 4 days, 23:06,  0 users,  load average: 2.18, 3.06, 2.12
                    Matcher m = LOAD_AVERAGE_PATTERN.matcher( metric );
                    if ( m.find() )
                    {
                        String[] loads = m.group( 1 ).split( "," );
                        try
                        {
                            loadAvg = ( Double.parseDouble( loads[0] ) + Double.parseDouble( loads[1] ) + Double
                                    .parseDouble( loads[2] ) ) / 3;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 3:
                    try
                    {
                        numOfProc = Integer.parseInt( metric );
                        if ( numOfProc > 0 )
                        {
                            cpuLoadPercent = ( loadAvg / numOfProc ) * 100;
                        }
                        else
                        {
                            break;
                        }
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
            }
        }
        if ( parseOk )
        {
            return new ServerMetric( getHostname(), freeHddMb, freeRamMb, ( int ) cpuLoadPercent, numOfProc, null );
        }
        else
        {
            return null;
        }
    }


    public boolean stopContainerHost( final ContainerHost container ) throws CommandException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s &", container.getHostname() ) )
                        .withTimeout( 180 );
        execute( requestBuilder );

        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.STOPPED.equals( getContainerHostState( container ) );
    }


    public boolean destroyContainerHost( final ContainerHost containerHost ) throws ResourceHostException
    {
        return run( Command.DESTROY, containerHost.getHostname() );
    }


    public ContainerHost getContainerHostByName( final String hostname )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }


    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getEnvironmentId() == null )
            {
                continue;
            }
            if ( containerHost.getEnvironmentId().equals( environmentId ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    public void removeContainerHost( final Host result )
    {
        if ( containersHosts.contains( result ) )
        {
            containersHosts.remove( result );
        }
    }


    public Host getContainerHostById( final UUID id )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getId().equals( id ) )
            {
                result = host;
            }
        }
        return result;
    }


    public ContainerHost createContainer( final UUID creatorPeerId, final UUID environmentId,
                                          final List<Template> templates, final String containerName )
            throws PeerException
    {
        Template template = templates.get( templates.size() - 1 );
        boolean cloneResult = false;
        try
        {
            cloneResult = run( Command.CLONE, template.getTemplateName(), containerName, environmentId.toString() );
        }
        catch ( ResourceHostException ignore )
        {

        }

        if ( cloneResult )
        {
            return waitAgentAndCreateContainerHost( containerName, template.getTemplateName(), environmentId,
                    creatorPeerId );
        }
        prepareTemplates( templates );
        cloneResult = run( Command.CLONE, template.getTemplateName(), containerName, environmentId.toString() );
        if ( cloneResult )
        {
            return waitAgentAndCreateContainerHost( containerName, template.getTemplateName(), environmentId,
                    creatorPeerId );
        }
        else
        {
            throw new ResourceHostException(
                    String.format( "Unable create container %s on %s using template %s", containerName, getHostname(),
                            template.getTemplateName() ), null );
        }
    }


    private ContainerHost waitAgentAndCreateContainerHost( final String containerName, final String templateName,
                                                           final UUID envId, final UUID creatorPeerId )
            throws PeerException
    {
        LocalPeer peer = ( LocalPeer ) getPeer();
        Agent agent = peer.waitForAgent( containerName, 120000 );
        if ( agent == null )
        {
            throw new ResourceHostException( "Container successfully created by agent not respond.", null );
        }
        ContainerHost containerHost = new ContainerHost( agent, getPeerId(), envId );
        containerHost.setParentAgent( getAgent() );
        containerHost.setCreatorPeerId( creatorPeerId );
        containerHost.setTemplateName( templateName );
        containerHost.updateHeartbeat();
        return containerHost;
    }


    protected void prepareTemplates( List<Template> templates ) throws ResourceHostException
    {
        for ( Template p : templates )
        {
            checkTemplate( p );
        }
    }


    private void checkTemplate( final Template p ) throws ResourceHostException
    {

        if ( isTemplateExist( p ) )
        {
            return;
        }

        importTemplate( p );
        if ( isTemplateExist( p ) )
        {
            return;
        }
        // trying add repository
        updateRepository( p );
        importTemplate( p );
        if ( !isTemplateExist( p ) )
        {
            throw new ResourceHostException( String.format( "Could not prepare template %s on %s", p.getTemplateName(),
                    getAgent().getHostname() ), null );
        }
    }


    protected boolean isTemplateExist( final Template template ) throws ResourceHostException
    {
        return run( Command.LIST_TEMPLATES, template.getTemplateName() );
    }


    protected void importTemplate( Template template ) throws ResourceHostException
    {
        run( Command.IMPORT, template.getTemplateName() );
    }


    protected void updateRepository( Template template ) throws ResourceHostException
    {
        if ( template.isRemote() )
        {
            run( Command.ADD_SOURCE, template.getPeerId().toString() );
            run( Command.APT_GET_UPDATE );
        }
    }


    /**
     * Promotes a given clone into a template with given name. This method gives possibility to promote a copy of the
     * clone instead of the clone itself.
     *
     * @param hostName the physical host name
     * @param cloneName name of the clone to be converted
     * @param newName new name for template
     * @param copyit if set <tt>true</tt>, a copy of clone is made first and a copied clone is promoted to template
     *
     * @return <tt>true</tt> if promote successfully completed
     */
    public boolean promote( String hostName, String cloneName, String newName, boolean copyit )
            throws ResourceHostException
    {
        List<String> args = new ArrayList<>();
        if ( newName != null && newName.length() > 0 )
        {
            args.add( "-n " + newName );
        }
        if ( copyit )
        {
            args.add( "-c" );
        }
        args.add( cloneName );
        String[] arr = args.toArray( new String[args.size()] );

        return run( Command.PROMOTE, arr );
    }


    /**
     * Exports the template in the given server into a deb package.
     *
     * @param templateName the template name to be exported
     *
     * @return path to generated deb package
     */
    public String exportTemplate( String templateName ) throws ResourceHostException
    {
        if ( run( Command.EXPORT, templateName ) )
        {
            return getExportedPackageFilePath( templateName );
        }
        else
        {
            return null;
        }
    }


    /**
     * Gets a full Debian package name for a given template. Name does not have <tt>.deb</tt> extension.
     *
     * @param templateName the template name
     */
    public String getDebianPackageName( String templateName ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( Command.GET_DEB_PACKAGE_NAME.build( templateName ) );
            if ( commandResult.hasSucceeded() )
            {
                return commandResult.getStdOut();
            }
            else
            {
                return null;
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get deb package name.", e.toString() );
        }
    }


    private String getExportedPackageFilePath( String templateName ) throws ResourceHostException
    {
        String result = null;
        try
        {
            CommandResult dirCommandResult = execute( Command.SUBUTAI_TMPDIR.build() );
            if ( dirCommandResult.hasSucceeded() )
            {
                String dir = dirCommandResult.getStdOut();
                CommandResult packageNameCommandResult = execute( Command.GET_PACKAGE_NAME.build( templateName ) );
                if ( packageNameCommandResult.hasSucceeded() )
                {
                    result = Paths.get( dir, packageNameCommandResult.getStdOut() ).toString();
                }
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get exported package file path.", e.toString() );
        }

        if ( result == null )
        {
            throw new ResourceHostException( "Could not get exported package file path.", templateName );
        }
        return result;
    }


    /**
     * Gets package name for a given template. Package name is a name used in Apt commands. It is NOT a full Debian
     * package name of a template.
     */
    public String getPackageName( String templateName ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( Command.GET_PACKAGE_NAME.build( templateName ) );
            if ( commandResult.hasSucceeded() )
            {
                return commandResult.getStdOut();
            }
            else
            {
                return null;
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get package name.", e.toString() );
        }
    }


    protected boolean run( Command command, String... args ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( command.build( args ) );
            return commandResult.hasSucceeded();
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Could not execute script/command %s", String.format( command.script, args ) ),
                    e.toString() );
        }
    }


    enum Command
    {
        LIST_TEMPLATES( "subutai list -t %s" ),
        CLONE( "subutai clone %s %s -e %s &" ),
        DESTROY( "subutai destroy %s" ),
        IMPORT( "subutai import %s" ),
        PROMOTE( "promote %s" ),
        EXPORT( "subutai export %s" ),
        SUBUTAI_TMPDIR( "echo $SUBUTAI_TMPDIR" ),
        GET_PACKAGE_NAME( ". /usr/share/subutai-cli/subutai/lib/deb_ops && get_package_name  %s" ),
        GET_DEB_PACKAGE_NAME(
                ". /etc/subutai/config && . /usr/share/subutai-cli/subutai/lib/deb_ops && get_debian_package_name  "
                        + "%s" ),
        ADD_SOURCE( "echo \"deb http://gw.intra.lan:9999/%1$s trusty main\" > /etc/apt/sources.list.d/%1$s.list " ),
        REMOVE_SOURCE( "rm /etc/apt/sources.list.d/%1$s.list " ),
        APT_GET_UPDATE( "apt-get update", 240 );

        String script;
        int timeout = 180;


        Command( String script )
        {
            this.script = script;
        }


        Command( String script, int timeout )
        {
            this.script = script;
            this.timeout = timeout;
        }


        public RequestBuilder build( String... args )
        {
            String s = String.format( this.script, args );
            RequestBuilder rb = new RequestBuilder( s );
            rb.withTimeout( timeout );
            return rb;
        }
    }
}
