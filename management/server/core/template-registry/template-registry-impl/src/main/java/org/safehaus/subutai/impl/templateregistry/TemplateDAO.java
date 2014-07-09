package org.safehaus.subutai.impl.templateregistry;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.templateregistry.Template;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Provides Data Access API for templates
 */
public class TemplateDAO {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private DbManager dbManager;


    public TemplateDAO( final DbManager dbManager ) {
        Preconditions.checkNotNull( dbManager, "DB Manager is null" );
        this.dbManager = dbManager;
    }


    public List<Template> getAllTemplates() {
        List<Template> list = new ArrayList<>();
        try {
            ResultSet rs = dbManager.executeQuery( "select info from template_registry_info" );
            if ( rs != null ) {
                for ( Row row : rs ) {
                    String info = row.getString( "info" );
                    Template template = gson.fromJson( info, Template.class );
                    if ( template != null ) {

                        list.add( template );
                    }
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new RuntimeException( String.format( "Error in getAllTemplates %s", ex ) );
        }
        return list;
    }


    public List<Template> geChildTemplates( String parentTemplateName, String lxcArch ) {
        List<Template> list = new ArrayList<>();
        try {
            ResultSet rs = dbManager.executeQuery( "select info from template_registry_info where parent = ?",
                    String.format( "%s-%s", parentTemplateName, lxcArch ) );
            if ( rs != null ) {
                for ( Row row : rs ) {
                    String info = row.getString( "info" );
                    Template template = gson.fromJson( info, Template.class );
                    if ( template != null ) {

                        list.add( template );
                    }
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new RuntimeException( String.format( "Error in getAllTemplates %s", ex ) );
        }
        return list;
    }


    public Template getTemplateByName( String templateName, String lxcArch ) {
        try {
            ResultSet rs = dbManager.executeQuery( "select info from template_registry_info where template = ?",
                    String.format( "%s-%s", templateName, lxcArch ) );
            if ( rs != null ) {
                Row row = rs.one();
                if ( row != null ) {
                    String info = row.getString( "info" );

                    return gson.fromJson( info, Template.class );
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new RuntimeException( String.format( "Error in getTemplateByName %s", ex ) );
        }
        return null;
    }


    public boolean saveTemplate( Template template ) {
        return dbManager.executeUpdate( "insert into template_registry_info(template, parent, info) values(?,?,?)",
                String.format( "%s-%s", template.getTemplateName(), template.getLxcArch() ),
                Strings.isNullOrEmpty( template.getParentTemplateName() ) ? null :
                String.format( "%s-%s", template.getParentTemplateName(), template.getLxcArch() ),
                gson.toJson( template ) );
    }


    public boolean removeTemplate( Template template ) {
        return dbManager.executeUpdate( "delete from template_registry_info where template = ?",
                String.format( "%s-%s", template.getTemplateName(), template.getLxcArch() ) );
    }
}
