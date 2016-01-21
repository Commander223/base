package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
    private final Topology topology;
    private final Environment environment;
    private final PeerManager peerManager;
    private final User user;
    private SecurityManager securityManager;


    public PEKGenerationStep( final Topology topology, final Environment environment, final PeerManager peerManager,
                              SecurityManager securityManager, User user )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.user = user;
    }


    //TODO this EK should be uploaded by user when creating environment via UI. @Nurkaly!
    private PGPSecretKeyRing createEnvironmentKeyPair( EnvironmentId envId, String userSecKeyId ) throws PeerException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        String pairId = envId.getId();
        final PGPSecretKeyRing userSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( userSecKeyId );
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), pubRing );

            //***************Sign Keys *********************************************************
            securityManager.getKeyManager().setKeyTrust( userSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            return secRing;
        }
        catch ( PGPException ex )
        {
            throw new PeerException( ex );
        }
    }


    public Map<Peer, String> execute() throws PeerException
    {
        PGPSecretKeyRing envSecKeyRing =
                securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.add( peerManager.getLocalPeer() );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        for ( final Peer peer : peers )
        {
            try
            {
                PublicKeyContainer publicKeyContainer =
                        peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

                PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

                PGPPublicKeyRing signedPEK = securityManager.getKeyManager().setKeyTrust( envSecKeyRing, pubRing,
                        KeyTrustLevel.Full.getId() );

                peer.updatePeerEnvironmentPubKey( environment.getEnvironmentId(), signedPEK );
            }
            catch ( PGPException e )
            {
                throw new PeerException( "Could not create PEK for: " + peer.getId() );
            }
        }

        return peerPekPubKeys;
    }
}
