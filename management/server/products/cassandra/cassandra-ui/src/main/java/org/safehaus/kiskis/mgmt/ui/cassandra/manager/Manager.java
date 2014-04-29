/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.cassandra.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.cassandra.CassandraUI;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table listenNodesTable;
    private final Table rpcNodesTable;
    private final Table seedNodesTable;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        listenNodesTable = createTableTemplate("Listen nodes", 100);
        rpcNodesTable = createTableTemplate("RPC nodes", 100);
        seedNodesTable = createTableTemplate("Seed nodes", 100);
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing(true);

        Label clusterNameLabel = new Label("Select the cluster");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.UNITS_PIXELS);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (Config) event.getProperty().getValue();
                refreshUI();
            }
        });

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        controlsContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UUID trackID = CassandraUI.getCassandraManager().checkAllNodes(config.getClusterName());
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
            }

        });

        controlsContent.addComponent(checkAllBtn);

        Button startAllBtn = new Button("Start all");
        startAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UUID trackID = CassandraUI.getCassandraManager().startAllNodes(config.getClusterName());
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
//                checkNodesStatus(listenNodesTable);
            }

        });

        controlsContent.addComponent(startAllBtn);

        Button stopAllBtn = new Button("Stop all");
        stopAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UUID trackID = CassandraUI.getCassandraManager().stopAllNodes(config.getClusterName());
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
//                checkNodesStatus(listenNodesTable);
            }

        });

        controlsContent.addComponent(stopAllBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    MgmtApplication.showConfirmationDialog(
                            "Cluster destruction confirmation",
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = CassandraUI.getCassandraManager().uninstallCluster(config.getClusterName());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(destroyClusterBtn);


        content.addComponent(controlsContent);

        content.addComponent(listenNodesTable);
        content.addComponent(rpcNodesTable);
        content.addComponent(seedNodesTable);

    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, Set<Agent> agents) {

        table.removeAllItems();

        for (Iterator it = agents.iterator(); it.hasNext(); ) {
            final Agent agent = (Agent) it.next();

            final Button checkBtn = new Button("Check");
//            final Button startBtn = new Button("Start");
//            final Button stopBtn = new Button("Stop");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
//            stopBtn.setEnabled(false);
//            startBtn.setEnabled(false);
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                            checkBtn,
//                            startBtn,
//                            stopBtn,
                            progressIcon},
                    null
            );

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
//                    startBtn.setEnabled(false);
//                    stopBtn.setEnabled(false);

                    CassandraUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
//                                if (state == NodeState.RUNNING) {
//                                    stopBtn.setEnabled(true);
//                                } else if (state == NodeState.STOPPED) {
//                                    startBtn.setEnabled(true);
//                                }
                                show(state.toString());
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

//            startBtn.addListener(new Button.ClickListener() {
//
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//
//                    progressIcon.setVisible(true);
////                    startBtn.setEnabled(false);
////                    stopBtn.setEnabled(false);
//
//                    CassandraUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {
//
//                        public void onComplete(NodeState state) {
//                            synchronized (progressIcon) {
//                                if (state == NodeState.RUNNING) {
//                                    stopBtn.setEnabled(true);
//                                } else {
//                                    startBtn.setEnabled(true);
//                                }
//                                progressIcon.setVisible(false);
//                            }
//                        }
//                    }));
//
//                }
//            });
//
//            stopBtn.addListener(new Button.ClickListener() {
//
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//
//                    progressIcon.setVisible(true);
//                    startBtn.setEnabled(false);
//                    stopBtn.setEnabled(false);
//
//                    CassandraUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {
//
//                        public void onComplete(NodeState state) {
//                            synchronized (progressIcon) {
//                                if (state == NodeState.STOPPED) {
//                                    startBtn.setEnabled(true);
//                                } else {
//                                    stopBtn.setEnabled(true);
//                                }
//                                progressIcon.setVisible(false);
//                            }
//                        }
//                    }));
//                }
//            });

        }
    }

    private void populateTable(final Table table, final Agent agent) {

        table.removeAllItems();
        final Button checkBtn = new Button("Check");
        final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
        progressIcon.setVisible(false);

        final Object rowId = table.addItem(new Object[]{
                        agent.getHostname(),
                        checkBtn,
                        progressIcon},
                null
        );

        checkBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                progressIcon.setVisible(true);
                CassandraUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                    public void onComplete(NodeState state) {
                        synchronized (progressIcon) {
//                                if (state == NodeState.RUNNING) {
//                                    stopBtn.setEnabled(true);
//                                } else if (state == NodeState.STOPPED) {
//                                    startBtn.setEnabled(true);
//                                }
                            show(state.toString());
                            progressIcon.setVisible(false);
                        }
                    }
                }));
            }
        });


    }

    private void refreshUI() {
        if (config != null) {
            populateTable(listenNodesTable, config.getNodes());
            populateTable(rpcNodesTable, config.getNodes());
            populateTable(seedNodesTable, config.getSeedNodes());
        } else {
            listenNodesTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> info = CassandraUI.getCassandraManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (info != null && info.size() > 0) {
            for (Config mongoInfo : info) {
                clusterCombo.addItem(mongoInfo);
                clusterCombo.setItemCaption(mongoInfo,
                        mongoInfo.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config cassandraInfo : info) {
                    if (cassandraInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(cassandraInfo);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(info.iterator().next());
            }
        }
    }

    public static void checkNodesStatus(Table table) {
        for (Iterator it = table.getItemIds().iterator(); it.hasNext(); ) {
            int rowId = (Integer) it.next();
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
//        table.addContainerProperty("Start", Button.class, null);
//        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = CassandraUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        Window terminal = MgmtApplication.createTerminalWindow(Util.wrapAgentToSet(lxcAgent));
                        MgmtApplication.addCustomWindow(terminal);
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

}
