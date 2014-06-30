/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.mongodb;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.api.mongodb.Mongo;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class MongoUI implements PortalModule {

	public static final String MODULE_IMAGE = "mongodb.png";

	private static Mongo mongoManager;
	private static AgentManager agentManager;
	private static CommandRunner commandRunner;
	private static ExecutorService executor;
	private static Tracker tracker;

	public MongoUI(AgentManager agentManager, Mongo mongoManager, Tracker tracker, CommandRunner commandRunner) {
		MongoUI.agentManager = agentManager;
		MongoUI.mongoManager = mongoManager;
		MongoUI.tracker = tracker;
		MongoUI.commandRunner = commandRunner;
	}

	public static Tracker getTracker() {
		return tracker;
	}

	public static Mongo getMongoManager() {
		return mongoManager;
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
		tracker = null;
		mongoManager = null;
		agentManager = null;
		executor.shutdown();
	}

	@Override
	public String getId() {
		return Config.PRODUCT_KEY;
	}

	public String getName() {
		return Config.PRODUCT_KEY;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(MongoUI.MODULE_IMAGE, this);
	}

	public Component createComponent() {
		return new MongoForm();
	}

}
