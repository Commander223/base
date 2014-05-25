/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.impl.spark.handler.*;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SparkImpl implements Spark {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;

    public SparkImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;

        Commands.init(commandRunner);
    }

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

    public UUID installCluster(final Config config) {

        Preconditions.checkNotNull(config, "Configuration is null");

        AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID uninstallCluster(final String clusterName) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler(this, clusterName);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID addSlaveNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new AddSlaveNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID destroySlaveNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new DestroySlaveNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID changeMasterNode(final String clusterName, final String newMasterHostname, final boolean keepSlave) {

        AbstractOperationHandler operationHandler = new ChangeMasterNodeOperationHandler(this, clusterName, newMasterHostname, keepSlave);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID startNode(final String clusterName, final String lxcHostname, final boolean master) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler(this, clusterName, lxcHostname, master);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();

    }

    public UUID stopNode(final String clusterName, final String lxcHostname, final boolean master) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler(this, clusterName, lxcHostname, master);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostname) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler(this, clusterName, lxcHostname);

        executor.execute(operationHandler);

        return operationHandler.getTrackerId();
    }

}
