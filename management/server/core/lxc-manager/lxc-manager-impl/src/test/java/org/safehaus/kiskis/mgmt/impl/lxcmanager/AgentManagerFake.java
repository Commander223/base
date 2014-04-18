/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AgentManagerFake implements AgentManager {

    private final Set<Agent> agents = new HashSet<Agent>();

    public AgentManagerFake() {
        agents.add(TestUtils.getPhysicalAgent());
        agents.add(TestUtils.getLxcAgent());
    }

    public Set<Agent> getAgents() {
        return Collections.unmodifiableSet(agents);
    }

    public Set<Agent> getPhysicalAgents() {
        return Util.wrapAgentToSet(TestUtils.getPhysicalAgent());
    }

    public Set<Agent> getLxcAgents() {
        return Util.wrapAgentToSet(TestUtils.getLxcAgent());
    }

    public Agent getAgentByHostname(String hostname) {

        for (Agent agent : agents) {
            if (agent.getHostname().equals(hostname)) {
                return agent;
            }
        }

        if (hostname != null) {
            if (hostname.contains(Common.PARENT_CHILD_LXC_SEPARATOR)) {
                return new Agent(TestUtils.getLxcAgent().getUuid(), hostname);
            } else {
                return new Agent(TestUtils.getPhysicalAgent().getUuid(), hostname);
            }
        }
        
        return null;
    }

    public Agent getAgentByUUID(UUID uuid) {
        for (Agent agent : agents) {
            if (agent.getUuid().equals(uuid)) {
                return agent;
            }
        }
        return null;
    }

    public Set<Agent> getLxcAgentsByParentHostname(String parentHostname) {
        Set<Agent> lxcAgents = new HashSet<Agent>();
        for (Agent agent : agents) {
            if (agent.getParentHostName().equals(parentHostname)) {
                lxcAgents.add(agent);
            }
        }
        return lxcAgents;
    }

    public void addListener(AgentListener listener) {
    }

    public void removeListener(AgentListener listener) {
    }

}
