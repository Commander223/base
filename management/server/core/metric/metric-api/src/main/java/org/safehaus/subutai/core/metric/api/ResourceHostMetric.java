package org.safehaus.subutai.core.metric.api;


import java.util.UUID;

import com.google.common.base.Objects;


/**
 * Interface for ResourceHostMetric
 */
public abstract class ResourceHostMetric extends Metric
{
    protected UUID peerId;


    public UUID getPeerId()
    {
        return peerId;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "host", host ).add( "availableRam", availableRam )
                      .add( "usedRam", usedRam ).add( "totalRam", totalRam ).add( "availableDisk", availableDisk )
                      .add( "usedDisk", usedDisk ).add( "totalDisk", totalDisk ).add( "cpuLoad5", cpuLoad5 )
                      .add( "peerId", peerId ).toString();
    }
}
