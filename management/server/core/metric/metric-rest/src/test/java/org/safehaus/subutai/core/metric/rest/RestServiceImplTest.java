package org.safehaus.subutai.core.metric.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.metric.impl.ContainerHostMetricImpl;
import org.safehaus.subutai.core.metric.impl.ResourceHostMetricImpl;
import org.safehaus.subutai.core.monitor.api.MonitorException;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for RestServiceImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    @Mock
    Monitor monitor;
    @Mock
    EnvironmentManager environmentManager;
    RestServiceImpl restService;
    ResourceHostMetric resourceHostMetric;
    ContainerHostMetric containerHostMetric;


    @Before
    public void setUp() throws Exception
    {
        monitor = mock( Monitor.class );
        environmentManager = mock( EnvironmentManager.class );
        resourceHostMetric = new ResourceHostMetricImpl();
        containerHostMetric = new ContainerHostMetricImpl();
        when( monitor.getResourceHostMetrics() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );
        restService = new RestServiceImpl( monitor, environmentManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMonitor() throws Exception
    {
        new RestServiceImpl( null, environmentManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullEnvironmentManager() throws Exception
    {
        new RestServiceImpl( monitor, null );
    }


    @Test
    public void testGetResourceHostMetrics() throws Exception
    {

        Response response = restService.getResourceHostMetrics();

        Set<ResourceHostMetric> metrics = JsonUtil.fromJson( response.getEntity().toString(),
                new TypeToken<Set<ResourceHostMetricImpl>>() {}.getType() );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertFalse( metrics.isEmpty() );
    }


    @Test
    public void testGetResourceHostMetricsException() throws Exception
    {
        when( monitor.getResourceHostMetrics() ).thenThrow( new MonitorException( "" ) );

        Response response = restService.getResourceHostMetrics();

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testAlertThresholdExcess() throws Exception
    {
        String alertMetric = JsonUtil.toJson( containerHostMetric );
        Response response = restService.alertThresholdExcess( alertMetric );

        verify( monitor ).alertThresholdExcess( alertMetric );

        assertEquals( Response.Status.ACCEPTED.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testAlertThresholdExcessException() throws Exception
    {
        String alertMetric = JsonUtil.toJson( containerHostMetric );
        doThrow( new MonitorException( "" ) ).when( monitor ).alertThresholdExcess( alertMetric );

        Response response = restService.alertThresholdExcess( alertMetric );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerHostMetrics() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        Environment environment = mock( Environment.class );
        when( environmentManager.getEnvironmentByUUID( environmentId ) ).thenReturn( environment );
        when( monitor.getContainerMetrics( environment ) ).thenReturn( Sets.newHashSet( containerHostMetric ) );

        Response response = restService.getContainerHostMetrics( environmentId.toString() );
        Set<ContainerHostMetric> metrics = JsonUtil.fromJson( response.getEntity().toString(),
                new TypeToken<Set<ContainerHostMetricImpl>>() {}.getType() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertFalse( metrics.isEmpty() );
    }


    @Test
    public void testGetContainerHostMetricsWithNullEnvironment() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        when( environmentManager.getEnvironmentByUUID( environmentId ) ).thenReturn( null );

        Response response = restService.getContainerHostMetrics( environmentId.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerHostMetricsWithMonitorException() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        Environment environment = mock( Environment.class );
        when( environmentManager.getEnvironmentByUUID( environmentId ) ).thenReturn( environment );
        when( monitor.getContainerMetrics( environment ) ).thenThrow( new MonitorException( "" ) );

        Response response = restService.getContainerHostMetrics( environmentId.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerHostMetricsWithIllegalEnvironmentId() throws Exception
    {
        Response response = restService.getContainerHostMetrics( null );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }
}
