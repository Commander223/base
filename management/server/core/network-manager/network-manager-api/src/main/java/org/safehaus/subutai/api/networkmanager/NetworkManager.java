package org.safehaus.subutai.api.networkmanager;

import org.safehaus.subutai.shared.protocol.Agent;

import java.util.List;

/**
 * Created by daralbaev on 03.04.14.
 */
public interface NetworkManager {
    public boolean configSshOnAgents(List<Agent> agentList);

    public boolean configSshOnAgents(List<Agent> agentList, Agent agent);

    public boolean configHostsOnAgents(List<Agent> agentList, String domainName);

    public boolean configHostsOnAgents(List<Agent> agentList, Agent agent, String domainName);
}
