package org.safehaus.subutai.ui.templateregistry;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import com.vaadin.ui.Component;


public class TemplateRegistryUI implements PortalModule {

    public static final String MODULE_IMAGE = "tree.png";
    public static final String MODULE_NAME = "Registry";
    private static ExecutorService executor;
    private AgentManager agentManager;
    private TemplateRegistryManager registryManager;


    public static ExecutorService getExecutor() {
        return executor;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setRegistryManager( final TemplateRegistryManager registryManager ) {
        this.registryManager = registryManager;
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
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new TemplateRegistryForm( agentManager, registryManager );
    }
}
