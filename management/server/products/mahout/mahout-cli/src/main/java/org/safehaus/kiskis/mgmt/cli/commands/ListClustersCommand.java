package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.mahout.Config;
import org.safehaus.kiskis.mgmt.api.mahout.Mahout;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "mahout", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Mahout mahoutManager;

    public Mahout getMahoutManager() {
        return mahoutManager;
    }

    public void setMahoutManager(Mahout mahoutManager) {
        this.mahoutManager = mahoutManager;
    }

    protected Object doExecute() {
        List<Config> configList = mahoutManager.getClusters();
        if (!configList.isEmpty())
            for (Config config : configList) {
                System.out.println(config.getClusterName());
            }
        else System.out.println("No Mahout cluster");

        return null;
    }
}
