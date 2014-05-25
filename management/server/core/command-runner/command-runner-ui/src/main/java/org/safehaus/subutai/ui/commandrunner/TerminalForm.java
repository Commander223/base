/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.commandrunner;


import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.server.ui.MgmtAgentManager;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Disposable;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Runo;


/**
 * Command Runner UI - Terminal
 */
public class TerminalForm extends CustomComponent implements Disposable {

    private final MgmtAgentManager agentTree;
    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;
    private ExecutorService executor;


    public TerminalForm( final CommandRunner commandRunner, final AgentManager agentManager ) {
        setHeight( 100, UNITS_PERCENTAGE );

        executor = Executors.newCachedThreadPool();

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, UNITS_PIXELS );
        agentTree = MgmtApplication.createAgentTree();
        horizontalSplit.setFirstComponent( agentTree );

        GridLayout grid = new GridLayout( 20, 10 );
        grid.setSizeFull();
        grid.setMargin( true );
        grid.setSpacing( true );
        commandOutputTxtArea = new TextArea( "Commands output" );
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate( true );
        commandOutputTxtArea.setWordwrap( false );
        grid.addComponent( commandOutputTxtArea, 0, 0, 19, 8 );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );
        Label programLbl = new Label( "Program" );
        final TextField programTxtFld = new TextField();
        programTxtFld.setValue( "pwd" );
        programTxtFld.setWidth( 300, UNITS_PIXELS );
        controls.addComponent( programLbl );
        controls.addComponent( programTxtFld );
        Label workDirLbl = new Label( "Cwd" );
        final TextField workDirTxtFld = new TextField();
        workDirTxtFld.setValue( "/" );
        controls.addComponent( workDirLbl );
        controls.addComponent( workDirTxtFld );
        Label timeoutLbl = new Label( "Timeout" );
        final TextField timeoutTxtFld = new TextField();
        timeoutTxtFld.setValue( "30" );
        timeoutTxtFld.setWidth( 30, UNITS_PIXELS );
        controls.addComponent( timeoutLbl );
        controls.addComponent( timeoutTxtFld );
        Label requestTypeLabel = new Label( "Req Type" );
        controls.addComponent( requestTypeLabel );
        final ComboBox requestTypeCombo = new ComboBox( null,
                Arrays.asList( RequestType.EXECUTE_REQUEST, RequestType.TERMINATE_REQUEST, RequestType.PS_REQUEST ) );
        requestTypeCombo.setMultiSelect( false );
        requestTypeCombo.setImmediate( true );
        requestTypeCombo.setTextInputAllowed( false );
        requestTypeCombo.setNullSelectionAllowed( false );
        requestTypeCombo.setValue( RequestType.EXECUTE_REQUEST );
        requestTypeCombo.setWidth( 150, UNITS_PIXELS );
        controls.addComponent( requestTypeCombo );
        Button clearBtn = new Button( "Clear" );
        controls.addComponent( clearBtn );
        final Button sendBtn = new Button( "Send" );
        controls.addComponent( sendBtn );
        final Label indicator = new Label();
        indicator.setIcon( new ThemeResource( "icons/indicator.gif" ) );
        indicator.setContentMode( Label.CONTENT_XHTML );
        indicator.setHeight( 11, UNITS_PIXELS );
        indicator.setWidth( 50, UNITS_PIXELS );
        indicator.setVisible( false );
        controls.addComponent( indicator );

        grid.addComponent( controls, 0, 9, 19, 9 );

        horizontalSplit.setSecondComponent( grid );
        setCompositionRoot( horizontalSplit );

        programTxtFld.addShortcutListener( new ShortcutListener( "Shortcut Name", ShortcutAction.KeyCode.ENTER, null ) {
            @Override
            public void handleAction( Object sender, Object target ) {
                sendBtn.click();
            }
        } );
        sendBtn.addListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                Set<Agent> agents = agentTree.getSelectedAgents();
                if ( agents.isEmpty() ) {
                    show( "Please, select nodes" );
                }
                else if ( programTxtFld.getValue() == null || Strings
                        .isNullOrEmpty( programTxtFld.getValue().toString() ) ) {
                    show( "Please, enter command" );
                }
                else {

                    RequestBuilder requestBuilder = new RequestBuilder( programTxtFld.getValue().toString() );

                    if ( requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST ) {
                        if ( Util.isNumeric( programTxtFld.getValue().toString() )
                                && Integer.valueOf( programTxtFld.getValue().toString() ) > 0 ) {
                            requestBuilder.withPid( Integer.valueOf( programTxtFld.getValue().toString() ) );
                            requestBuilder.withType( RequestType.TERMINATE_REQUEST );
                        }
                        else {
                            show( "Please, enter numeric PID greater than 0 to kill" );
                            return;
                        }
                    }
                    else if ( requestTypeCombo.getValue() == RequestType.PS_REQUEST ) {
                        requestBuilder.withType( RequestType.PS_REQUEST );
                    }

                    if ( timeoutTxtFld.getValue() != null && Util.isNumeric( timeoutTxtFld.getValue().toString() ) ) {
                        int timeout = Integer.valueOf( timeoutTxtFld.getValue().toString() );
                        if ( timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC ) {
                            requestBuilder.withTimeout( timeout );
                        }
                    }

                    if ( workDirTxtFld.getValue() != null && !Strings
                            .isNullOrEmpty( workDirTxtFld.getValue().toString() ) ) {
                        requestBuilder.withCwd( workDirTxtFld.getValue().toString() );
                    }
                    final Command command = commandRunner.createCommand( requestBuilder, agents );
                    indicator.setVisible( true );
                    taskCount++;
                    executor.execute( new Runnable() {

                        public void run() {
                            commandRunner.runCommand( command, new CommandCallback() {

                                @Override
                                public void onResponse( Response response, AgentResult agentResult, Command command ) {
                                    Agent agent = agentManager.getAgentByUUID( response.getUuid() );
                                    String host = agent == null ? String.format( "Offline[%s]", response.getUuid() ) :
                                                  agent.getHostname();
                                    StringBuilder out =
                                            new StringBuilder( host ).append( " [" ).append( response.getPid() )
                                                                     .append( "]" ).append( ":\n" );
                                    if ( !Strings.isNullOrEmpty( response.getStdOut() ) ) {
                                        out.append( response.getStdOut() ).append( "\n" );
                                    }
                                    if ( !Strings.isNullOrEmpty( response.getStdErr() ) ) {
                                        out.append( response.getStdErr() ).append( "\n" );
                                    }
                                    if ( response.isFinal() ) {
                                        if ( response.getType() == ResponseType.EXECUTE_RESPONSE_DONE ) {
                                            out.append( "Exit code: " ).append( response.getExitCode() )
                                               .append( "\n\n" );
                                        }
                                        else {
                                            out.append( response.getType() ).append( "\n\n" );
                                        }
                                    }
                                    addOutput( out.toString() );
                                }
                            } );

                            taskCount--;
                            if ( taskCount == 0 ) {
                                indicator.setVisible( false );
                            }
                        }
                    } );
                }
            }
        } );
        clearBtn.addListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                commandOutputTxtArea.setValue( "" );
            }
        } );
    }


    private void addOutput( String output ) {
        if ( !Strings.isNullOrEmpty( output ) ) {
            commandOutputTxtArea.setValue( String.format( "%s%s", commandOutputTxtArea.getValue(), output ) );
            commandOutputTxtArea.setCursorPosition( commandOutputTxtArea.getValue().toString().length() - 1 );
        }
    }


    private void show( String notification ) {
        getWindow().showNotification( notification );
    }


    public void dispose() {
        agentTree.dispose();
        executor.shutdown();
    }
}
