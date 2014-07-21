/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ApiBase;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


/**
 * @author dilshat
 */
public interface Mongo extends ApiBase<MongoClusterConfig> {

    /**
     * adds node to the specified cluster
     *
     * @param clusterName - name of cluster
     * @param nodeType - type of node to add
     *
     * @return - UUID of operation to track
     */
    public UUID addNode( String clusterName, NodeType nodeType );

    /**
     * destroys node in the specified cluster
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     *
     * @return - UUID of operation to track
     */
    public UUID destroyNode( String clusterName, String lxcHostName );

    /**
     * Starts the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     *
     * @return - UUID of operation to track
     */
    public UUID startNode( String clusterName, String lxcHostName );

    /**
     * Stops the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     *
     * @return - UUID of operation to track
     */
    public UUID stopNode( String clusterName, String lxcHostName );

    /**
     * Checks status of the specified node
     *
     * @param clusterName - name of cluster
     * @param lxcHostName - hostname of node
     *
     * @return - UUID of operation to track
     */
    public UUID checkNode( String clusterName, String lxcHostName );

    public ClusterSetupStrategy getSetupStrategy( ProductOperation po, AgentManager agentManager, Mongo mongoManager,
                                                  CommandRunner commandRunner, ContainerManager containerManager,
                                                  MongoClusterConfig config );
}
