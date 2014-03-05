package org.safehaus.kiskis.mgmt.server.ui.modules.client;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.SomeApi;

public class ClientModule implements Module {

    public static final String MODULE_NAME = "Client";
    private static final Logger LOG = Logger.getLogger(ClientModule.class.getName());
    private SomeApi someApi;

    public SomeApi getSomeApi() {
        return someApi;
    }

    public void setSomeApi(SomeApi someApi) {
        this.someApi = someApi;
    }

    public static class ModuleComponent extends CustomComponent {

        public ModuleComponent(final SomeApi someApi) {

            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

//            TabSheet sheet = new TabSheet();
//            sheet.setStyleName(Runo.TABSHEET_SMALL);
//            sheet.setSizeFull();
            final TextArea t = new TextArea();
            t.setSizeFull();
            Button b = new Button("Get System Logs");
            b.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    String hello = someApi.sayHello("logs");
                    t.setValue(hello);

                }
            });

            verticalLayout.addComponent(b);
            verticalLayout.addComponent(t);

            setCompositionRoot(verticalLayout);
        }

        public Iterable<Agent> getLxcList() {
            return MgmtApplication.getSelectedAgents();
        }

    }

    @Override
    public String getName() {
        return ClientModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(someApi);
    }

}
