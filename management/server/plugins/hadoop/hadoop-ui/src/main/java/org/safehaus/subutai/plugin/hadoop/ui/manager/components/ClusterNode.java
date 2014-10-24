package org.safehaus.subutai.plugin.hadoop.ui.manager.components;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;


/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout
{

    public static final int ICON_SIZE = 18;

    protected HadoopClusterConfig cluster;
    protected Embedded progressButton, startButton, stopButton, restartButton;
    protected List<ClusterNode> slaveNodes;
    protected Label hostname;


    public ClusterNode( HadoopClusterConfig cluster )
    {
        this.cluster = cluster;
        slaveNodes = new ArrayList<>();

        setMargin( true );
        setSpacing( true );

        addComponent( getHostnameLabel() );
        setComponentAlignment( hostname, Alignment.MIDDLE_CENTER );
        addComponent( getProgressButton() );
        setComponentAlignment( progressButton, Alignment.TOP_CENTER );
        addComponent( getStartButton() );
        setComponentAlignment( startButton, Alignment.TOP_CENTER );
        addComponent( getStopButton() );
        setComponentAlignment( stopButton, Alignment.TOP_CENTER );
        addComponent( getRestartButton() );
        setComponentAlignment( restartButton, Alignment.TOP_CENTER );
    }


    private Label getHostnameLabel()
    {
        hostname = new Label( "" );
        return hostname;
    }


    private Embedded getProgressButton()
    {
        progressButton = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
        progressButton.setWidth( ICON_SIZE + 2, Unit.PIXELS );
        progressButton.setHeight( ICON_SIZE + 2, Unit.PIXELS );
        progressButton.setVisible( false );

        return progressButton;
    }


    private Embedded getStartButton()
    {
        startButton = new Embedded( "", new ThemeResource( "img/btn/play.png" ) );
        startButton.setDescription( "Start" );
        /*startButton.setWidth(ICON_SIZE, Unit.PIXELS);
        startButton.setHeight(ICON_SIZE, Unit.PIXELS);*/

        return startButton;
    }


    private Embedded getStopButton()
    {
        stopButton = new Embedded( "", new ThemeResource( "img/btn/stop.png" ) );
        stopButton.setDescription( "Stop" );
        /*stopButton.setWidth(ICON_SIZE, Unit.PIXELS);
        stopButton.setHeight(ICON_SIZE, Unit.PIXELS);*/

        return stopButton;
    }


    private Embedded getRestartButton()
    {
        restartButton = new Embedded( "", new ThemeResource( "img/btn/update.png" ) );
        restartButton.setDescription( "Restart" );
        /*restartButton.setWidth(ICON_SIZE, Unit.PIXELS);
        restartButton.setHeight(ICON_SIZE, Unit.PIXELS);*/

        return restartButton;
    }


    public void setHostname( String value )
    {
        hostname.setValue( value );
    }


    public void addSlaveNode( ClusterNode slaveNode )
    {
        slaveNodes.add( slaveNode );
    }


    protected void getStatus( UUID trackID )
    {
    }


    protected void setLoading( boolean isLoading )
    {

    }


    public HadoopClusterConfig getCluster()
    {
        return cluster;
    }
}
