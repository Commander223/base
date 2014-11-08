package org.safehaus.subutai.core.peer.impl;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.StrategyManager;


/**
 * Created by timur on 11/5/14.
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class LocalPeerImplTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    DataSource dataSource;
    @Mock
    Messenger messenger;
    @Mock
    AgentManager agentManager;

    @Mock
    TemplateRegistry templateRegistry;

    @Mock
    PeerDAO peerDAO;

    @Mock
    CommunicationManager communicationManager;

    @Mock
    CommandRunner commandRunner;

    @Mock
    QuotaManager quotaManager;

    @Mock
    StrategyManager strategyManager;


    @Before
    public void setup()
    {
        peerManager = new PeerManagerImpl( dataSource, messenger );
        //        peerManager.init();
    }


    @Test( expected = PeerException.class )
    public void testBindHostShouldFailOnNotExistenceHost() throws PeerException
    {
//        LocalPeerImpl localPeer =
        //                new LocalPeerImpl( peerManager, agentManager, templateRegistry, peerDAO, communicationManager,
        //                        commandRunner, quotaManager, strategyManager, null );
        //
        //        localPeer.bindHost( UUID.randomUUID() );
    }
}
