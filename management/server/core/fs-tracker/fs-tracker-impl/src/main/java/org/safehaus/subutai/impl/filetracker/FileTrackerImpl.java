package org.safehaus.subutai.impl.filetracker;


import java.util.HashSet;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.api.fstracker.FileTracker;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;

import com.google.common.collect.Sets;


public class FileTrackerImpl implements FileTracker, ResponseListener {

    private final HashSet<ResponseListener> listeners = new HashSet<>();

    private CommandRunner commandRunner;

    private CommunicationManager communicationManager;


    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }


    public void setCommunicationManager( CommunicationManager communicationManager ) {
        this.communicationManager = communicationManager;
    }


    public void init() {
        communicationManager.addListener( this );
    }


    public void destroy() {
        communicationManager.removeListener( this );
    }


    @Override
    public void addListener( ResponseListener listener ) {
        listeners.add( listener );
    }


    @Override
    public void removeListener( ResponseListener listener ) {
        listeners.remove( listener );
    }


    @Override
    public void onResponse( Response response ) {
        if ( response == null || response.getType() != ResponseType.INOTIFY_RESPONSE ) {
            return;
        }

        for ( ResponseListener listener : listeners ) {
            listener.onResponse( response );
        }
    }


    @Override
    public void createConfigPoints( Agent agent, String configPoints[] ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( "pwd" )
                        .withType( RequestType.INOTIFY_REQUEST )
                        .withConfPoints( configPoints ),
                Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public void removeConfigPoints( Agent agent, String configPoints[] ) {

        Command command = commandRunner.createCommand(
            new RequestBuilder( "pwd" )
                    .withType( RequestType.INOTIFY_CANCEL_REQUEST )
                    .withConfPoints( configPoints ),
            Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );
    }


    @Override
    public String[] listConfigPoints( final Agent agent ) {

        Command command = commandRunner.createCommand(
             new RequestBuilder( "pwd" )
                 .withType( RequestType.INOTIFY_SHOW_REQUEST ),
            Sets.newHashSet( agent )
        );

        commandRunner.runCommandAsync( command );

        return null;
    }
}
