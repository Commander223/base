package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


@Ignore
public class AddEnvironmentContainerNodeOperationHandlerTest
{

    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new AddNodeOperationHandler( new AccumuloImplMock(), "test-cluster", "test-node", NodeType.Tracer );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }
}
