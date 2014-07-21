package org.safehaus.subutai.cli.commands;


import java.util.UUID;

import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "mongo", name = "uninstall-cluster", description = "Command to uninstall MongoDB cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Mongo mongoManager;
    private Tracker tracker;


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void setMongoManager( Mongo mongoManager ) {
        this.mongoManager = mongoManager;
    }


    public Mongo getMongoManager() {
        return mongoManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;


    protected Object doExecute() {
        UUID uuid = mongoManager.uninstallCluster( clusterName );
        int logSize = 0;
        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( MongoClusterConfig.PRODUCT_KEY, uuid );
            if ( po != null ) {
                if ( logSize != po.getLog().length() ) {
                    System.out.print( po.getLog().substring( logSize, po.getLog().length() ) );
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    break;
                }
            }
            else {
                System.out.println( "Product operation not found. Check logs" );
                break;
            }
            try {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex ) {
                break;
            }
        }
        return null;
    }
}
