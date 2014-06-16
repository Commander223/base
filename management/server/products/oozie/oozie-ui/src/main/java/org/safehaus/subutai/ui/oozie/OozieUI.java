/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.oozie.Config;
import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class OozieUI implements PortalModule {

    private static Oozie oozieManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hadoop hadoopManager;
    private static CommandRunner commandRunner;
    private static ExecutorService executor;

    public OozieUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Oozie oozieManager, CommandRunner commandRunner) {
        OozieUI.agentManager = agentManager;
        OozieUI.tracker = tracker;
        OozieUI.hadoopManager = hadoopManager;
        OozieUI.oozieManager = oozieManager;
        OozieUI.commandRunner = commandRunner;
    }

    public static Oozie getOozieManager() {
        return oozieManager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        oozieManager = null;
        agentManager = null;
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }

    @Override
    public String getId() {
        return Config.PRODUCT_KEY;
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new OozieForm();
    }

}
