package org.safehaus.subutai.core.executor.impl;


/**
 * Exception thrown by Command Executor internal methods
 */
public class CommandExecutorException extends Exception
{
    public CommandExecutorException( final String message )
    {
        super( message );
    }


    public CommandExecutorException( final Throwable cause )
    {
        super( cause );
    }
}
