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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.util.Date;

/**
 * An event used to represent the Status of opennmsd
 *
 * @author brozow
 */
public class StatusEvent implements Event {

    public static StatusEvent createStartEvent() {
    	StatusEvent e = new StatusEvent("uei.opennms.org/external/nnm/opennmsdStart");
    	try {
        	e.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
    	} catch (Exception excp) {
    		
    	}
    	return e;
    }

    public static StatusEvent createStopEvent() {
    	StatusEvent e = new StatusEvent("uei.opennms.org/external/nnm/opennmsdStop");
       	try {
        	e.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
    	} catch (Exception excp) {
    		
    	}
    	return e;
    }

    public static StatusEvent createSyncLostEvent() {
	StatusEvent e = new StatusEvent("uei.opennms.org/external/nnm/opennmsdSyncLost");
	e.setPreserved(true);
   	try {
    	e.setAgentAddress(InetAddress.getLocalHost().getHostAddress());
	} catch (Exception excp) {
		
	}
	return e;
    }

  
    
    private String m_uei;
    private Date m_timeStamp;
    private boolean m_preserved;
    private String m_agentAddress;

    // This fields caches the resolved agentAddress for using in forwarding
    private String m_nodeLabel;

    
    public String getAgentAddress() {
		return m_agentAddress;
	}

	public void setAgentAddress(String address) {
		m_agentAddress = address;
	}

	public String getNodeLabel() {
		return m_nodeLabel;
	}

	public void setNodeLabel(String label) {
		m_nodeLabel = label;
	}

	public StatusEvent(String uei) {
	this(uei, new Date());
    }

    public StatusEvent(String uei, Date timeStamp) {
	m_uei = uei;
        m_timeStamp = timeStamp;
    }
    
    public String getUei() {
        return m_uei;
    }
    
    public Date getTimeStamp() {
        return m_timeStamp;
    }

    public void setTimeStamp(Date timestamp) {
        m_timeStamp = timestamp;
    }

    public boolean isPreserved() {
        return m_preserved;
    }
    
    public void setPreserved(boolean preserved) {
        m_preserved = preserved;
    }

    public String resolveNodeLabel(Resolver r) {
        if (m_nodeLabel == null) {
            m_nodeLabel = r.resolveAddress(getAgentAddress());
        }
        return m_nodeLabel;
    }
 
}
