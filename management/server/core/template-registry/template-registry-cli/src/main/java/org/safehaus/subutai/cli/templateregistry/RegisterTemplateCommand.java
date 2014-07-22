package org.safehaus.subutai.cli.templateregistry;


import java.nio.charset.Charset;

import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.shared.protocol.FileUtil;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.registerTemplate command
 */
@Command(scope = "registry", name = "register-template", description = "Register template with registry")
public class RegisterTemplateCommand extends OsgiCommandSupport {
    @Argument(index = 0, name = "path to template config file", required = true, multiValued = false,
            description = "path to template config file")
    String configFilePath;
    @Argument(index = 1, name = "path to template packages file", required = true, multiValued = false,
            description = "path to template packages file")
    String packagesFilePath;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        templateRegistryManager.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ) );

        System.out.println( "Template registered successfully" );

        return null;
    }
}
