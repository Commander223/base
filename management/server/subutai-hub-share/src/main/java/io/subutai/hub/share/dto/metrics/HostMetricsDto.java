package io.subutai.hub.share.dto.metrics;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;


public class HostMetricsDto
{
    public enum HostType
    {
        RESOURCE_HOST, CONTAINER_HOST
    }


    public enum InstanceType
    {
        LOCAL, EC2
    }


    @JsonProperty( "hostId" )
    private String hostId;

    @JsonProperty( "host" )
    protected String hostName;

    @JsonProperty( "type" )
    private HostType type;

    @JsonProperty( "RAM" )
    private MemoryDto memory = new MemoryDto();

    @JsonProperty( "CPU" )
    private CpuDto cpu = new CpuDto();

    @JsonProperty( "Net" )
    private NetDto net = new NetDto( "wan", 0, 0 );

    @JsonProperty( "Disk" )
    private DiskDto disk = new DiskDto();

    @JsonProperty( "instanceType" )
    private InstanceType instanceType;

    @JsonProperty
    private Integer containersCount;

    @JsonProperty
    private boolean management;

    @JsonProperty
    private Date createdTime = new Date();


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
    }


    public HostType getType()
    {
        return type;
    }


    public void setType( final HostType type )
    {
        this.type = type;
    }


    public MemoryDto getMemory()
    {
        return memory;
    }


    public void setMemory( final MemoryDto memory )
    {
        this.memory = memory;
    }


    public CpuDto getCpu()
    {
        return cpu;
    }


    public void setCpu( final CpuDto cpu )
    {
        this.cpu = cpu;
    }


    public NetDto getNet()
    {
        return net;
    }


    public void setNet( final NetDto net )
    {
        this.net = net;
    }


    public DiskDto getDisk()
    {
        return disk;
    }


    public void setDisk( final DiskDto disk )
    {
        this.disk = disk;
    }


    public InstanceType getInstanceType()
    {
        return instanceType;
    }


    public void setInstanceType( final InstanceType instanceType )
    {
        this.instanceType = instanceType;
    }


    public Integer getContainersCount()
    {
        return containersCount;
    }


    public void setContainersCount( final Integer containersCount )
    {
        this.containersCount = containersCount;
    }


    public boolean isManagement()
    {
        return management;
    }


    public void setManagement( final boolean management )
    {
        this.management = management;
    }


    public Date getCreatedTime()
    {
        return createdTime;
    }


    public void setCreatedTime( final Date createdTime )
    {
        this.createdTime = createdTime;
    }
}
