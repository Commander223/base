package io.subutai.common.quota;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.resource.ContainerResourceType;


public class Quota
{
    @JsonProperty( "resource" )
    private ContainerResource resource;

    @JsonProperty( "threshold" )
    private Integer threshold;


    public Quota( @JsonProperty( "resource" ) final ContainerResource resource,
                  @JsonProperty( "threshold" ) final Integer threshold )
    {
        this.resource = resource;
        this.threshold = threshold;
    }


    public ContainerResource getResource()
    {
        return resource;
    }


    public Integer getThreshold()
    {
        return threshold;
    }


    public ContainerCpuResource getAsCpuResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.CPU )
        {
            return ( ContainerCpuResource ) resource;
        }
        throw new IllegalStateException( "Could not get as CPU resource." );
    }


    public ContainerRamResource getAsRamResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.RAM )
        {
            return ( ContainerRamResource ) resource;
        }
        throw new IllegalStateException( "Could not get as RAM resource." );
    }


    public ContainerDiskResource getAsDiskResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.OPT
                || resource.getContainerResourceType() == ContainerResourceType.HOME
                || resource.getContainerResourceType() == ContainerResourceType.ROOTFS
                || resource.getContainerResourceType() == ContainerResourceType.VAR )
        {
            return ( ContainerDiskResource ) resource;
        }
        throw new IllegalStateException( "Could not get as disk resource." );
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Quota{" );
        sb.append( "resource=" ).append( resource );
        sb.append( ", threshold=" ).append( threshold );
        sb.append( '}' );
        return sb.toString();
    }
}
