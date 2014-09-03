package org.safehaus.subutai.product.common.test.unit.mock;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.commandrunner.api.*;

import java.util.Set;
import java.util.UUID;


public class CommandRunnerMock implements CommandRunner
{

	@Override
	public void runCommandAsync(Command command, CommandCallback commandCallback) {

	}


	@Override
	public void runCommandAsync(Command command) {

	}


	@Override
	public void runCommand(Command command) {

	}


	@Override
	public void runCommand(Command command, CommandCallback commandCallback) {

	}


	@Override
	public Command createCommand(RequestBuilder requestBuilder, Set<Agent> agents) {
		Request request = requestBuilder.build(UUID.randomUUID(), UUID.randomUUID());
		return new CommandMock().setDescription(request.getProgram());
	}


	@Override
	public Command createCommand(String description, RequestBuilder requestBuilder, Set<Agent> agents) {
		return null;
	}


	@Override
	public Command createCommand(Set<AgentRequestBuilder> agentRequestBuilders) {
		return null;
	}


	@Override
	public Command createCommand(String description, Set<AgentRequestBuilder> agentRequestBuilders) {
		return null;
	}


	@Override
	public Command createBroadcastCommand(final RequestBuilder requestBuilder) {
		return null;
	}
}
