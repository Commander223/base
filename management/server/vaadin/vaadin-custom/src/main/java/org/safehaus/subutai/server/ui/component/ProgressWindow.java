/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;

import com.google.common.base.Strings;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class ProgressWindow {
    private final Window window;
    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final UUID trackID;
    private final Tracker tracker;
    private final String source;
    private final VerticalLayout l = new VerticalLayout();
    private volatile boolean track = true;
    private final ExecutorService executor;

    public ProgressWindow(final ExecutorService executor, Tracker tracker, UUID trackID, String source) {

        window = new Window("Operation progress", l);
        window.setModal(true);
        window.setClosable(false);
        window.setWidth(650, Sizeable.Unit.PIXELS);

        this.executor = Executors.newCachedThreadPool();
        this.trackID = trackID;
        this.tracker = tracker;
        this.source = source;

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(40);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        ok = new Button("Ok");
        ok.setStyleName("default");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                //close window
                track = false;
                executor.shutdown();
                window.close();
            }
        });

        indicator = new Label();
        indicator.setIcon(new ThemeResource("img/spinner.gif"));
        indicator.setContentMode(ContentMode.HTML);
        indicator.setHeight(11, Sizeable.Unit.PIXELS);
        indicator.setWidth(50, Sizeable.Unit.PIXELS);
        indicator.setVisible(false);

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        l.addComponent(content);
        start();
    }

    private void start() {

        showProgress();
        executor.execute(new Runnable() {

            public void run() {
                while (track) {
                    ProductOperationView po = tracker.getProductOperation(source, trackID);
                    if (po != null) {
                        System.out.println(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                        setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                        if (po.getState() != ProductOperationState.RUNNING) {
                            hideProgress();
                            break;
                        }
                    } else {
                        setOutput("Product operation not found. Check logs");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });

    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    private void setOutput(String output) {
        if (!Strings.isNullOrEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

    public Window getWindow() {
        return window;
    }
}
