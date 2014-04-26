/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.spark;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes install ksks-spark")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );
    }

    public static Command getUninstallCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-spark")
                        .withTimeout(60),
                agents
        );
    }

    public static Command getCheckInstalledCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
                agents);
    }

    public static Command getStartAllCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder("service spark-all start").withTimeout(360),
                Util.wrapAgentToSet(masterNode));
    }

    public static Command getStopAllCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder("service spark-all stop").withTimeout(60),
                Util.wrapAgentToSet(masterNode));
    }

    public static Command getStatusAllCommand(Agent agent) {
        return createCommand(
                new RequestBuilder("service spark-all status").withTimeout(60),
                Util.wrapAgentToSet(agent));
    }

    public static Command getStartMasterCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder("service spark-master start").withTimeout(90),
                Util.wrapAgentToSet(masterNode));
    }

    public static Command getRestartMasterCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder("service spark-master stop && service spark-master start")
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

    public static Command getStopMasterCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder("service spark-master stop")
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

    public static Command getStartSlaveCommand(Agent slaveNode) {
        return createCommand(
                new RequestBuilder("service spark-slave start")
                        .withTimeout(90),
                Util.wrapAgentToSet(slaveNode)
        );
    }

    public static Command getStopSlaveCommand(Agent slaveNode) {
        return createCommand(
                new RequestBuilder("service spark-slave stop")
                        .withTimeout(60),
                Util.wrapAgentToSet(slaveNode)
        );
    }

    public static Command getSetMasterIPCommand(Agent masterNode, Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && sparkMasterConf.sh clear ; sparkMasterConf.sh %s", masterNode.getHostname()))
                        .withTimeout(60),
                agents
        );
    }

    public static Command getClearSlavesCommand(Agent masterNode) {
        return createCommand(
                new RequestBuilder(". /etc/profile && sparkSlaveConf.sh clear")
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

    public static Command getClearSlaveCommand(Agent slave, Agent masterNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && sparkSlaveConf.sh clear %s", slave.getHostname()))
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

    public static Command getAddSlaveCommand(Agent slave, Agent masterNode) {
        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && sparkSlaveConf.sh %s", slave.getHostname()))
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

    public static Command getAddSlavesCommand(Set<Agent> slaveNodes, Agent masterNode) {
        StringBuilder slaves = new StringBuilder();
        for (Agent slaveNode : slaveNodes) {
            slaves.append(slaveNode.getHostname()).append(" ");
        }

        return createCommand(
                new RequestBuilder(String.format(". /etc/profile && sparkSlaveConf.sh clear ; sparkSlaveConf.sh %s", slaves))
                        .withTimeout(60),
                Util.wrapAgentToSet(masterNode)
        );
    }

}
