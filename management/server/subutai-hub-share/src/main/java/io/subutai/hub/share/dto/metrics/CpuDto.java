package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties( ignoreUnknown = true )
public class CpuDto
{
    @JsonProperty( "model" )
    private String model = "UNKNOWN";

    @JsonProperty( "idle" )
    private Double idle = 0.0;

    @JsonProperty( "coreCount" )
    private int coreCount = 0;

    @JsonProperty( "frequency" )
    private double frequency = 0.0;

    @JsonProperty( "aSystem" )
    private double avgSystem = 0.0;

    @JsonProperty( "aIdle" )
    private double avgIdle = 0.0;

    @JsonProperty( "aIowait" )
    private double avgIowait = 0.0;

    @JsonProperty( "aUser" )
    private double avgUser = 0.0;

    @JsonProperty( "aNice" )
    private double avgNice = 0.0;


    public String getModel()
    {
        return model;
    }


    public void setModel( final String model )
    {
        this.model = model;
    }


    public Double getIdle()
    {
        return idle;
    }


    public void setIdle( final Double idle )
    {
        this.idle = idle;
    }


    public int getCoreCount()
    {
        return coreCount;
    }


    public void setCoreCount( final int coreCount )
    {
        this.coreCount = coreCount;
    }


    public double getFrequency()
    {
        return frequency;
    }


    public void setFrequency( final double frequency )
    {
        this.frequency = frequency;
    }


    public double getAvgSystem()
    {
        return avgSystem;
    }


    public void setAvgSystem( final double avgSystem )
    {
        this.avgSystem = avgSystem;
    }


    public double getAvgIdle()
    {
        return avgIdle;
    }


    public void setAvgIdle( final double avgIdle )
    {
        this.avgIdle = avgIdle;
    }


    public double getAvgIowait()
    {
        return avgIowait;
    }


    public void setAvgIowait( final double avgIowait )
    {
        this.avgIowait = avgIowait;
    }


    public double getAvgUser()
    {
        return avgUser;
    }


    public void setAvgUser( final double avgUser )
    {
        this.avgUser = avgUser;
    }


    public double getAvgNice()
    {
        return avgNice;
    }


    public void setAvgNice( final double avgNice )
    {
        this.avgNice = avgNice;
    }
}
