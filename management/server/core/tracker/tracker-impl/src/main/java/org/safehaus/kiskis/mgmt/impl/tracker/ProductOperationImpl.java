/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.tracker;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;

import java.util.Date;
import java.util.UUID;

/**
 * This is an implementaion of ProductOperation
 *
 * @author dilshat
 */
public class ProductOperationImpl implements ProductOperation {

    /**
     * product operation id
     */
    private final UUID id;
    /**
     * product operation description
     */
    private final String description;
    /**
     * reference to tracker
     */
    private final transient TrackerImpl tracker;

    /**
     * log of product operation
     */
    private final StringBuilder log;
    /**
     * Creation date of product operation
     */
    private final Date createDate;
    /**
     * Source of product operation
     */
    private final String source;
    /**
     * State of product operation
     */
    private ProductOperationState state;

    public ProductOperationImpl(String source, String description, TrackerImpl tracker) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(source), "Source is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(description), "Description is null or empty");
        Preconditions.checkNotNull(tracker, "Tracker is null");

        this.description = description;
        this.source = source;
        this.tracker = tracker;
        log = new StringBuilder();
        state = ProductOperationState.RUNNING;
        id = UUID.fromString(new com.eaio.uuid.UUID().toString());
        createDate = new Date();
    }

    public String getLog() {
        return log.toString();
    }

    public void addLog(String logString) {
        addLog(logString, state);
    }

    public void addLogDone(String logString) {
        addLog(logString, ProductOperationState.SUCCEEDED);
    }

    public void addLogFailed(String logString) {
        addLog(logString, ProductOperationState.FAILED);
    }

    private void addLog(String logString, ProductOperationState state) {
        if (!Strings.isNullOrEmpty(logString)) {

            if (log.length() > 0) {
                log.append("\n");
            }
            log.append(logString);
        }
        this.state = state;
        tracker.saveProductOperation(source, this);
    }

    public ProductOperationState getState() {
        return state;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProductOperationImpl other = (ProductOperationImpl) obj;
        return !(this.id != other.id && (this.id == null || !this.id.equals(other.id)));
    }

    public Date createDate() {
        return createDate;
    }

}
