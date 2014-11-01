package org.safehaus.subutai.core.peer.impl;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Remote Peer REST client
 */
public class RemotePeerRestClient
{

    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerRestClient.class.getName() );
    private static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long CONNECTION_TIMEOUT = 1000 * 60 * 1;
    private final long receiveTimeout;
    private final long connectionTimeout;
    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String baseUrl = "http://%s:%s/cxf";


    public RemotePeerRestClient( String ip, String port )
    {
        this.receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
        this.connectionTimeout = CONNECTION_TIMEOUT;

        baseUrl = String.format( baseUrl, ip, port );
        LOG.info( baseUrl );
    }


    public RemotePeerRestClient( long timeout, String ip, String port )
    {
        this.connectionTimeout = CONNECTION_TIMEOUT;
        this.receiveTimeout = timeout;

        baseUrl = String.format( baseUrl, ip, port );
        LOG.info( baseUrl );
    }


    protected WebClient createWebClient()
    {
        WebClient client = WebClient.create( baseUrl );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( connectionTimeout );
        httpClientPolicy.setReceiveTimeout( receiveTimeout );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }


    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {

        String path = "peer/environment/containers";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "environmentId", environmentId.toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        String jsonObject = response.readEntity( String.class );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( jsonObject, new TypeToken<Set<ContainerHost>>()
            {}.getType() );
        }

        if ( response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
        else
        {
            return Collections.emptySet();
        }
    }


    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/stop";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/start";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/destroy";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public boolean isConnected( final Host host ) throws PeerException
    {

        if ( !( host instanceof ContainerHost ) )
        {
            throw new PeerException( "Operation not allowed." );
        }
        String path = "peer/container/isconnected";


        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( host ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Boolean.class );
        }
        else
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }
}
