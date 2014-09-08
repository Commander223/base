/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.solr;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;


public class Config implements ConfigBase {

	public static final String PRODUCT_KEY = "Solr";
	private String clusterName = "";
	private int numberOfNodes = 1;
	private Set<Agent> nodes = new HashSet<>();


	public String getClusterName() {
		return clusterName;
	}


	public Config setClusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
	}

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(final int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public Set<Agent> getNodes() {
		return nodes;
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("clusterName", clusterName)
				.append("numberOfNodes", numberOfNodes)
				.append("nodes", nodes).toString();
	}
}
