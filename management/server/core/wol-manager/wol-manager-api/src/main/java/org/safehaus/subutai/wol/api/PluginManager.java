package org.safehaus.subutai.wol.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginManager
{
    public UUID installPlugin( String packageName );

    public UUID removePlugin( String packageName );

    public void upgradePlugin( String packageName );

    public Set<PluginInfo> getInstalledPlugins();

    public Set<PluginInfo> getAvailablePlugins();

    public List<String> getAvailablePluginNames();

    public List<String> getAvaileblePluginVersions();

    public List<String> getInstalledPluginVersions();

    public List<String> getInstalledPluginNames();

    public String getPluginVersion( String pluginName );

    public boolean isUpgradeAvailable( String pluginName);

    public String getProductKey();
}
