/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

/**
 * This interface should be implemented for supplying expiry callbacks for
 * entries to {@code ExpiringCache}
 *
 * @author dilshat
 */
interface EntryExpiryCallback<ValueType> {

    /**
     * This method of callback is called when entry expires
     *
     * @param entry - cache entry being expired
     */
    public void onEntryExpiry(ValueType entry);
}
