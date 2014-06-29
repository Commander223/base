/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.lxcmanager;


import org.safehaus.subutai.impl.strategy.DefaultLxcPlacementStrategy;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.lxcmanager.LxcState;
import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.monitor.Metric;
import org.safehaus.subutai.api.monitor.Monitor;
import org.safehaus.subutai.shared.protocol.Agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for LxcManagerImpl class
 */
public class LxcManagerImplUT {

    private LxcManager lxcManager;


    @Before
    public void setUpMethod() {
        Monitor monitor = mock( Monitor.class );
        when( monitor.getData( any( String.class ), any( Metric.class ), any( Date.class ), any( Date.class ) ) )
                .thenReturn( Collections.EMPTY_MAP );
        lxcManager = new LxcManagerImpl( new AgentManagerFake(), MockUtils.getAutoCommandRunner(), monitor );
        ( ( LxcManagerImpl ) lxcManager ).init();
    }


    @After
    public void tearDownMethod() {

        ( ( LxcManagerImpl ) lxcManager ).destroy();
    }


    @Test
    public void testCloneLxcOnHost() {

        boolean result =
                lxcManager.cloneLxcOnHost( MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname() );

        assertTrue( result );
    }


    @Test
    public void testGetLxcOnPhysicalServers() {

        Map<String, EnumMap<LxcState, List<String>>> result = lxcManager.getLxcOnPhysicalServers();

        assertFalse( result.isEmpty() );
    }


    @Test
    public void testGetPhysicalServerMetrics() {

        Map<Agent, ServerMetric> result = lxcManager.getPhysicalServerMetrics();

        assertFalse( result.isEmpty() );
    }


    @Test
    public void testGetPhysicalServersWithLxcSlots() {

        Map<Agent, Integer> result = lxcManager.getPhysicalServersWithLxcSlots();

        assertTrue( result.entrySet().iterator().next().getValue() > 0 );
    }


    @Test
    public void testStartLxcOnHost() {

        boolean result =
                lxcManager.startLxcOnHost( MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname() );

        assertTrue( result );
    }


    @Test
    public void testStopLxcOnHost() {
        LxcManager lxcManager = new LxcManagerImpl( new AgentManagerFake(),
                MockUtils.getHardCodedCommandRunner( true, true, 0, "STOPPED", null ), mock( Monitor.class ) );

        boolean result =
                lxcManager.stopLxcOnHost( MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname() );

        assertTrue( result );
    }


    @Test
    public void testDestroyLxcOnHost() {

        boolean result =
                lxcManager.destroyLxcOnHost( MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname() );

        assertTrue( result );
    }


    @Test
    public void testCloneNStartLxcOnHost() {

        boolean result =
                lxcManager.cloneNStartLxcOnHost( MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname() );

        assertTrue( result );
    }


    @Test
    public void testDestroyLxcs() {

        boolean error = false;
        try {
            Set<String> lxcHostnames = new HashSet<>();
            lxcHostnames.add( MockUtils.getLxcAgent().getHostname() );
            lxcManager.destroyLxcsByHostname( lxcHostnames );
        }
        catch ( LxcDestroyException ex ) {
            error = true;
        }
        assertFalse( error );
    }


    @Test
    public void testCreateLxcsByStrategy() throws LxcCreateException {

        Map<String, Map<Agent, Set<Agent>>> agentMap =
                lxcManager.createLxcsByStrategy( new DefaultLxcPlacementStrategy( 1 ) );

        assertFalse( agentMap.isEmpty() );
    }


    @Test
    public void testCreateLxcs() throws LxcCreateException {

        Map<Agent, Set<Agent>> agentMap = lxcManager.createLxcs( 1 );

        assertFalse( agentMap.isEmpty() );
    }
}
