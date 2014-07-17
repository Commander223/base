/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.templateregistry;


import java.util.List;


/**
 * Class to work with templates registry, templates metadata
 */
public interface TemplateRegistryManager {

    /**
     * Adds template entry to registry
     *
     * @param configFile - template configuration file contents
     * @param packagesFile - template packages manifest file contents
     */
    public void registerTemplate( String configFile, String packagesFile );

    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     */
    public void unregisterTemplate( String templateName );

    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     */
    public void unregisterTemplate( String templateName, String lxcArch );

    /**
     * Returns template by name
     *
     * @param templateName - name of template
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName );

    /**
     * Returns template by name
     *
     * @param templateName - name of template
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName, String lxcArch );

    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName );

    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch );

    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName );

    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName, String lxcArch );

    /**
     * Returns templates in genealogical hierarchy
     *
     * @return - {@code TemplateTree}
     */
    public TemplateTree getTemplateTree();

    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName );

    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName, String lxcArch );
}
