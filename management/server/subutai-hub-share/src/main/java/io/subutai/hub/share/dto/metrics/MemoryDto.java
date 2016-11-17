package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties( ignoreUnknown = true )
public class MemoryDto
{
    @JsonProperty( "total" )
    private Double total;

    @JsonProperty( "free" )
    private Double free;

    @JsonProperty( "active" )
    private double avgActive = 0.0D;

    @JsonProperty( "cached" )
    private double avgCached = 0.0D;

    @JsonProperty( "memFree" )
    private double avgMemFree = 0.0D;

    @JsonProperty( "buffers" )
    private double avgBuffers = 0.0D;


    public Double getTotal()
    {
        return total;
    }


    public void setTotal( final Double total )
    {
        this.total = total;
    }


    public Double getFree()
    {
        return free;
    }


    public void setFree( final Double free )
    {
        this.free = free;
    }


    public double getAvgActive()
    {
        return avgActive;
    }


    public void setAvgActive( final double avgActive )
    {
        this.avgActive = avgActive;
    }


    public double getAvgCached()
    {
        return avgCached;
    }


    public void setAvgCached( final double avgCached )
    {
        this.avgCached = avgCached;
    }


    public double getAvgMemFree()
    {
        return avgMemFree;
    }


    public void setAvgMemFree( final double avgMemFree )
    {
        this.avgMemFree = avgMemFree;
    }


    public double getAvgBuffers()
    {
        return avgBuffers;
    }


    public void setAvgBuffers( final double avgBuffers )
    {
        this.avgBuffers = avgBuffers;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "MemoryDto{" );
        sb.append( "total=" ).append( total );
        sb.append( ", free=" ).append( free );
        sb.append( ", avgActive=" ).append( avgActive );
        sb.append( ", avgCached=" ).append( avgCached );
        sb.append( ", avgMemFree=" ).append( avgMemFree );
        sb.append( ", avgBuffers=" ).append( avgBuffers );
        sb.append( '}' );
        return sb.toString();
    }
}
