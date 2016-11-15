package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties( ignoreUnknown = true )
public class DiskDto
{
    @JsonProperty( "total" )
    private double total = 0.0;

    @JsonProperty( "used" )
    private double used = 0.0;

    @JsonProperty( "aTotal" )
    private double avgTotal = 0.0D;

    @JsonProperty( "aAvailable" )
    private double avgAvailable = 0.0D;

    @JsonProperty( "aUsed" )
    private double avgUsed = 0.0D;


    public double getTotal()
    {
        return total;
    }


    public void setTotal( final double total )
    {
        this.total = total;
    }


    public double getUsed()
    {
        return used;
    }


    public void setUsed( final double used )
    {
        this.used = used;
    }


    public double getAvgTotal()
    {
        return avgTotal;
    }


    public void setAvgTotal( final double avgTotal )
    {
        this.avgTotal = avgTotal;
    }


    public double getAvgAvailable()
    {
        return avgAvailable;
    }


    public void setAvgAvailable( final double avgAvailable )
    {
        this.avgAvailable = avgAvailable;
    }


    public double getAvgUsed()
    {
        return avgUsed;
    }


    public void setAvgUsed( final double avgUsed )
    {
        this.avgUsed = avgUsed;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "DiskDto{" );
        sb.append( "total=" ).append( total );
        sb.append( ", used=" ).append( used );
        sb.append( ", avgTotal=" ).append( avgTotal );
        sb.append( ", avgAvailable=" ).append( avgAvailable );
        sb.append( ", avgUsed=" ).append( avgUsed );
        sb.append( '}' );
        return sb.toString();
    }
}
