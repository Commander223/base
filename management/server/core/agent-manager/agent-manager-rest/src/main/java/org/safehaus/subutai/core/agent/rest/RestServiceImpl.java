package org.safehaus.subutai.core.agent.rest;


import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );

    AgentManager agentManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    @Override
    public Response getAgents()
    {
        Set<Agent> agents = agentManager.getAgents();
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agents ) ).build();
    }


    @Override
    public Response getPhysicalAgents()
    {
        Set<Agent> agents = agentManager.getPhysicalAgents();
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agents ) ).build();
    }


    @Override
    public Response getLxcAgents()
    {
        Set<Agent> agents = agentManager.getLxcAgents();
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agents ) ).build();
    }


    @Override
    public Response getAgentByHostname( String hostname )
    {
        Agent agent = agentManager.getAgentByHostname( hostname );
        if ( agent != null )
        {
            return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agent ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getAgentByUUID( String uuid )
    {
        try
        {
            UUID agentUuid = UUID.fromString( uuid );
            Agent agent = agentManager.getAgentByUUID( agentUuid );
            if ( agent != null )
            {
                return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agent ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( IllegalArgumentException e )
        {
            LOG.log( Level.SEVERE, "Error in getAgentByUUID", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getLxcAgentsByParentHostname( String parentHostname )
    {
        Set<Agent> agents = agentManager.getLxcAgentsByParentHostname( parentHostname );
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agents ) ).build();
    }


    @Override
    public Response getAgentsByEnvironmentId( String environmentId )
    {
        try
        {
            UUID envId = UUID.fromString( environmentId );
            Set<Agent> agents = agentManager.getAgentsByEnvironmentId( envId );
            return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( agents ) ).build();
        }
        catch ( IllegalArgumentException e )
        {
            LOG.log( Level.SEVERE, "Error in getAgentsByEnvironmentId", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }
}
