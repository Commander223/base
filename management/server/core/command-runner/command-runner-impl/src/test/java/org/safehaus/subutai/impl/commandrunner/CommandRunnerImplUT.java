/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.commandrunner;


import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.CommandStatus;
import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.Response;

import com.jayway.awaitility.Awaitility;

import static com.jayway.awaitility.Awaitility.to;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for CommandRunner class
 */
public class CommandRunnerImplUT {

    private static ExecutorService exec;
    private final boolean allTests = true;
    private CommunicationManager communicationManager;
    private CommandRunnerImpl commandRunner;


    @BeforeClass
    public static void setupClass() {
        exec = Executors.newCachedThreadPool();
    }


    @AfterClass
    public static void afterClass() {
        exec.shutdown();
    }


    @Before
    public void beforeMethod() {
        communicationManager = mock( CommunicationManager.class );
        commandRunner = new CommandRunnerImpl( communicationManager );
        commandRunner.init();
    }


    @After
    public void afterMethod() {
        commandRunner.destroy();
    }


    @Test
    public void shouldRunCommand() {
        Assume.assumeTrue( allTests );
        CommandRunner commandRunnerMock = mock( CommandRunner.class );
        Command command = mock( CommandImpl.class );
        CommandCallback callback = mock( CommandCallback.class );

        commandRunnerMock.runCommand( command, callback );

        verify( commandRunnerMock ).runCommand( command, callback );
    }


    @Test
    public void shouldAddListenerToCommManager() {
        Assume.assumeTrue( allTests );

        verify( communicationManager ).addListener( commandRunner );
    }


    @Test
    public void shouldRemoveListenerFromCommManager() {
        Assume.assumeTrue( allTests );

        commandRunner.destroy();

        verify( communicationManager ).removeListener( commandRunner );
    }


    @Test
    public void shouldSendRequestToCommManager() {
        Assume.assumeTrue( allTests );

        Command command = MockUtils.getCommand( "ls", commandRunner, UUID.randomUUID(), 1 );

        commandRunner.runCommandAsync( command );

        verify( communicationManager ).sendRequest( any( Request.class ) );
    }


    @Test
    @Ignore
    public void commandShouldTimeout() {
        Assume.assumeTrue( allTests );

        Command command = MockUtils.getCommand( "ls", commandRunner, UUID.randomUUID(), 1 );

        commandRunner.runCommand( command );

        assertEquals( CommandStatus.TIMEOUT, command.getCommandStatus() );
    }


    @Test
    @Ignore
    public void commandShouldTimeoutAsync() {
        Assume.assumeTrue( allTests );

        Command command = MockUtils.getCommand( "ls", commandRunner, UUID.randomUUID(), 1 );

        commandRunner.runCommandAsync( command );

        Awaitility.await().atMost( 3050, TimeUnit.MILLISECONDS ).with().pollInterval( 10, TimeUnit.MILLISECONDS )
                  .untilCall( to( command ).getCommandStatus(), is( CommandStatus.TIMEOUT ) );
    }


    @Test
    public void commandShouldSucceedAsync() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getSucceededResponse( agentUUID, command.getCommandUUID() );

        commandRunner.runCommandAsync( command );
        exec.execute( new Runnable() {

            public void run() {
                commandRunner.onResponse( response );
            }
        } );

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 100, TimeUnit.MILLISECONDS )
                  .untilCall( to( command ).getCommandStatus(), is( CommandStatus.SUCCEEDED ) );
    }


    @Test
    public void commandShouldSucceed() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getSucceededResponse( agentUUID, command.getCommandUUID() );

        exec.execute( new Runnable() {

            public void run() {
                try {
                    Thread.sleep( 100 );
                    commandRunner.onResponse( response );
                }
                catch ( InterruptedException ex ) {
                }
            }
        } );
        commandRunner.runCommand( command );

        assertEquals( CommandStatus.SUCCEEDED, command.getCommandStatus() );
    }


    @Test
    public void commandShouldFailAsync() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getFailedResponse( agentUUID, command.getCommandUUID() );

        commandRunner.runCommandAsync( command );
        exec.execute( new Runnable() {

            public void run() {
                commandRunner.onResponse( response );
            }
        } );

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 100, TimeUnit.MILLISECONDS )
                  .untilCall( to( command ).getCommandStatus(), is( CommandStatus.FAILED ) );
    }


    @Test
    public void commandShouldFail() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getFailedResponse( agentUUID, command.getCommandUUID() );

        exec.execute( new Runnable() {

            public void run() {
                try {
                    Thread.sleep( 100 );
                    commandRunner.onResponse( response );
                }
                catch ( InterruptedException ex ) {
                }
            }
        } );

        commandRunner.runCommand( command );

        assertEquals( CommandStatus.FAILED, command.getCommandStatus() );
    }


    @Test
    public void commandShouldStopAsync() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getIntermediateResponse( agentUUID, command.getCommandUUID() );

        final AtomicInteger atomicInteger = new AtomicInteger();
        commandRunner.runCommandAsync( command, new CommandCallback() {

            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                atomicInteger.incrementAndGet();
                stop();
            }
        } );
        exec.execute( new Runnable() {

            public void run() {
                commandRunner.onResponse( response );
                commandRunner.onResponse( response );
            }
        } );

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
                  .pollDelay( 100, TimeUnit.MILLISECONDS ).until( new Callable<Boolean>() {

            public Boolean call() throws Exception {
                return atomicInteger.get() == 1;
            }
        } );
    }


    @Test
    public void commandShouldStop() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getIntermediateResponse( agentUUID, command.getCommandUUID() );

        final AtomicInteger atomicInteger = new AtomicInteger();
        exec.execute( new Runnable() {

            public void run() {
                try {
                    Thread.sleep( 100 );
                    commandRunner.onResponse( response );
                    commandRunner.onResponse( response );
                }
                catch ( InterruptedException ex ) {
                }
            }
        } );
        commandRunner.runCommand( command, new CommandCallback() {

            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                atomicInteger.incrementAndGet();
                stop();
            }
        } );

        assertEquals( 1, atomicInteger.get() );
    }


    @Test
    public void commandShouldStopEarlier() throws InterruptedException {
        Assume.assumeTrue( allTests );

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand( "ls", commandRunner, agentUUID, 1 );
        final Response response = MockUtils.getIntermediateResponse( agentUUID, command.getCommandUUID() );

        long ts = System.currentTimeMillis();
        exec.execute( new Runnable() {

            public void run() {
                try {
                    Thread.sleep( 100 );
                    commandRunner.onResponse( response );
                }
                catch ( InterruptedException ex ) {
                }
            }
        } );
        commandRunner.runCommand( command, new CommandCallback() {

            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                stop();
            }
        } );

        assertFalse( command.hasCompleted() );
        assertTrue( System.currentTimeMillis() - ts < 200 );
    }
}
