package org.safehaus.subutai.core.metric.api;


/**
 * Interface for MetricListener
 */
public interface MetricListener
{
    /**
     * Notifies listeners about threshold excess on the container
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void alertThresholdExcess( ContainerHostMetric metric );

    /**
     * Returns unique id of subscriber module for routing notifications
     *
     * @return - id of subscriber module
     */
    public String getSubscriberId();
}
