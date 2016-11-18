package io.subutai.common.metric;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.dto.metrics.CpuDto;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.HostMetricsDto;
import io.subutai.hub.share.dto.metrics.MemoryDto;
import io.subutai.hub.share.dto.metrics.NetDto;


/**
 * Historical metrics
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class HistoricalMetrics
{
    @JsonProperty( "startTime" )
    private Date startTime;

    @JsonProperty( "endTime" )
    private Date endTime;

    @JsonProperty( "Metrics" )
    private List<SeriesBatch> metrics = new ArrayList<>();
    private Map<SeriesBatch.SeriesType, List<Series>> seriesMap = new HashMap<>();


    public HistoricalMetrics()
    {
    }


    public HistoricalMetrics( @JsonProperty( "startTime" ) final Date startTime,
                              @JsonProperty( "endTime" ) final Date endTime,
                              @JsonProperty( "Metrics" ) final List<SeriesBatch> metrics )
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
    }


    public HistoricalMetrics( final Date startTime, final Date endTime )
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public Date getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Date startTime )
    {
        this.startTime = startTime;
    }


    public Date getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Date endTime )
    {
        this.endTime = endTime;
    }


    @JsonIgnore
    public List<SeriesBatch> getMetrics()
    {
        return metrics;
    }


    @JsonIgnore
    public HostMetricsDto.HostType getHostType()
    {
        HostMetricsDto.HostType result = null;
        if ( !metrics.isEmpty() && !metrics.get( 0 ).getSeries().isEmpty() )
        {
            if ( metrics.get( 0 ).getSeries().get( 0 ).getName().startsWith( "host_" ) )
            {
                result = HostMetricsDto.HostType.RESOURCE_HOST;
            }
            else
            {
                if ( metrics.get( 0 ).getSeries().get( 0 ).getName().startsWith( "lxc_" ) )
                {
                    result = HostMetricsDto.HostType.CONTAINER_HOST;
                }
            }
        }

        if ( result == null )
        {
            throw new IllegalStateException( "Could not determine host type." );
        }
        return result;
    }


    @JsonIgnore
    public List<Series> getSeriesByType( final SeriesBatch.SeriesType type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "Series type could not be null." );
        }

        List<Series> result = new ArrayList<>();
        final HostMetricsDto.HostType hostType = getHostType();

        for ( SeriesBatch batch : metrics )
        {
            result.addAll( batch.getSeriesByName( type.getName( hostType ) ) );
        }
        return result;
    }


    protected void splitSeries()
    {
        for ( SeriesBatch.SeriesType type : SeriesBatch.SeriesType.values() )
        {
            seriesMap.put( type, getSeriesByType( type ) );
        }
    }


    @JsonIgnore
    private CpuDto getCpuDto( final List<Series> series )
    {
        CpuDto cpuDto = new CpuDto();
        cpuDto.setAvgSystem( SeriesHelper.getAvg( series, new Tag( "type", "system" ) ) );
        cpuDto.setAvgUser( SeriesHelper.getAvg( series, new Tag( "type", "user" ) ) );
        cpuDto.setAvgIdle( SeriesHelper.getAvg( series, new Tag( "type", "idle" ) ) );
        cpuDto.setAvgIowait( SeriesHelper.getAvg( series, new Tag( "type", "iowait" ) ) );
        cpuDto.setAvgNice( SeriesHelper.getAvg( series, new Tag( "type", "nice" ) ) );

        return cpuDto;
    }


    @JsonIgnore
    private MemoryDto getMemoryDto( final List<Series> series )
    {
        MemoryDto memoryDto = new MemoryDto();
        memoryDto.setAvgActive( SeriesHelper.getAvg( series, new Tag( "type", "active" ) ) );
        memoryDto.setAvgBuffers( SeriesHelper.getAvg( series, new Tag( "type", "buffers" ) ) );
        memoryDto.setAvgCached( SeriesHelper.getAvg( series, new Tag( "type", "cached" ) ) );
        memoryDto.setAvgMemFree( SeriesHelper.getAvg( series, new Tag( "type", "memfree" ) ) );
        return memoryDto;
    }


    @JsonIgnore
    private DiskDto getDiskDto( final List<Series> series )
    {
        DiskDto dto = new DiskDto();
        //        dto.setAvgAvailable(
        //                SeriesHelper.getAvg( series, new Tag( "type", "available" ), new Tag( "mount", "/mnt" ) ) );
        //        dto.setAvgTotal( SeriesHelper.getAvg( series, new Tag( "type", "total" ), new Tag( "mount", "/mnt"
        // ) ) );
        //        dto.setAvgUsed( SeriesHelper.getAvg( series, new Tag( "type", "used" ), new Tag( "mount", "/mnt" )
        // ) );

        return dto;
    }


    @JsonIgnore
    private NetDto getNetDto( final List<Series> series )
    {
        double avgIn = SeriesHelper.getAvg( series, new Tag( "iface", "wan" ), new Tag( "type", "in" ) );
        double avgOut = SeriesHelper.getAvg( series, new Tag( "iface", "wan" ), new Tag( "type", "out" ) );
        long timeInterval = ( endTime.getTime() - startTime.getTime() ) / 1000;
        return new NetDto( "wan", avgIn / timeInterval / 1024, avgOut / timeInterval / 1024 );
    }


    @JsonIgnore
    public HostMetricsDto getHostMetrics()
    {
        HostMetricsDto result = new HostMetricsDto();

        HostMetricsDto.HostType hostType = getHostType();
        result.setType( hostType );
        splitSeries();

        result.setCpu( getCpuDto( seriesMap.get( SeriesBatch.SeriesType.CPU ) ) );
        result.setMemory( getMemoryDto( seriesMap.get( SeriesBatch.SeriesType.MEMORY ) ) );
        result.setDisk( getDiskDto( seriesMap.get( SeriesBatch.SeriesType.DISK ) ) );
        result.setNet( getNetDto( seriesMap.get( SeriesBatch.SeriesType.NET ) ) );

        return result;
    }
}