/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hbase.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.api.hbase.Config;

/**
 * @author dilshat
 */
public class Wizard {

    private static final int MAX_STEPS = 3;
    private final VerticalLayout vlayout;
    private int step = 1;
    private Config config = new Config();
    private StepFinish stepFinish;

    public Wizard() {
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        vlayout.setMargin(true);
        putForm();
    }

    public Component getContent() {
        return vlayout;
    }

    public void next() {
        step++;
        putForm();
    }

    public void back() {
        step--;
        putForm();
    }

    public void cancel() {
        step = 1;
        putForm();
    }

    public void init() {
        step = 1;
        config = new Config();
        putForm();
    }

    public Config getConfig() {
        return config;
    }

    private void putForm() {
        vlayout.removeAllComponents();
        switch (step) {
            case 1: {
                vlayout.addComponent(new StepStart(this));
                break;
            }
            case 2: {
                vlayout.addComponent(new ConfigurationStep(this));
                break;
            }
            case 3: {
                vlayout.addComponent(new StepSetMaster(this));
                break;
            }
            case 4: {
                vlayout.addComponent(new StepSetRegion(this));
                break;
            }
            case 5: {
                vlayout.addComponent(new StepSetQuorum(this));
                break;
            }
            case 6: {
                vlayout.addComponent(new StepSetBackuUpMasters(this));
                break;
            }
            case 7: {
                vlayout.addComponent(new VerificationStep(this));
                break;
            }
            default: {
                step = 1;
                vlayout.addComponent(new StepStart(this));
                break;
            }
        }
    }

}
