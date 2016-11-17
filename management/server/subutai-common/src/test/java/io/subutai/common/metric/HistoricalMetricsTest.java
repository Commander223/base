package io.subutai.common.metric;


import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.hub.share.dto.metrics.CpuDto;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.HostMetricsDto;
import io.subutai.hub.share.dto.metrics.MemoryDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HistoricalMetricsTest
{
    private HistoricalMetrics rhMetrics;
    private HistoricalMetrics chMetrics;


    @Before
    public void setup() throws IOException
    {
        final ObjectMapper objectMapper = new ObjectMapper();

        final String rhJson = IOUtils.toString( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream(
                "metrics/resource_host_metrics.json" ), "UTF-8" );
        rhMetrics = objectMapper.readValue( rhJson, HistoricalMetrics.class );
        final String chJson = IOUtils.toString( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream(
                "metrics/container_host_metrics.json" ), "UTF-8" );
        chMetrics = objectMapper.readValue( chJson, HistoricalMetrics.class );
    }


    @Test
    public void testGetHostType()
    {
        assertEquals( HostMetricsDto.HostType.CONTAINER_HOST, chMetrics.getHostType() );
        assertEquals( HostMetricsDto.HostType.RESOURCE_HOST, rhMetrics.getHostType() );
    }


    @Test
    public void testGetSeriesByType()
    {
        // count(system, user, iowait, nice, idle) == 5
        assertEquals( 5, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );
        // count(system, user) == 2
        assertEquals( 2, chMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );
        // count(Active, Buffers, MemFree, Cached) == 4
        assertEquals( 4, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.MEMORY ).size() );
        // count(cache, rss) == 2
        assertEquals( 2, chMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );

        assertEquals( 15, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.DISK ).size() );
    }


    @Test
    public void testCpu()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        CpuDto cpuDto = metrics.getCpu();

        assertNotNull( cpuDto );

        assertEquals( 0.9345833333, cpuDto.getAvgSystem(), 0.0001 );
        assertEquals( 3.005, cpuDto.getAvgUser(), 0.0001 );
        assertEquals( 0.0, cpuDto.getAvgNice(), 0.0001 );
        assertEquals( 95.26708333333335, cpuDto.getAvgIdle(), 0.0001 );
        assertEquals( 0.04125000000000002, cpuDto.getAvgIowait(), 0.0001 );
    }


    @Test
    public void testMemory()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        MemoryDto memoryDto = metrics.getMemory();

        assertNotNull( memoryDto );

        assertEquals( 1440642129.26984, memoryDto.getAvgActive(), 0.0001 );
        assertEquals( 6414672749.71429, memoryDto.getAvgMemFree(), 0.0001 );
        assertEquals( 905762913.52381, memoryDto.getAvgCached(), 0.0001 );
        assertEquals( 17029022.4761905, memoryDto.getAvgBuffers(), 0.0001 );
    }


    @Test
    public void testDisk()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        DiskDto disk = metrics.getDisk();

        // mnt partition
//        double available = disk.getAvgAvailable();
//        assertEquals( 1.0129919970742857E11, available, 0.0001 );
//        double total = disk.getAvgTotal();
//        assertEquals( 1.073741824E11, total, 0.0001 );
//        double used = disk.getAvgUsed();
//        assertEquals( 5.519773403428572E9, used, 0.0001 );

        //        assertEquals( total, available+used,0.001 );
    }
}
