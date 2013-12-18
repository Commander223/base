package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleServiceListener;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.Iterator;

@SuppressWarnings("serial")
public class MgmtApplication extends Application implements ModuleServiceListener {

    private ModuleServiceListener app;
    private final ModuleService moduleService;
    private final AgentManagerInterface agentManagerService;
    private Window window;
    private MgmtAgentManager mgmtAgentManager;

    public MgmtApplication(String title, ModuleService moduleService, AgentManagerInterface agentManagerService) {
        this.moduleService = moduleService;
        this.agentManagerService = agentManagerService;
        this.title = title;
    }
    private final String title;
    private TabSheet tabs;

    @Override
    public void init() {
        try {
            app = this;
            window = new Window(title);
            // Create the application data instance
            AppData sessionData = new AppData(this);

            // Register it as a listener in the application context
            getContext().addTransactionListener(sessionData);

            setMainWindow(window);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
            horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
            layout.addComponent(horizontalSplit);

            layout.setExpandRatio(horizontalSplit, 1);
            horizontalSplit.setSplitPosition(200, Sizeable.UNITS_PIXELS);

            Panel panel = new Panel();
            panel.addComponent(new MgmtAgentManager(agentManagerService));
            panel.setSizeFull();
            horizontalSplit.setFirstComponent(panel);

            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);

            for (Module module : moduleService.getModules()) {
                tabs.addTab(module.createComponent(), module.getName(), null);
            }
            horizontalSplit.setSecondComponent(tabs);

            getMainWindow().setContent(layout);
            setTheme("runo");

            moduleService.addListener(this);
            getMainWindow().addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    try {
                        if (moduleService != null) {
                            moduleService.removeListener(app);
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            });
            //
            final ProgressIndicator indicator
                    = new ProgressIndicator(new Float(0.0));
            indicator.setPollingInterval(3000);
            indicator.setWidth("1px");
            indicator.setHeight("1px");
            getMainWindow().addComponent(indicator);
            //            
        } catch (Exception ex) {
        } finally {
        }
    }

    @Override
    public void close() {
        super.close();
        System.out.println("Kiskis Management Vaadin UI: Application closing, removing module service listener");
    }

    @Override
    public void moduleRegistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module registered, adding tab");
        tabs.addTab(module.createComponent(), module.getName(), null);
    }

    @Override
    public void moduleUnregistered(ModuleService source, Module module) {
        System.out.println("Kiskis Management Vaadin UI: Module unregistered, removing tab");
        Iterator<Component> it = tabs.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (tabs.getTab(c).getCaption().equals(module.getName())) {
                tabs.removeComponent(c);
                return;
            }
        }

    }
}
