package org.safehaus.subutai.impl.lucene.handler;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.impl.lucene.Commands;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest {

	private static Commands commands;

	@BeforeClass
	public static void setUp() {
		commands = new Commands(new CommandRunnerMock());
	}


	@Test
	public void testInstallCommand() {
		Command command = commands.getInstallCommand(null);

		assertNotNull(command);
		assertEquals(Commands.INSTALL, command.getDescription());
	}


	@Test
	public void testUninstallCommand() {
		Command command = commands.getUninstallCommand(null);

		assertNotNull(command);
		assertEquals(Commands.UNINSTALL, command.getDescription());
	}


	@Test
	public void testCheckCommand() {
		Command command = commands.getCheckInstalledCommand(null);

		assertNotNull(command);
		assertEquals(Commands.CHECK, command.getDescription());
	}

}
