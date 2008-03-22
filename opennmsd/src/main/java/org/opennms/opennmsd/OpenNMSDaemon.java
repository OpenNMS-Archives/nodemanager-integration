/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.opennmsd;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.ovapi.TrapProcessingDaemon;

public class OpenNMSDaemon extends TrapProcessingDaemon implements ProcessManagementListener, NNMEventListener {
    
    private static Logger log = Logger.getLogger(OpenNMSDaemon.class);
    
    private Configuration m_configuration;
    private EventForwarder m_eventForwarder;
    private NNMEventFactory m_eventFactory;
    
        
    public void setConfiguration(Configuration configuration) {
        m_configuration = configuration;
    }
    
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }
    
    public void setEventFactory(NNMEventFactory eventFactory) {
        m_eventFactory = eventFactory;
    }

    public String onInit() {
        Assert.notNull(m_configuration, "the configuration property must not be null");
        Assert.notNull(m_eventForwarder, "the eventForwarder property must not be null");
        
        super.onInit();
        
        sendStartEvent();
        
        return "Initialization complete.";
    }
    
    private void sendStartEvent() {
        m_eventForwarder.start();
    }
    
    public String onStop() {
        
        m_eventForwarder.stop();
        
        super.onStop();
        
        return "opennms stopped successfully.";
        
    }
    
    public void onEvent(NNMEvent event) {
        FilterChain chain = m_configuration.getFilterChain();
        
        
        String action = chain.filterEvent(event);
        
        log.debug("received an event to be filtered "+event+" action is "+action);
        
        if (Filter.PRESERVE.equals(action)) {
            m_eventForwarder.preserve(event);
        } else if (Filter.ACCEPT.equals(action)) {
            m_eventForwarder.accept(event);
        } else {
            m_eventForwarder.discard(event);
        }
        
    }
    
    protected void onEvent(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
        
        try {
            NNMEvent event = m_eventFactory.createEvent(pdu);
            if (event != null) {
                onEvent(event);
            }
        } catch (Exception e) {
            log.debug("Exception processing pdu: "+pdu, e);
        } finally {
            pdu.delete();
        }
        
        
    }
    
    
}
