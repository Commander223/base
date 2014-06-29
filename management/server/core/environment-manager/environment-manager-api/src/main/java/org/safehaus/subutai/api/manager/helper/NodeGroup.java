package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroup {

    private String name;
    private int numberOfNodes;
    private String templateName;
    private PlacementStrategyENUM placementStrategyENUM;
    private boolean linkHosts;
    private boolean exchangeSshKeys;
    private Set<String> physicalNodes;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public void setNumberOfNodes( final int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    public PlacementStrategyENUM getPlacementStrategyENUM() {
        return placementStrategyENUM;
    }


    public void setPlacementStrategyENUM( final PlacementStrategyENUM placementStrategyENUM ) {
        this.placementStrategyENUM = placementStrategyENUM;
    }


    public boolean isLinkHosts() {
        return linkHosts;
    }


    public void setLinkHosts( final boolean linkHosts ) {
        this.linkHosts = linkHosts;
    }


    public boolean isExchangeSshKeys() {
        return exchangeSshKeys;
    }


    public void setExchangeSshKeys( final boolean exchangeSshKeys ) {
        this.exchangeSshKeys = exchangeSshKeys;
    }


    public Set<String> getPhysicalNodes() {
        return physicalNodes;
    }


    public void setPhysicalNodes( final Set<String> physicalNodes ) {
        this.physicalNodes = physicalNodes;
    }
}
