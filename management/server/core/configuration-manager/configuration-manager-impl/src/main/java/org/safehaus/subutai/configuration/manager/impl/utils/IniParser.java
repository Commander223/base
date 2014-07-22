package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.JsonObject;


/**
 * @author dilshat
 */
public class IniParser implements ConfigParser {

    private final PropertiesConfiguration config;


    public IniParser( String content ) throws ConfigurationException {
        config = new PropertiesConfiguration();
        config.load( new ByteArrayInputStream( content.getBytes() ) );
    }


    public PropertiesConfiguration getConfig() {
        return config;
    }


    public Object getProperty( String propertyName ) {
        return config.getString( propertyName );
    }


    public String getStringProperty( String propertyName ) {
        return config.getString( propertyName );
    }


    public void setProperty( String propertyName, Object propertyValue ) {
        config.setProperty( propertyName, propertyValue );
    }


    public void addProperty( String propertyName, Object propertyValue ) {
        config.addProperty( propertyName, propertyValue );
    }


    public String getIni() throws ConfigurationException {
        StringWriter str = new StringWriter();
        config.save( str );
        return str.toString();
    }


    @Override
    public JsonObject parserConfig( String pathToConfig, ConfigTypeEnum configTypeEnum ) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject( pathToConfig, configTypeEnum );

        Iterator<String> iterator = config.getKeys();
        List<JsonObject> fields = new ArrayList<>();
        while ( iterator.hasNext() ) {
            String key = iterator.next();
            String value = ( String ) config.getProperty( key );
            JsonObject field = configBuilder.buildFieldJsonObject( key.trim(), "", "", "", true, value.trim() );
            fields.add( field );
        }

        JsonObject njo = configBuilder.addJsonArrayToConfig( jo, fields );

        return njo;
    }
}