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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpVarBind;
import org.opennms.ovapi.OVsnmpPduUtils;

public class DefaultNNMEventFactory implements NNMEventFactory {
    
    private static Logger log = Logger.getLogger(DefaultNNMEventFactory.class);

    private EventConfiguration m_eventConfiguration;

    public void setEventConfiguation(EventConfiguration eventConfiguration) {
        m_eventConfiguration = eventConfiguration;
    }
    
    public NNMEvent createEvent(OVsnmpPdu trap) {
        
        // add trap information to event
        NNMEvent e = new NNMEvent(trap.getEnterpriseObjectId(), trap.getGenericType(), trap.getSpecificType());
        
        e.setAgentAddress(trap.getAgentAddress());
        e.setSnmpHost(trap.getIpAddress());
        e.setTimeStamp(new Date());
        e.setCommunity(trap.getCommunity());
        e.setVersion(1);
        
        for(OVsnmpVarBind varBind = trap.getVarBinds(); varBind != null; varBind = varBind.getNextVarBind()) 
        {
            e.addVarBind(varBind.getObjectId(), 
                    OVsnmpPduUtils.getTypeString(varBind.getType()),
                    OVsnmpPduUtils.getVarbindValue(varBind));
     
            
        }

        // fix up event fields for nnm-internal events
        String newAddr = e.getVarBindValue(".1.3.6.1.4.1.11.2.17.2.2.0");
        if (newAddr != null) {
            try {
                InetAddress addr = InetAddress.getByName(newAddr);
                String iface = addr.getHostAddress();
                String nodeLabel = addr.getHostName();
                e.setAgentAddress(iface);
                e.setNodeLabel(nodeLabel);
                
            } catch (UnknownHostException ex) {
                // this is the normal case so do nothing
                log.info("Unable to resolve "+newAddr+" as hostname/ipAddress for event "+e+" using trap supplied values");
            }
        }

        // add formatting information to event
        EventFormat format = m_eventConfiguration.getFormat(e);
        format.apply(e);

        return e;
    }


}
