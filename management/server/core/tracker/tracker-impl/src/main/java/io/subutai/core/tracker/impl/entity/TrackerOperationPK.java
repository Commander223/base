package io.subutai.core.tracker.impl.entity;


import java.io.Serializable;


public class TrackerOperationPK implements Serializable
{
    private String source;
    private String operationTrackId;


    public TrackerOperationPK()
    {
    }


    public TrackerOperationPK( final String source, final String operationTrackId )
    {
        this.source = source;
        this.operationTrackId = operationTrackId;
    }


    public String getSource()
    {

        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
    }


    public String getOperationTrackId()
    {
        return operationTrackId;
    }


    public void setOperationTrackId( final String operationTrackId )
    {
        this.operationTrackId = operationTrackId;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrackerOperationPK ) )
        {
            return false;
        }

        final TrackerOperationPK that = ( TrackerOperationPK ) o;

        return operationTrackId.equals( that.operationTrackId ) && source.equals( that.source );
    }


    @Override
    public int hashCode()
    {
        int result = source.hashCode();
        result = 31 * result + operationTrackId.hashCode();
        return result;
    }
}
