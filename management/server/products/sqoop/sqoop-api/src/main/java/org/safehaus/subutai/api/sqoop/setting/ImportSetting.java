package org.safehaus.subutai.api.sqoop.setting;

import java.util.EnumMap;
import java.util.Map;
import org.safehaus.subutai.api.sqoop.DataSourceType;

public class ImportSetting extends CommonSetting {

    DataSourceType type;
    Map<ImportParameter, Object> parameters;

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public void addParameter(ImportParameter param, Object value) {
        if(parameters == null)
            parameters = new EnumMap<ImportParameter, Object>(ImportParameter.class);
        parameters.put(param, value);
    }

    public Object getParameter(ImportParameter param) {
        return parameters != null ? parameters.get(param) : null;
    }

    public String getStringParameter(ImportParameter param) {
        Object p = getParameter(param);
        return p != null ? p.toString() : null;
    }

    public boolean getBooleanParameter(ImportParameter param) {
        Object p = getParameter(param);
        if(p == null) return false;
        if(p instanceof Boolean) return ((Boolean)p).booleanValue();
        return Boolean.parseBoolean(p.toString());
    }

    @Override
    public String toString() {
        return super.toString() + ", type=" + type + ", parameters="
                + (parameters != null ? parameters.size() : 0);
    }

}
