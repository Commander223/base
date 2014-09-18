package org.safehaus.subutai.core.peer.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerUI implements PortalModule {

    public static final String MODULE_IMAGE = "peer.png";
    public static final String MODULE_NAME = "Peer";
    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager(final PeerManager peerManager) {
        this.peerManager = peerManager;
    }


    public void init() {
    }


    public void destroy() {
    }


    @Override
    public String getId() {
        return MODULE_NAME;
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile(MODULE_IMAGE, this);
    }


    @Override
    public Component createComponent() {
        return new PeerForm(this);
    }

    @Override
    public Boolean isCorePlugin() {
        return true;
    }
}
