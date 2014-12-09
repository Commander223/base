package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/2/14.
 */
public class PeerQuotaInfo
{
    CpuQuotaInfo cpuQuotaInfo;
    HddQuotaInfo homePartitionQuota;
    HddQuotaInfo varPartitionQuota;
    HddQuotaInfo optPartitionQuota;
    HddQuotaInfo rootfsPartitionQuota;
    MemoryQuotaInfo memoryQuota;


    public PeerQuotaInfo( final CpuQuotaInfo cpuQuotaInfo, final HddQuotaInfo homePartitionQuota,
                          final HddQuotaInfo varPartitionQuota, final HddQuotaInfo optPartitionQuota,
                          final HddQuotaInfo rootfsPartitionQuota, final MemoryQuotaInfo memoryQuota )
    {
        this.cpuQuotaInfo = cpuQuotaInfo;
        this.homePartitionQuota = homePartitionQuota;
        this.varPartitionQuota = varPartitionQuota;
        this.optPartitionQuota = optPartitionQuota;
        this.rootfsPartitionQuota = rootfsPartitionQuota;
        this.memoryQuota = memoryQuota;
    }


    public CpuQuotaInfo getCpuQuotaInfo()
    {
        return cpuQuotaInfo;
    }


    public HddQuotaInfo getHomePartitionQuota()
    {
        return homePartitionQuota;
    }


    public HddQuotaInfo getVarPartitionQuota()
    {
        return varPartitionQuota;
    }


    public HddQuotaInfo getOptPartitionQuota()
    {
        return optPartitionQuota;
    }


    public HddQuotaInfo getRootfsPartitionQuota()
    {
        return rootfsPartitionQuota;
    }


    public MemoryQuotaInfo getMemoryQuota()
    {
        return memoryQuota;
    }


    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        if ( cpuQuotaInfo != null )
        {
            result.append( cpuQuotaInfo.toString() ).append( "\n" );
        }
        if ( homePartitionQuota != null )
        {
            result.append( homePartitionQuota.toString() ).append( "\n" );
        }
        if ( varPartitionQuota != null )
        {
            result.append( varPartitionQuota.toString() ).append( "\n" );
        }
        if ( optPartitionQuota != null )
        {
            result.append( optPartitionQuota.toString() ).append( "\n" );
        }
        if ( rootfsPartitionQuota != null )
        {
            result.append( rootfsPartitionQuota.toString() ).append( "\n" );
        }
        if ( memoryQuota != null )
        {
            result.append( memoryQuota.toString() ).append( "\n" );
        }
        return result.toString();
    }
}