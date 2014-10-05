package org.safehaus.subutai.server.ui.views;


import org.safehaus.subutai.server.ui.api.PortalModule;

import com.google.common.base.Preconditions;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.FileResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;


/**
 * Created by talas on 9/16/14.
 */
public class ModuleView extends CssLayout
{

    private PortalModule mModule;
    private ModuleViewListener mClickListener;


    public ModuleView( PortalModule module, ModuleViewListener clickListener )
    {
        Preconditions.checkNotNull( module, "Module is null" );

        this.mModule = module;
        this.mClickListener = clickListener;

        setId( module.getId() );
        setWidth( 150, Unit.PIXELS );
        setHeight( 200, Unit.PIXELS );
        addStyleName( "create" );

        addLayoutClickListener( new LayoutEvents.LayoutClickListener()
        {
            @Override
            public void layoutClick( LayoutEvents.LayoutClickEvent layoutClickEvent )
            {
                if ( mClickListener != null )
                {
                    mClickListener.OnModuleClick( mModule );
                }
            }
        } );

        Image image = new Image( "", new FileResource( module.getImage() ) );
        image.setWidth( 90, Unit.PERCENTAGE );
        image.setDescription( module.getName() );
        addComponent( image );
    }


    public interface ModuleViewListener
    {
        public void OnModuleClick( PortalModule module );
    }
}
