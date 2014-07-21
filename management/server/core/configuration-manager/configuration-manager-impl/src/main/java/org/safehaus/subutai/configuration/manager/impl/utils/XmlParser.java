package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


/**
 * @author dilshat
 */
public class XmlParser implements ConfigParser {

    private final XMLConfiguration config;
    private final ExpressionEngine defaultEngine = new DefaultExpressionEngine();
    private final ExpressionEngine expressEngine = new XPathExpressionEngine();


    public XmlParser( String content ) throws ConfigurationException {
        config = new XMLConfiguration();
        config.load( new ByteArrayInputStream( content.getBytes() ) );
    }


    public Object getProperty( String propertyName ) {
        return config.getProperty( propertyName );
    }


    public void setProperty( String propertyName, Object propertyValue ) {
        config.setProperty( propertyName, propertyValue );
    }


    public String getStringProperty( String propertyName ) {
        return config.getString( propertyName );
    }


    public String getConventionalProperty( String propertyName ) {
        XMLConfiguration.setDefaultExpressionEngine( expressEngine );
        return config.getString( String.format( "property[name='%s']/value", propertyName ) );
    }


    public void addConventionalProperty( String propertyName, Object propertyValue ) {
        XMLConfiguration.setDefaultExpressionEngine( defaultEngine );
        config.addProperty( "property(-1).name", propertyName );
        config.addProperty( "property.value", propertyValue );
    }


    public void setConventionalProperty( String propertyName, Object propertyValue ) {
        XMLConfiguration.setDefaultExpressionEngine( expressEngine );
        config.setProperty( String.format( "property[name='%s']/value", propertyName ), propertyValue );
    }


    public String getXml() throws ConfigurationException {
        StringWriter str = new StringWriter();
        config.save( str );
        return str.toString();
    }


    @Override
    public JsonObject parserConfig( final String pathToConfig, final ConfigTypeEnum configTypeEnum ) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject( pathToConfig, configTypeEnum );

        Iterator<String> iterator = config.getKeys();
        List<JsonObject> fields = new ArrayList<>();
        while ( iterator.hasNext() ) {
            String key = iterator.next();
            String value = ( String ) config.getProperty( key );
            JsonObject field = configBuilder.buildFieldJsonObject( key, "", "", "", value );
            fields.add( field );
        }

        JsonObject njo = configBuilder.addJsonArrayToConfig( jo, fields );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return njo;
    }
}