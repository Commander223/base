package org.safehaus.subutai.api.templateregistry;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;


/**
 * TemplateTree is used for storing templates in genealogical tree. This class is used by UI modules to visualize
 * template hierarchy.
 */
public class TemplateTree {

    Map<String, List<Template>> parentChild = new HashMap<>();
    Map<String, String> childParent = new HashMap<>();


    /**
     * Adds template to template tree
     *
     * @param template - {@code Template}
     */
    public void addTemplate( Template template ) {
        String parentTemplateName = Strings.isNullOrEmpty( template.getParentTemplateName() ) ? null :
                                    String.format( "%s-%s", template.getParentTemplateName().toLowerCase(),
                                            template.getLxcArch().toLowerCase() );
        List<Template> children = parentChild.get( parentTemplateName );
        if ( children == null ) {
            children = new LinkedList<>();
            parentChild.put( parentTemplateName, children );
        }
        children.add( template );
        childParent.put( String
                .format( "%s-%s", template.getTemplateName().toLowerCase(), template.getLxcArch().toLowerCase() ),
                parentTemplateName );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplate - template whose parent to return
     *
     * @return - parent template {@code Template}
     */
    public Template getParentTemplate( Template childTemplate ) {
        return getParentTemplate( childTemplate.getTemplateName(), childTemplate.getLxcArch() );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplateName - name of template whose parent to return
     *
     * @return - parent template {@code Template}
     */
    public Template getParentTemplate( String childTemplateName, String lxcArch ) {
        String parentTemplateName = getParentTemplateName( childTemplateName, lxcArch );
        if ( parentTemplateName != null ) {
            List<Template> templates =
                    getChildrenTemplates( getParentTemplateName( parentTemplateName, lxcArch ), lxcArch );
            if ( templates != null ) {
                for ( Template template : templates ) {
                    if ( parentTemplateName.equalsIgnoreCase( template.getTemplateName() ) && template.getLxcArch()
                                                                                                      .equalsIgnoreCase(
                                                                                                              lxcArch ) ) {
                        return template;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Returns parent template name of the supplied template or null if the template is root template
     *
     * @param childTemplateName - name of template whose parent template name to return
     *
     * @return - name of parent template {@code String}
     */
    public String getParentTemplateName( String childTemplateName, String lxcArch ) {
        if ( lxcArch != null ) {
            String childName = childTemplateName != null ?
                               String.format( "%s-%s", childTemplateName.toLowerCase(), lxcArch.toLowerCase() ) : null;
            String parentName = childParent.get( childName );

            if ( parentName != null ) {
                return parentName.replace( String.format( "-%s", lxcArch.toLowerCase() ), "" );
            }
        }
        return null;
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplateName - name of template whose children to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( String parentTemplateName, String lxcArch ) {
        if ( lxcArch != null ) {
            String parentName = parentTemplateName != null ?
                                String.format( "%s-%s", parentTemplateName.toLowerCase(), lxcArch.toLowerCase() ) :
                                null;
            return parentChild.get( parentName );
        }
        return null;
    }


    public List<Template> getRootTemplates() {
        return parentChild.get( null );
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplate - template whose children to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( Template parentTemplate ) {
        return getChildrenTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
    }
}
