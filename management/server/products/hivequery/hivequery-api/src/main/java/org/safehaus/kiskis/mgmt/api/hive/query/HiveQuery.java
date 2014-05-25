package org.safehaus.kiskis.mgmt.api.hive.query;

import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.List;
import java.util.UUID;

public interface HiveQuery extends ApiBase<Config> {
    public boolean save(Config config);

    public boolean save(String name, String query, String description);

    public UUID run(String hostname, String query);

    public List<Config> load();

    public List<org.safehaus.subutai.api.hadoop.Config> getHadoopClusters();
}
