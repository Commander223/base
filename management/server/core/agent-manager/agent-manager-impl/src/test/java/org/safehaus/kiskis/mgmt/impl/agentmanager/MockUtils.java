/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;


import java.util.List;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test mocking utilities
 */
public class MockUtils {

    public static Response getRegistrationRequestFromLxcAgent() {
        Response response = mock( Response.class );

        when( response.getUuid() ).thenReturn( UUID.randomUUID() );
        when( response.isIsLxc() ).thenReturn( true );
        when( response.getIps() ).thenReturn( mock( List.class ) );
        when( response.getHostname() ).thenReturn( "lxchostname" );
        when( response.getParentHostName() ).thenReturn( "hostname" );
        when( response.getType() ).thenReturn( ResponseType.REGISTRATION_REQUEST );

        return response;
    }


    public static Response getRegistrationRequestFromPhysicalAgent() {
        Response response = mock( Response.class );

        when( response.getUuid() ).thenReturn( UUID.randomUUID() );
        when( response.isIsLxc() ).thenReturn( false );
        when( response.getHostname() ).thenReturn( "hostname" );
        when( response.getIps() ).thenReturn( mock( List.class ) );
        when( response.getType() ).thenReturn( ResponseType.REGISTRATION_REQUEST );

        return response;
    }
}
