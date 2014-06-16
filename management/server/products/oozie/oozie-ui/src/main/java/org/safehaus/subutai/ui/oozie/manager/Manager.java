/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.oozie.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.oozie.OozieUI;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table serverTable;
    private final Table clientsTable;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.Unit.PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.Unit.PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.Unit.PERCENTAGE);
        content.setHeight(100, Sizeable.Unit.PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        serverTable = createServerTableTemplate("Server", 200);
        clientsTable = createClientsTableTemplate("Clients", 200);
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing(true);

        Label clusterNameLabel = new Label("Select the cluster");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
        clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (Config) event.getProperty().getValue();
                refreshUI();
            }
        });

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                refreshClustersInfo();
            }
        });

        controlsContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                checkNodesStatus(serverTable);
            }
        });

        // TODO add restart hadoop button

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (config != null) {
                    ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No");
                    alert.getOk().addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            UUID trackID = OozieUI.getOozieManager().uninstallCluster(config.getClusterName());
                            ProgressWindow window = new ProgressWindow(OozieUI.getExecutor(), OozieUI.getTracker(), trackID, Config.PRODUCT_KEY);
                            window.getWindow().addCloseListener(new Window.CloseListener() {
                                @Override
                                public void windowClose(Window.CloseEvent closeEvent) {
                                    refreshClustersInfo();
                                }
                            });
                            contentRoot.getUI().addWindow(window.getWindow());
                        }
                    });

                    contentRoot.getUI().addWindow(alert.getAlert());
                } else {
                    show("Please, select cluster");
                }
            }
        });

        controlsContent.addComponent(destroyClusterBtn);
        content.addComponent(controlsContent);
        content.addComponent(serverTable);
        content.addComponent(clientsTable);

    }

    public static void checkNodesStatus(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        Notification.show(notification);
    }

    private void populateServerTable(final Table table, final Agent agent) {

        table.removeAllItems();
        final Button checkBtn = new Button("Check");
        final Button startBtn = new Button("Start");
        final Button stopBtn = new Button("Stop");
        final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
        progressIcon.setVisible(false);

        final Object rowId = table.addItem(new Object[]{
                        agent.getHostname(),
                        progressIcon},
                null
        );

        checkBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                progressIcon.setVisible(true);

                UUID trackID = OozieUI.getOozieManager().checkServerStatus(config);
                ProgressWindow window = new ProgressWindow(OozieUI.getExecutor(), OozieUI.getTracker(), trackID, Config.PRODUCT_KEY);
                window.getWindow().addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent closeEvent) {
                        refreshClustersInfo();
                    }
                });
                contentRoot.getUI().addWindow(window.getWindow());
            }
        });

        startBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

                UUID trackID = OozieUI.getOozieManager().startServer(config);
                ProgressWindow window = new ProgressWindow(OozieUI.getExecutor(), OozieUI.getTracker(), trackID, Config.PRODUCT_KEY);
                window.getWindow().addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent closeEvent) {
                        refreshClustersInfo();
                    }
                });
                contentRoot.getUI().addWindow(window.getWindow());
            }
        });

        stopBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                progressIcon.setVisible(true);

                UUID trackID = OozieUI.getOozieManager().stopServer(config);
                ProgressWindow window = new ProgressWindow(OozieUI.getExecutor(), OozieUI.getTracker(), trackID, Config.PRODUCT_KEY);
                window.getWindow().addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent closeEvent) {
                        refreshClustersInfo();
                    }
                });
                contentRoot.getUI().addWindow(window.getWindow());
            }
        });

    }

    private void populateClientsTable(final Table table, Set<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                    },
                    null
            );
        }
    }

    private void refreshUI() {
        if (config != null) {
            populateServerTable(serverTable, config.getServer());
            populateClientsTable(clientsTable, config.getClients());
        } else {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> info = OozieUI.getOozieManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (info != null && info.size() > 0) {
            for (Config oozieConfig : info) {
                clusterCombo.addItem(oozieConfig);
                clusterCombo.setItemCaption(oozieConfig,
                        oozieConfig.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config oozieInfo : info) {
                    if (oozieInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(oozieInfo);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(info.iterator().next());
            }
        }
    }

    private Table createServerTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.Unit.PERCENTAGE);
        table.setHeight(size, Sizeable.Unit.PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), OozieUI.getExecutor(), OozieUI.getCommandRunner(), OozieUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

    private Table createClientsTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.setWidth(100, Sizeable.Unit.PERCENTAGE);
        table.setHeight(size, Sizeable.Unit.PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), OozieUI.getExecutor(), OozieUI.getCommandRunner(), OozieUI.getAgentManager());
                        contentRoot.getUI().addWindow(terminal.getWindow());
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

}
