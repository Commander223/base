package org.safehaus.subutai.core.tracker.rest;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService
{

    private static final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Tracker tracker;


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    @Override
    public Response getProductOperation( final String source, final String uuid )
    {
        try
        {
            UUID poUUID = UUID.fromString( uuid );

            ProductOperationView productOperationView = tracker.getProductOperation( source, poUUID );

            if ( productOperationView != null )
            {
                return Response.ok().entity( gson.toJson( productOperationView ) ).build();
            }
            return null;
        }
        catch ( IllegalArgumentException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getProductOperations( final String source, final String fromDate, final String toDate,
                                          final int limit )
    {
        try
        {
            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            Date fromDat = df.parse( fromDate + " 00:00:00" );
            Date toDat = df.parse( toDate + " 23:59:59" );

            List<ProductOperationView> pos = tracker.getProductOperations( source, fromDat, toDat, limit );

            return Response.ok().entity( gson.toJson( pos ) ).build();
        }
        catch ( ParseException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getProductOperationSources()
    {
        return Response.ok().entity( gson.toJson( tracker.getProductOperationSources() ) ).build();
    }
}
