package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.api.sqoop.DataSourceType;
import org.safehaus.subutai.api.sqoop.setting.ImportParameter;
import org.safehaus.subutai.api.sqoop.setting.ImportSetting;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

public class ImportPanel extends ImportExportBase {

    DataSourceType type;
    CheckBox chkImportAllTables = new CheckBox("Import all tables");
    AbstractTextField hbaseTableNameField = UIUtil.getTextField("Table name:", 300);
    AbstractTextField hbaseColumnFamilyField = UIUtil.getTextField("Column family:", 300);
    AbstractTextField hiveDatabaseField = UIUtil.getTextField("Database:", 300);
    AbstractTextField hiveTableNameField = UIUtil.getTextField("Table name:", 300);

    public ImportPanel() {
        init();
    }

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
        init();
    }

    @Override
    final void init() {
        removeAllActionHandlers();
        removeAllComponents();

        if(type == null) {
            VerticalLayout layout = new VerticalLayout();
            layout.addComponent(UIUtil.getLabel("Select data source type<br/>", 200));
            for(final DataSourceType dst : DataSourceType.values()) {
                Button btn = UIUtil.getButton(dst.toString(), 100);
                btn.addListener(new Button.ClickListener() {

                    public void buttonClick(Button.ClickEvent event) {
                        setType(dst);
                    }
                });
                layout.addComponent(btn);
            }
            Button btn = UIUtil.getButton("Cancel", 100);
            btn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    detachFromParent();
                }
            });
            layout.addComponent(UIUtil.getLabel("</br>", 100));
            layout.addComponent(btn);
            addComponent(layout);
            return;
        }
        if(agent == null) {
            addComponent(UIUtil.getLabel("<h1>No node selected</h1>", 200));
            return;
        }

        super.init();
        chkImportAllTables.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent e) {
                String v = e.getProperty().getValue().toString();
                tableField.setEnabled(!Boolean.parseBoolean(v));
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(UIUtil.getButton("Import", 120,
                new Button.ClickListener() {

                    public void buttonClick(Button.ClickEvent event) {
                        clearLogMessages();
                        if(!checkFields()) return;
                        setFieldsEnabled(false);
                        ImportSetting sett = makeSettings();
                        final UUID trackId = SqoopUI.getManager().importData(sett);

                        OperationWatcher watcher = new OperationWatcher(trackId);
                        watcher.setCallback(new OperationCallback() {

                            public void onComplete() {
                                setFieldsEnabled(true);
                            }
                        });
                        SqoopUI.getExecutor().execute(watcher);
                    }

                }));
        buttons.addComponent(UIUtil.getButton("Back", 120, new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                reset();
                setType(null);
            }
        }));

        List<Component> ls = new ArrayList<Component>();
        ls.add(UIUtil.getLabel("<h1>Sqoop Import</h1>", 100, UNITS_PERCENTAGE));
        ls.add(UIUtil.getLabel("<h1>" + type.toString() + "</h1>", 200));
        ls.add(connStringField);
        ls.add(tableField);
        ls.add(usernameField);
        ls.add(passwordField);

        switch(type) {
            case HDFS:
                ls.add(3, chkImportAllTables);
                this.fields.add(chkImportAllTables);
                break;
            case HBASE:
                ls.add(UIUtil.getLabel("<b>HBase parameters</b>", 200));
                ls.add(hbaseTableNameField);
                ls.add(hbaseColumnFamilyField);
                this.fields.add(hbaseTableNameField);
                this.fields.add(hbaseColumnFamilyField);
                break;
            case HIVE:
                ls.add(3, chkImportAllTables);
                ls.add(UIUtil.getLabel("<b>Hive parameters</b>", 200));
                ls.add(hiveDatabaseField);
                ls.add(hiveTableNameField);
                this.fields.add(chkImportAllTables);
                this.fields.add(hiveDatabaseField);
                this.fields.add(hiveTableNameField);
                break;
            default:
                throw new AssertionError(type.name());
        }
        ls.add(buttons);

        addComponents(ls);
    }

    @Override
    public ImportSetting makeSettings() {
        ImportSetting s = new ImportSetting();
        s.setType(type);
        s.setClusterName(clusterName);
        s.setHostname(agent.getHostname());
        s.setConnectionString(connStringField.getValue().toString());
        s.setTableName(tableField.getValue().toString());
        s.setUsername(usernameField.getValue().toString());
        s.setPassword(passwordField.getValue().toString());
        switch(type) {
            case HDFS:
                s.addParameter(ImportParameter.IMPORT_ALL_TABLES,
                        chkImportAllTables.getValue());
                break;
            case HBASE:
                s.addParameter(ImportParameter.DATASOURCE_TABLE_NAME,
                        hbaseTableNameField.getValue());
                s.addParameter(ImportParameter.DATASOURCE_COLUMN_FAMILY,
                        hbaseColumnFamilyField.getValue());
                break;
            case HIVE:
                s.addParameter(ImportParameter.DATASOURCE_DATABASE,
                        hiveDatabaseField.getValue());
                s.addParameter(ImportParameter.DATASOURCE_TABLE_NAME,
                        hiveTableNameField.getValue());
                break;
            default:
                throw new AssertionError(type.name());
        }
        return s;
    }

    @Override
    boolean checkFields() {
        if(super.checkFields()) {
            switch(type) {
                case HDFS:
                    if(!isChecked(chkImportAllTables))
                        if(!hasValue(tableField, "Table name not specified"))
                            return false;
                    break;
                case HBASE:
                    if(!hasValue(hbaseTableNameField, "HBase table name not specified"))
                        return false;
                    if(!hasValue(hbaseColumnFamilyField, "HBase column family not specified"))
                        return false;
                    break;
                case HIVE:
                    if(!isChecked(chkImportAllTables))
                        if(!hasValue(tableField, "Table name not specified"))
                            return false;
                    if(!hasValue(hiveDatabaseField, "Hive database not specified"))
                        return false;
                    if(!hasValue(hiveTableNameField, "Hive table name not specified"))
                        return false;
                    break;
                default:
                    throw new AssertionError(type.name());
            }
            return true;
        }
        return false;
    }

    private boolean isChecked(CheckBox chb) {
        Object v = chb.getValue();
        return v != null ? Boolean.parseBoolean(v.toString()) : false;
    }

}
