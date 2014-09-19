package org.safehaus.subutai.server.ui.views;


import java.util.HashMap;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.subutai.server.ui.MainUI;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 9/17/14.
 */
public class CoreModulesView extends VerticalLayout implements View, PortalModuleListener {

    private static final Logger LOG = Logger.getLogger( MainUI.class.getName() );
    private TabSheet editors;
    private CssLayout modulesLayout;
    private HashMap<String, PortalModule> modules = new HashMap<>();
    private HashMap<String, AbstractLayout> moduleViews = new HashMap<>();


    @Override
    public void enter( ViewChangeListener.ViewChangeEvent event ) {
        setSizeFull();
        addStyleName( "reports" );

        addComponent( buildDraftsView() );
        getPortalModuleService().addListener( this );
    }


    private Component buildDraftsView() {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName( "borderless" );
        editors.addStyleName( "editors" );

        editors.setCloseHandler( new TabSheet.CloseHandler() {
            @Override
            public void onTabClose( TabSheet components, Component component ) {
                editors.removeComponent( component );
                modules.remove( component.getId() );
            }
        } );

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setCaption( "Modules" );
        titleAndDrafts.setSpacing( true );
        titleAndDrafts.addStyleName( "drafts" );
        editors.addComponent( titleAndDrafts );

        Label draftsTitle = new Label( "Core modules" );
        draftsTitle.addStyleName( "h1" );
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent( draftsTitle );
        titleAndDrafts.setComponentAlignment( draftsTitle, Alignment.TOP_CENTER );

        modulesLayout = new CssLayout();
        modulesLayout.setSizeUndefined();
        modulesLayout.addStyleName( "catalog" );
        titleAndDrafts.addComponent( modulesLayout );

        for ( PortalModule module : getPortalModuleService().getCoreModules() )
        {
            addModule( module );
        }

        return editors;
    }


    public static PortalModuleService getPortalModuleService() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle( PortalModuleService.class ).getBundleContext();
        if ( ctx != null )
        {
            ServiceReference serviceReference = ctx.getServiceReference( PortalModuleService.class.getName() );
            if ( serviceReference != null )
            {
                return PortalModuleService.class.cast( ctx.getService( serviceReference ) );
            }
        }

        return null;
    }


    private void addModule( final PortalModule module ) {

        ModuleView moduleView = new ModuleView( module, new ModuleView.ModuleViewListener() {
            @Override
            public void OnModuleClick( PortalModule module ) {
                if ( !modules.containsKey( module.getId() ) )
                {
                    autoCreate( module );
                    modules.put( module.getId(), module );
                }
            }
        } );
        moduleViews.put( module.getId(), moduleView );
        modulesLayout.addComponent( moduleView );
    }


    public void autoCreate( PortalModule module ) {
        Component component = module.createComponent();
        component.setId( module.getId() );
        TabSheet.Tab tab = editors.addTab( component );
        tab.setCaption( module.getName() );
        tab.setClosable( true );
        editors.setSelectedTab( tab );
    }


    @Override
    public void moduleRegistered( PortalModule module ) {
        if ( module.isCorePlugin() )
        {
            addModule( module );
        }
    }


    @Override
    public void moduleUnregistered( PortalModule module ) {
        if ( module.isCorePlugin() )
        {
            removeModule( module );
        }
    }


    private void removeModule( PortalModule module ) {
        ModuleView moduleView = ( ModuleView ) moduleViews.get( module.getId() );
        if ( moduleView != null )
        {
            modulesLayout.removeComponent( moduleView );
        }
    }
}