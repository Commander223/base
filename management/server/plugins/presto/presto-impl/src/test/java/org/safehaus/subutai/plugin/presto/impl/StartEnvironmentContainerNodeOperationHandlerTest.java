package org.safehaus.subutai.plugin.presto.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;


public class StartEnvironmentContainerNodeOperationHandlerTest
{

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new PrestoImplMock();
        handler = new StartNodeOperationHandler( mock, "test-cluster", "test-host" );
    }


    @Test
    public void testWithoutCluster()
    {

        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }


    @Test
    public void testFail()
    {
        mock.setClusterConfig( new PrestoClusterConfig() );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().contains( "not connected" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }
}