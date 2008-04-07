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

import org.opennms.opennmsd.NNMEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.opennms.opennmsd.StatusEvent;
import groovy.xml.MarkupBuilder;

class EventMarshalMethodsTest extends GroovyTestCase {
    String m_host = InetAddress.getLocalHost().hostName;

    void testMarshallNNMEvent() {
        NNMEvent e = new NNMEvent(".1.2.3.4", 6, 5);
        e.setAgentAddress("192.168.1.1")
        e.setSnmpHost("192.168.1.2")
        e.setTimeStamp(new Date(1204996817484L));
        e.setCommunity("public")
        e.setVersion(1)
        e.addVarBind(new DefaultNNMVarBind("text", "1.3.5.7", "OctetString", "varbind1"))
        e.addVarBind(new DefaultNNMVarBind("text", "1.3.5.8", "OctetString", "varbind2"))
        e.setNodeLabel("node")
        e.setName("testEvent")
        
        String actualXml = marshalIt(e)
        
        String expectedXml = 
"""<event>
  <uei>uei.opennms.org/external/nnm/testEvent</uei>
  <source>opennmsd</source>
  <time>Saturday, March 8, 2008 5:20:17 PM GMT</time>
  <host>${m_host}</host>
  <interface>192.168.1.1</interface>
  <snmphost>192.168.1.2</snmphost>
  <snmp>
    <id>.1.2.3.4</id>
    <generic>6</generic>
    <specific>5</specific>
    <version>1</version>
    <community>public</community>
  </snmp>
  <parms>
    <parm>
      <parmName>1.3.5.7</parmName>
      <value encoding='text'>varbind1</value>
    </parm>
    <parm>
      <parmName>1.3.5.8</parmName>
      <value encoding='text'>varbind2</value>
    </parm>
    <parm>
      <parmName>nnmEventOid</parmName>
      <value encoding='text'>.1.2.3.4.0.5</value>
    </parm>
    <parm>
      <parmName>nodelabel</parmName>
      <value encoding='text'>node</value>
    </parm>
  </parms>
</event>"""

        
        assertEquals(expectedXml, actualXml)
        
    }
    
    void testMarshallStatusEvent() {
        String expectedXml = 
"""<event>
  <uei>uei.opennms.org/external/nnm/opennmsdStop</uei>
  <source>opennmsd</source>
  <time>Saturday, March 8, 2008 5:20:17 PM GMT</time>
  <host>${m_host}</host>
</event>"""

        StatusEvent e = new StatusEvent("uei.opennms.org/external/nnm/opennmsdStop");
        // this time corresponds to the date above
        e.setTimeStamp(new Date(1204996817484L))
        
        String actualXml = marshalIt(e);
        
        assertEquals(expectedXml, actualXml)
        
    }
    
    String marshalIt(def event) {
        StringWriter s = new StringWriter();
        MarkupBuilder xml = new MarkupBuilder(s)
        use (EventMarshalMethods) {
            event.marshal(xml)
        }
        return s.toString();
    }
    
}
