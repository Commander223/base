/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

/**
 * Status of command
 *
 * @author dilshat
 */
public enum CommandStatus {

    /**
     * command just created
     */
    NEW,
    /**
     * command has been just sent to agents
     */
    RUNNING,
    /**
     * command had timed out before agent sent response
     */
    TIMEDOUT,
    /**
     * command succeeded, exit code was 0
     */
    SUCCEEDED,
    /**
     * command failed, exit code was not 0 or agent interrupted command because
     * of timeout
     */
    FAILED
}
