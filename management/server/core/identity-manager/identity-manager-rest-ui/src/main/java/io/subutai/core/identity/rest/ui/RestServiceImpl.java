package io.subutai.core.identity.rest.ui;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.identity.api.*;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.common.util.JsonUtil;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceImpl.class);
    protected JsonUtil jsonUtil = new JsonUtil();
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }



    /** Users ***********************************************/

    @Override
    public Response getUsers()
    {
        try
        {
            return Response.ok(jsonUtil.to( identityManager.getAllUsers() )).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting users #getUsers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    // @todo convert to User object
    @Override
    public Response saveUser( final String username, final String fullName,
                             final String password, final String email,
                             final String rolesJson, final Long userId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( username ), "username is missing" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( fullName ), "fullname is missing" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( email ), "email must be set" );

        try
        {
            User newUser;

            if( userId == null || userId <= 0 )
            {
                Preconditions.checkArgument( !Strings.isNullOrEmpty( password ), "User name must be set" );
                newUser = identityManager.createUser( username, password, fullName, email, UserType.Regular.getId());
            }
            else
            {
                newUser = identityManager.getUser( userId );
            }

            if(!Strings.isNullOrEmpty(rolesJson)) {
                List<Long> roleIds = jsonUtil.fromJson(
                    rolesJson, new TypeToken<ArrayList<Long>>() {}.getType()
                );


                newUser.setRoles( roleIds.stream().map( r -> identityManager.getRole(r) ).collect( Collectors.toList() ) );
            }
            identityManager.updateUser(newUser);
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response deleteUser( final Long userId )
    {
        try
        {
            identityManager.removeUser(userId);
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting user #deleteUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }



    /** Roles ***********************************************/

    @Override
    public Response getRoles()
    {
        try
        {
            return Response.ok(jsonUtil.to(identityManager.getAllRoles())).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting roles #getRoles", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response saveRole( final String rolename, final String permissionJson, final Long roleId )
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(rolename));

            Role role;

            if(roleId == null || roleId <= 0){
                role = identityManager.createRole( rolename, UserType.Regular.getId() );
            } else {
                role = identityManager.getRole(roleId);
            }

            if(!Strings.isNullOrEmpty(permissionJson)) {
                ArrayList<PermissionJson> permissions = JsonUtil.fromJson(
                    permissionJson, new TypeToken<ArrayList<PermissionJson>>() {}.getType()
                );


                if(!Strings.isNullOrEmpty(rolename)) {
                    role.setName(rolename);
                }

                role.setPermissions( permissions.stream().map( p -> identityManager.createPermission(
                        p.getObject(),
                        p.getScope(),
                        p.getRead(),
                        p.getWrite(),
                        p.getUpdate(),
                        p.getDelete()
                        ) ).collect( Collectors.toList() ) );
            }

            identityManager.updateRole(role);
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new role #createRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response deleteRole( final Long roleId )
    {
        try
        {
            identityManager.removeRole(roleId);
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting role #deleteRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getRoleTypes()
    {
        return Response.ok( io.subutai.common.security.objects.TokenType.values() ).build();
    }



    /** Permissions ***********************************************/

    @Override
    public Response getPermissions()
    {
        try
        {
            return Response.ok( jsonUtil.to(identityManager.getAllPermissions()) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error receiving permissions", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }



    /** Tokens ***********************************************/

    @Override
    public Response getAllUserTokens()
    {
        try
        {
            List<UserTokenJson> list = identityManager.getAllUserTokens().stream().map( p -> new UserTokenJson(
                    p.getUserId(),
                    identityManager.getUser( p.getUserId() ).getUserName(),
                    p.getToken(),
                    p.getFullToken(),
                    p.getType(),
                    p.getHashAlgorithm(),
                    p.getIssuer(),
                    p.getValidDate()
            ) ).collect( Collectors.toList() );

            return Response.ok( JsonUtil.toJson( list ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error receiving user tokens", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response createUserToken( final Long userId, final String token, final Integer period )
    {
        try
        {
            Preconditions.checkNotNull( userId, "Invalid userId" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token" );
            Preconditions.checkNotNull( period, "Invalid period" );

            Date newDate = new Date( );
            java.util.Calendar cal = Calendar.getInstance();
            cal.setTime( newDate );
            cal.add( Calendar.HOUR_OF_DAY, period );

            identityManager.createUserToken( identityManager.getUser( userId ), token, null, "subutai.io", 2, cal.getTime() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error creating new user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }



    @Override
    public Response updateUserToken( final Long userId, final String token, final String newToken, final Integer period )
    {
        try
        {
            Preconditions.checkNotNull( userId, "Invalid userId" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token id to be replaced" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( newToken ), "Invalid newToken" );
            Preconditions.checkNotNull( period, "Invalid period" );

            Date newDate = new Date( );
            java.util.Calendar cal = Calendar.getInstance();
            cal.setTime( newDate );
            cal.add( Calendar.HOUR_OF_DAY, period );

            identityManager.updateUserToken( token, identityManager.getUser( userId ), newToken, null, "issuer", 1, cal.getTime() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response removeUserToken( final String tokenId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( tokenId ), "Invalid tokenId" );

            identityManager.removeUserToken( tokenId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating new user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }


        return Response.ok().build();
    }
}