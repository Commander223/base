package org.safehaus.kiskis.mgmt.server.ui.modules.lxc;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone.Cloner;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage.Manager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class LxcModule implements Module {

    public static final String MODULE_NAME = "LXC";
    private TaskRunner taskRunner;
    private AgentManager agentManager;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public static class ModuleComponent extends CustomComponent {

        public ModuleComponent(TaskRunner taskRunner, AgentManager agentManager) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet commandsSheet = new TabSheet();
            commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
            commandsSheet.setSizeFull();

            commandsSheet.addTab(new Cloner(taskRunner), "Clone");
            commandsSheet.addTab(new Manager(taskRunner, agentManager), "Manage");

            verticalLayout.addComponent(commandsSheet);

            setCompositionRoot(verticalLayout);

        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(taskRunner, agentManager);
    }

}
