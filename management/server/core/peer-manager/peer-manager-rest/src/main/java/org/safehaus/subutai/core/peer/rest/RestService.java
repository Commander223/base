package org.safehaus.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.PeerInfo;


public interface RestService
{


    @POST
    @Path("peer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public PeerInfo registerPeer( @QueryParam("peer") String peer );


    @GET
    @Path("containers/format")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCreateContainersMsgJsonFormat();


    @GET
    @Path("json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPeerJsonFormat();

    @GET
    @Path("id")
    @Produces("text/plain")
    public String getId();


    @POST
    @Path("container/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createContainers( @FormParam("ownerPeerId") String ownerPeerId,
                                      @FormParam("environmentId") String environmentId,
                                      @FormParam("templates") String templates, @FormParam("quantity") int quantity,
                                      @FormParam("strategyId") String strategyId,
                                      @FormParam("criteria") String criteria );

    @POST
    @Path("container/destroy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response destroyContainer( @FormParam("host") String host );

    @POST
    @Path("container/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startContainer( @FormParam("host") String host );

    @POST
    @Path("container/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopContainer( @FormParam("host") String host );

    @POST
    @Path("container/isconnected")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isContainerConnected( @FormParam("host") String host );


    @POST
    @Path("template/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTemplate( @FormParam("host") String host );

    @POST
    @Path("execute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response execute( @FormParam("requestBuilder") String requestBuilder, @FormParam("host") String host );

    @POST
    @Path("environment/containers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response environmentContainers( @FormParam("environmentId") String envId );

    @GET
    @Path("ping")
    public Response ping();

    @POST
    @Path("register")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response processRegisterRequest( @QueryParam("peer") String peer );

    @DELETE
    @Path("unregister")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response unregisterPeer( @QueryParam("peerId") String peerId );

    @PUT
    @Path("update")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updatePeer( @QueryParam("peer") String peer );
}