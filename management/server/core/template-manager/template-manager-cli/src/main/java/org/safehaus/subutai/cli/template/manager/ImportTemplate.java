package org.safehaus.subutai.cli.template.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.template.manager.TemplateManager;

@Command(scope = "template", name = "import", description = "import template")
public class ImportTemplate extends OsgiCommandSupport {

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String templateName;

    public TemplateManager getTemplateManaget() {
        return templateManager;
    }

    public void setTemplateManaget(TemplateManager templateManaget) {
        this.templateManager = templateManaget;
    }

    @Override
    protected Object doExecute() throws Exception {
        boolean b = templateManager.importTemplate(hostName, templateName);
        if(b) System.out.println("Template successfully imported");
        else System.out.println("Failed to import");
        return null;
    }

}
