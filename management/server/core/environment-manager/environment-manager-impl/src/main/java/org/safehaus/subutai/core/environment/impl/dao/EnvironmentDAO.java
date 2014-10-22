package org.safehaus.subutai.core.environment.impl.dao;


import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class EnvironmentDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDAO.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected DbUtil dbUtil;


    public EnvironmentDAO( DataSource dataSource ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "DataSource is null" );
        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    protected void setupDb() throws SQLException
    {

        String sql1 = "create table if not exists blueprint (name varchar(100), info clob, PRIMARY KEY (name));";
        String sql2 =
                "create table if not exists process (source varchar(100), id uuid, info clob, PRIMARY KEY (source, "
                        + "id));";
        String sql3 = "create table if not exists environment (source varchar(100), id uuid, info clob, "
                + "PRIMARY KEY (source, id));";

        dbUtil.update( sql1 );
        dbUtil.update( sql2 );
        dbUtil.update( sql3 );
    }


    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        try
        {
            dbUtil.update( "merge into environment (source, id, info) values (? , ?, ?)", source,
                    UUID.fromString( key ), GSON.toJson( info ) );

            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return false;
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        List<T> list = new ArrayList<>();
        try
        {
            ResultSet rs = dbUtil.select( "select info from environment where source = ?", source );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    list.add( GSON.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return list;
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        try
        {

            ResultSet rs = dbUtil.select( "select info from environment where source = ? and id = ?", source,
                    UUID.fromString( key ) );
            if ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    return GSON.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );

        try
        {
            dbUtil.update( "delete from environment where source = ? and id = ?", source, UUID.fromString( key ) );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return false;
    }


    public void saveBlueprint( final EnvironmentBlueprint blueprint ) throws EnvironmentPersistenceException
    {
        try
        {
            String json = GSON.toJson( blueprint );
            dbUtil.update( "merge into blueprint (name, info) values (? , ?)", blueprint.getName(), json );
        }
        catch ( JsonParseException | SQLException e )
        {
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
    }


    public List<EnvironmentBlueprint> getBlueprints() throws EnvironmentPersistenceException
    {
        List<EnvironmentBlueprint> blueprints = new ArrayList<>();
        try
        {
            ResultSet rs = dbUtil.select( "select info from blueprint" );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    blueprints.add( GSON.fromJson( info, EnvironmentBlueprint.class ) );
                }
            }
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
        return blueprints;
    }


    public void deleteBlueprint( final String name ) throws EnvironmentPersistenceException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Blueprint name is null or empty" );
        try
        {
            dbUtil.update( "delete from blueprint where name = ?", name );
        }
        catch ( SQLException e )
        {
            throw new EnvironmentPersistenceException( e.getMessage() );
        }
    }
}
