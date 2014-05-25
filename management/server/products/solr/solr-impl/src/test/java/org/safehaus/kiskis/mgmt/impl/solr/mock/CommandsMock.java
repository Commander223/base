package org.safehaus.kiskis.mgmt.impl.solr.mock;


import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.shared.protocol.Agent;


public class CommandsMock extends Commands {

    private Command installCommand = null;
    private Command startCommand;
    private Command stopCommand;
    private Command statusCommand;


    public CommandsMock() {
        super( new CommandRunnerMock() );
    }


    @Override
    public Command getInstallCommand( Set<Agent> agents ) {
        return installCommand;
    }


    @Override
    public Command getStartCommand( Agent agent ) {
        return startCommand;
    }


    @Override
    public Command getStopCommand( Agent agent ) {
        return stopCommand;
    }


    @Override
    public Command getStatusCommand( Agent agent ) {
        return statusCommand;
    }


    public CommandsMock setInstallCommand( Command command ) {
        this.installCommand = command;
        return this;
    }
}
