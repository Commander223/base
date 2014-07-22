package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


/**
 * Created by dilshat on 7/21/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<MongoImpl> {

    private final ProductOperation po;
    private final MongoClusterConfig config;


    public InstallOperationHandler( final MongoImpl manager, final MongoClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", MongoClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        ClusterSetupStrategy clusterSetupStrategy =
                manager.getClusterSetupStrategy( po, manager.getAgentManager(), manager.getCommandRunner(),
                        manager.getContainerManager(), config );

        try {
            MongoClusterConfig finalConfig = ( MongoClusterConfig ) clusterSetupStrategy.setup();

            if ( manager.getDbManager()
                        .saveInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), finalConfig ) ) {
                po.addLogDone( String.format( "Cluster %s setup successfully", clusterName ) );
            }
            else {
                po.addLogFailed( "Failed to save cluster information to database" );
            }
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
