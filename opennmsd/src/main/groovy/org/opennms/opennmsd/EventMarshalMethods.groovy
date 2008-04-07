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
package org.opennms.opennmsd

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger
import groovy.xml.MarkupBuilder


abstract class EventMarshalMethods {
    private static Logger s_log = Logger.getLogger(EventMarshalMethods.class);
    private static String s_host = InetAddress.getLocalHost().hostName;
    private static DateFormat s_dateFormat = null;
    
    public static String formatForEvent(Date date) {
        return getDateFormat().format(date);
    }
    
    private static DateFormat getDateFormat() {
        if (s_dateFormat == null) {
            s_dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            s_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return s_dateFormat;
    }
    
     public static void marshal(NNMEvent e, MarkupBuilder xml) {
         xml.event {
             uei("uei.opennms.org/external/nnm/${e.name}")
             source("opennmsd")
             time(formatForEvent(e.timeStamp))
             host(s_host)
             'interface'(e.agentAddress)
             snmphost(e.snmpHost)
             snmp {
                 id(e.enterpriseId)
                 generic(e.generic)
                 specific(e.specific)
                 version(e.version)
                 community(e.community)
                 //'time-stamp'(m_dateFormat.format(e.timeStamp))
             }
             List varBinds = e.varBinds;
             parms {
                 for(NNMVarBind v in varBinds) {   
                     s_log.debug("adding varbind")
                     s_log.debug("varbind ${v.objectId}=${v.encoding}(${v.value})")
                     parm {
                         parmName(v.objectId)
                         value(encoding:v.encoding, v.value)
                     }                              
                 }
                 parm {
                     parmName("nnmEventOid")
                     value(encoding:'text', e.eventObjectId)
                 }
                 parm {
                     parmName("nodelabel")
                     value(encoding:'text', e.nodeLabel)
                 }
             }
         }
     }
     
     public static void marshal(StatusEvent e, MarkupBuilder xml) {
         xml.
         event {
             uei(e.uei)
             source("opennmsd")
             time(formatForEvent(e.timeStamp))
             host(s_host)
         }

     }
}
