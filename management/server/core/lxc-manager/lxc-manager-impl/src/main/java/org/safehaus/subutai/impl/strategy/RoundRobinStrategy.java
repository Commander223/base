package org.safehaus.subutai.impl.strategy;

import java.util.*;
import org.safehaus.subutai.api.lxcmanager.*;
import org.safehaus.subutai.shared.protocol.Agent;

public class RoundRobinStrategy extends LxcPlacementStrategy {

    public static final String DEFAULT_NODE_TYPE = "default";
    private final int nodesCount;

    public RoundRobinStrategy(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {
        if(serverMetrics == null || serverMetrics.isEmpty()) return;

        List<Agent> ls = sortServers(serverMetrics);

        // distribute required nodes among servers in round-robin fashion
        Map<Agent, Integer> slots = new HashMap<>();
        for(int i = 0; i < nodesCount; i++) {
            Agent best = ls.get(i % nodesCount);
            if(slots.containsKey(best)) slots.put(best, slots.get(best) + 1);
            else slots.put(best, 1);
        }
        // add node distribution counts
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            addPlacementInfo(e.getKey(), DEFAULT_NODE_TYPE, e.getValue());
        }
    }

    protected List<Agent> sortServers(Map<Agent, ServerMetric> serverMetrics) {
        List<Agent> ls = new ArrayList(serverMetrics.keySet());
        Collections.sort(ls);
        return ls;
    }

}
