package org.safehaus.kiskis.mgmt.impl.hive.query;

import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;

public class Commands {


    private static final String EXEC_PROFILE = ". /etc/profile";
    private static String cmdSave = "echo '%s' > /opt/hive/query/query.cql && " + EXEC_PROFILE + " && hive -f /opt/hive/query/query.cql";

    public static Command geRunCommand(Agent agent, String query) {
        return HiveQueryImpl.getCommandRunner().createCommand(
                new RequestBuilder(
                        String.format(cmdSave, query)
                ).withTimeout(360),
                Sets.newHashSet(agent)
        );
    }
}
