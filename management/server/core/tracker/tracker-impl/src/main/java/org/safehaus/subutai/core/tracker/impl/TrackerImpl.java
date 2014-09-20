/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.tracker.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.tracker.api.Tracker;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * This is an implementation of Tracker
 */
public class TrackerImpl implements Tracker
{

    /**
     * Used to serialize/deserialize product operation to/from json format
     */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger( TrackerImpl.class.getName() );

    /**
     * reference to dbmanager
     */
    private DbManager dbManager;


    public void setDbManager( DbManager dbManager )
    {
        Preconditions.checkNotNull( dbManager, "Db manager is null" );

        this.dbManager = dbManager;
    }


    /**
     * Get view of product operation by operation id
     *
     * @param source - source of product operation, usually this is a module name
     * @param operationTrackId - id of operation
     *
     * @return - product operation view
     */
    public ProductOperationView getProductOperation( String source, UUID operationTrackId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( operationTrackId, "Operation track id is null" );

        try
        {
            ResultSet rs = dbManager.executeQuery2( "select info from product_operation where source = ? and id = ?",
                    source.toLowerCase(), operationTrackId );
            if ( rs != null )
            {
                Row row = rs.one();
                if ( row != null )
                {
                    String info = row.getString( "info" );
                    ProductOperationImpl po = gson.fromJson( info, ProductOperationImpl.class );
                    if ( po != null )
                    {
                        return new ProductOperationViewImpl( po );
                    }
                }
            }
        }
        catch ( DBException | JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in getProductOperation", ex );
        }
        return null;
    }


    /**
     * Saves product operation o DB
     *
     * @param source - source of product operation, usually this is a module
     * @param po - product operation
     *
     * @return - true if all went well, false otherwise
     */
    boolean saveProductOperation( String source, ProductOperationImpl po )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        try
        {
            dbManager.executeUpdate2( "insert into product_operation(source,id,info) values(?,?,?)",
                    source.toLowerCase(), po.getId(), gson.toJson( po ) );
            return true;
        }
        catch ( DBException e )
        {
            LOG.log( Level.SEVERE, "Error in saveProductOperation", e );
        }

        return false;
    }


    /**
     * Creates product operation and save it to DB
     *
     * @param source - source of product operation, usually this is a module
     * @param description - description of operation
     *
     * @return - returns created product operation
     */
    public ProductOperation createProductOperation( String source, String description )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( !Strings.isNullOrEmpty( description ), "Description is null or empty" );

        ProductOperationImpl po = new ProductOperationImpl( source.toLowerCase(), description, this );
        if ( saveProductOperation( source, po ) )
        {
            return po;
        }
        return null;
    }


    /**
     * Returns list of product operations (views) filtering them by date interval
     *
     * @param source - source of product operation, usually this is a module
     * @param fromDate - beginning date of filter
     * @param toDate - ending date of filter
     * @param limit - limit of records to return
     *
     * @return - list of product operation views
     */
    public List<ProductOperationView> getProductOperations( String source, Date fromDate, Date toDate, int limit )
    {
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( fromDate, "From Date is null" );
        Preconditions.checkNotNull( toDate, "To Date is null" );

        List<ProductOperationView> list = new ArrayList<>();
        try
        {
            ResultSet rs = dbManager.executeQuery2(
                    "select info from product_operation where source = ?" + " and id >= maxTimeuuid(?)"
                            + " and id <= minTimeuuid(?)" + " order by id desc limit ?", source.toLowerCase(), fromDate,
                    toDate, limit );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    ProductOperationImpl po = gson.fromJson( info, ProductOperationImpl.class );
                    if ( po != null )
                    {
                        ProductOperationViewImpl productOperationViewImpl = new ProductOperationViewImpl( po );
                        list.add( productOperationViewImpl );
                    }
                }
            }
        }
        catch ( DBException | JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in getProductOperations", ex );
        }
        return list;
    }


    /**
     * Returns list of all sources of product operations for which product operations exist in DB
     *
     * @return list of product operation sources
     */
    public List<String> getProductOperationSources()
    {
        List<String> sources = new ArrayList<>();
        try
        {
            ResultSet rs = dbManager.executeQuery2( "select distinct source from product_operation" );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String source = row.getString( "source" );
                    if ( !Strings.isNullOrEmpty( source ) )
                    {
                        sources.add( source.toLowerCase() );
                    }
                }
            }
        }
        catch ( DBException e )
        {
            LOG.log( Level.SEVERE, "Error in getProductOperationSources", e );
        }

        return sources;
    }


    /**
     * Prints log of product operation to std out stream
     *
     * @param operationTrackId - id of operation
     * @param maxOperationDurationMs - max operation duration timeout after which printing ceases
     */
    @Override
    public void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs )
    {
        int logSize = 0;
        long startedTs = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            ProductOperationView po = getProductOperation( source.toLowerCase(), operationTrackId );
            if ( po != null )
            {
                //print log if anything new is appended to it
                if ( logSize != po.getLog().length() )
                {
                    //                    System.out.print( po.getLog().substring( logSize, po.getLog().length() ) );
                    //                    System.out.flush();
                    logSize = po.getLog().length();
                }
                //return if operation is completed
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    break;
                }
                //return if time limit is reached
                if ( System.currentTimeMillis() - startedTs > maxOperationDurationMs )
                {
                    break;
                }
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e )
                {
                    break;
                }
            }
            else
            {
                System.out.println( "Product operation not found" );
                break;
            }
        }
        System.out.println();
    }
}