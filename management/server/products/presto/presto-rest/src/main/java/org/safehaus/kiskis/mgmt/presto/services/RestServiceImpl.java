package org.safehaus.kiskis.mgmt.presto.services;

import org.safehaus.kiskis.mgmt.api.presto.Presto;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Presto prestoManager;

    public Presto getPrestoManager() {
        return prestoManager;
    }

    public void setPrestoManager(Presto prestoManager) {
        this.prestoManager = prestoManager;
    }

    @Override
    public String installCluster(String clusterName) {
        return null;
    }

    @Override
    public String uninstallCluster(String clusterName) {
        return null;
    }
}
