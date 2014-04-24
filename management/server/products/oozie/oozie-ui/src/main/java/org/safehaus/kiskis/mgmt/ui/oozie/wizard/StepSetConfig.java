/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.oozie.wizard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author dilshat
 */
public class StepSetConfig extends Panel {

    public StepSetConfig(final Wizard wizard) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Oozie Installation Wizard");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);

        Label configServersLabel = new Label("<strong>Oozie Server</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        vl.addComponent(configServersLabel);

        final Label server = new Label("Server");
        vl.addComponent(server);

        final ComboBox cbServers = new ComboBox();
        cbServers.setMultiSelect(false);
        for (Agent agent : wizard.getConfig().getHadoopNodes()) {
            cbServers.addItem(agent);
            cbServers.setItemCaption(agent, agent.getHostname());
            cbServers.setNullSelectionAllowed(false);
        }

        vl.addComponent(cbServers);

        final TwinColSelect selectClients = new TwinColSelect("", new ArrayList<Agent>());
        selectClients.setItemCaptionPropertyId("hostname");
        selectClients.setRows(7);
        selectClients.setNullSelectionAllowed(true);
        selectClients.setMultiSelect(true);
        selectClients.setImmediate(true);
        selectClients.setLeftColumnCaption("Available nodes");
        selectClients.setRightColumnCaption("Client nodes");
        selectClients.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        selectClients.setRequired(true);
        selectClients.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, wizard.getConfig().getHadoopNodes())
        );

        vl.addComponent(selectClients);

        grid.addComponent(vl, 3, 0, 9, 9);
        grid.setComponentAlignment(vl, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.getConfig().setServer((Agent) cbServers.getValue());
                wizard.getConfig().setClients((Set<Agent>) selectClients.getValue());

                if (Util.isCollectionEmpty(wizard.getConfig().getClients())) {
                    show("Please select nodes for Oozie clients");
                } else if (wizard.getConfig().getServer() == null) {
                    show("Please select node for Oozie server");
                } else {
                    if (wizard.getConfig().getClients().contains(wizard.getConfig().getServer())) {
                        show("Oozie server and client can not be installed on the same host");
                    } else {
                        wizard.next();
                    }
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

//        selectClients.setContainerDataSource(new BeanItemContainer<Agent>(Agent.class, wizard.getConfig().getClients()));

        //set values if this is a second visit
//        server.setValue(wizard.getConfig().getServer().getHostname());
//        selectClients.setValue(wizard.getConfig().getClients());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
