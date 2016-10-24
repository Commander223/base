package io.subutai.common.util;


import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;


/**
 * UUID utilities
 */
public class UUIDUtil
{

    private UUIDUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static UUID generateTimeBasedUUID()
    {
        return Generators.timeBasedGenerator( EthernetAddress.fromInterface() ).generate();
    }


    public static UUID generateRandomUUID()
    {
        return UUID.randomUUID();
    }


    public static UUID generateUUIDFromString( String uuid )
    {
        return UUID.fromString( uuid );
    }


    public static UUID generateUUIDFromBytes( byte[] bytes )
    {
        return UUID.nameUUIDFromBytes( bytes );
    }


    public static boolean isStringAUuid( String uuid )
    {
        try
        {
            UUID.fromString( uuid );
            return true;
        }
        catch ( NullPointerException | IllegalArgumentException e )
        {
            return false;
        }
    }
}
