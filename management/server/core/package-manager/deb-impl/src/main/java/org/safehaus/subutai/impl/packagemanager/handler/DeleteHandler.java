package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;
import org.safehaus.subutai.shared.protocol.Agent;

public class DeleteHandler extends AbstractHandler<Boolean> {

    public DeleteHandler(DebPackageManager pm, String hostname) {
        super(pm, hostname);
    }

    @Override
    public Boolean performAction() {
        FindHandler h = new FindHandler(packageManager, hostname);
        Collection<PackageInfo> col = h.performAction();
        if(col == null) return Boolean.FALSE;

        Agent a = getAgent();
        if(a == null) return false;

        RequestBuilder rb = new RequestBuilder("rm " + packageManager.getFilename())
                .withCwd(packageManager.getLocation());
        Command cmd = packageManager.getCommandRunner().createCommand(rb,
                new HashSet<>(Arrays.asList(a)));
        packageManager.getCommandRunner().runCommand(cmd);
        return cmd.hasSucceeded();
    }

}
