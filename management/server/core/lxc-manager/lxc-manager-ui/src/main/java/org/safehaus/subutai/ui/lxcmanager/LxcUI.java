package org.safehaus.subutai.ui.lxcmanager;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LxcUI implements PortalModule {

	public static final String MODULE_IMAGE = "lxc.jpg";
	public static final String MODULE_NAME = "LXC";
	private static ExecutorService executor;
	private AgentManager agentManager;
	private LxcManager lxcManager;


	public static ExecutorService getExecutor() {
		return executor;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	@Override
	public String getId() {
		return MODULE_NAME;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public File getImage() {
		return FileUtil.getFile(LxcUI.MODULE_IMAGE, this);
	}


	@Override
	public Component createComponent() {
		return new LxcForm(agentManager, lxcManager);
	}
}
