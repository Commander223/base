/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * Implementation of AgentResult interface
 *
 * @author dilshat
 */
public class AgentResultImpl implements AgentResult {

    //tagent UUID
    private final UUID agentUUID;
    //std out of command execution
    private final StringBuilder stdOut = new StringBuilder();
    //std err of command execution
    private final StringBuilder stdErr = new StringBuilder();
    //exit code of command execution
    private Integer exitCode;

    /**
     * Constructor
     *
     * @param agentUUID - UUID of agent
     */
    public AgentResultImpl(UUID agentUUID) {
        Preconditions.checkNotNull(agentUUID, "Agent UUID is null");

        this.agentUUID = agentUUID;
    }

    /**
     * When a response arrives this method is called by command runner to append
     * results of command execution to this object
     *
     * @param response
     */
    public void appendResults(Response response) {
        if (response != null && exitCode == null && agentUUID.equals(response.getUuid())) {
            if (!Strings.isNullOrEmpty(response.getStdOut())) {
                stdOut.append(response.getStdOut());
            }
            if (!Strings.isNullOrEmpty(response.getStdErr())) {
                stdOut.append(response.getStdErr());
            }
            if (response.isFinal() && response.getExitCode() != null) {
                exitCode = response.getExitCode();
            }
        }
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public String getStdErr() {
        return stdErr.toString();
    }

    public UUID getAgentUUID() {
        return agentUUID;
    }

}
