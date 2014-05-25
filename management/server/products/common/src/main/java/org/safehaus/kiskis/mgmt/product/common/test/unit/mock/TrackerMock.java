package org.safehaus.kiskis.mgmt.product.common.test.unit.mock;


import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;


public class TrackerMock implements Tracker {
    @Override
    public ProductOperationView getProductOperation( String source, UUID operationTrackId ) {
        return null;
    }


    @Override
    public ProductOperation createProductOperation( String source, String description ) {
        return new ProductOperationMock();
    }


    @Override
    public List<ProductOperationView> getProductOperations( String source, Date fromDate, Date toDate, int limit ) {
        return null;
    }


    @Override
    public List<String> getProductOperationSources() {
        return null;
    }


    @Override
    public void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs ) {

    }
}
