package org.safehaus.subutai.core.backup.api;


import java.util.Date;
import java.util.UUID;


public interface BackupManager
{
    public void backupContainer( String containerName );


    public void restoreContainer( String containerName, UUID backupId );


    public void getRecentBackupList();


    public void getContainerBackupList( String containerName );


    public void getBackupsForPeriod( Date fromDate, Date toDate );
}