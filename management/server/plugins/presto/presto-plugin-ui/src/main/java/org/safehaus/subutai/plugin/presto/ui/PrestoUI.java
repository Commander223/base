package org.safehaus.subutai.plugin.presto.ui;

import com.vaadin.ui.Component;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

public class PrestoUI implements PortalModule {

    public static final String MODULE_IMAGE = "presto.png";

    private static Presto prestoManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hadoop hadoopManager;
    private static CommandRunner commandRunner;
    private static ExecutorService executor;

    public PrestoUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Presto prestoManager, CommandRunner commandRunner) {
        PrestoUI.agentManager = agentManager;
        PrestoUI.tracker = tracker;
        PrestoUI.hadoopManager = hadoopManager;
        PrestoUI.prestoManager = prestoManager;
        PrestoUI.commandRunner = commandRunner;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Presto getPrestoManager() {
        return prestoManager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        prestoManager = null;
        agentManager = null;
        hadoopManager = null;
        tracker = null;
        executor.shutdown();
    }

    @Override
    public String getId() {
        return PrestoClusterConfig.PRODUCT_KEY;
    }

    @Override
    public String getName() {
        return PrestoClusterConfig.PRODUCT_KEY;
    }

    @Override
    public File getImage() {
        return FileUtil.getFile(PrestoUI.MODULE_IMAGE, this);
    }

    @Override
    public Component createComponent() {
        return new PrestoForm();
    }

}
