package org.safehaus.kiskis.mgmt.impl.hive.query;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hive.query.HiveQuery;
import org.safehaus.subutai.api.tracker.Tracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HiveQueryBase implements HiveQuery {

    protected static CommandRunner commandRunner;
    protected static DbManager dbManager;
    protected static Tracker tracker;
    protected static AgentManager agentManager;

    protected ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        HiveQueryBase.commandRunner = commandRunner;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        HiveQueryBase.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        HiveQueryImpl.tracker = tracker;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HiveQueryBase.agentManager = agentManager;
    }
}
