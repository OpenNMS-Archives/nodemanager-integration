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


class TrapdConfigurationTest extends GroovyTestCase {

    public void testReadGenericWithDuplicate() {

        boolean resolverCalled = false;
        Resolver r = {
                resolverCalled = true;
                return "www.notknownbyanyone.com"
        } as Resolver;
        

        TrapdConfiguration trapdConf = new TrapdConfiguration();
        trapdConf.setTrapConf(new File("src/test/resources/trapd.conf"));
        trapdConf.setResolver(r);
        trapdConf.load();
        
        NNMEvent e = new NNMEvent(".1.3.6.1.6.3.1.1.5", 2, 0);
        e.setAgentAddress("192.168.1.1");
        
        EventFormat format = trapdConf.getFormat(e);
        assertNotNull(format);
        
        format.apply(e);
        
        assertEquals("192.168.1.1", e.getAgentAddress());
        assertEquals("SNMP_Link_Down", e.getName());
        assertEquals("LOGONLY",e.getCategory());
        assertEquals("Minor",e.getSeverity());
        assertTrue(resolverCalled);
        assertEquals("www.notknownbyanyone.com", e.getNodeLabel());
        
        
    }
    
    public void testFindFormatThatMatchedHost() {
        
        boolean resolverCalled = false;
        Resolver r = {
                resolverCalled = true;
                return "MIOT-G01.ippubbl.wind"
        } as Resolver;
        

        TrapdConfiguration trapdConf = new TrapdConfiguration();
        trapdConf.setTrapConf(new File("src/test/resources/trapd.conf"));
        trapdConf.setResolver(r);
        trapdConf.load();
        
        NNMEvent e = new NNMEvent(".1.3.6.1.6.3.1.1.5", 2, 0);
        e.setAgentAddress("192.168.1.1");
        
        EventFormat format = trapdConf.getFormat(e);
        assertNotNull(format);
        
        format.apply(e);
        
        assertEquals("192.168.1.1", e.getAgentAddress());
        assertEquals("SNMP_Link_Down_J20", e.getName());
        assertEquals("LOGONLY",e.getCategory());
        assertEquals("Minor",e.getSeverity());
        assertTrue(resolverCalled);
        assertEquals("MIOT-G01.ippubbl.wind", e.getNodeLabel());
    }
    
    public void testFindFormatThatMatchedWildcard() {
        
        boolean resolverCalled = false;
        Resolver r = {
                resolverCalled = true;
                return "BADE-E40.ippubbl.wind"
        } as Resolver;
        

        TrapdConfiguration trapdConf = new TrapdConfiguration();
        trapdConf.setTrapConf(new File("src/test/resources/trapd.conf"));
        trapdConf.setResolver(r);
        trapdConf.load();
        
        NNMEvent e = new NNMEvent(".1.3.6.1.4.1.9", 6, 1);
        e.setAgentAddress("192.168.1.1");
        
        EventFormat format = trapdConf.getFormat(e);
        assertNotNull(format); 
        
        format.apply(e);
        
        assertEquals("192.168.1.1", e.getAgentAddress());
        assertEquals("WindConnect", e.getName());
        assertEquals("Cisco IPBA",e.getCategory());
        assertEquals("Warning",e.getSeverity());
        assertTrue(resolverCalled);
        assertEquals("BADE-E40.ippubbl.wind", e.getNodeLabel());
    }
    
    public void testReadSimple() {

        boolean resolverCalled = false;
        Resolver r = {
                resolverCalled = true;
                return "www.notknownbyanyone.com"
        } as Resolver;
        
        TrapdConfiguration trapdConf = new TrapdConfiguration();
        trapdConf.setTrapConf(new File("src/test/resources/trapd.conf"));
        trapdConf.setResolver(r);
        trapdConf.load();
        
        NNMEvent e = new NNMEvent(".1.3.6.1.2.1.10.5", 6, 1);
        e.setAgentAddress("192.168.1.1");
        
        EventFormat format = trapdConf.getFormat(e);
        assertNotNull(format);
        
        format.apply(e);
        
        assertEquals("192.168.1.1", e.getAgentAddress());
        assertEquals("x25Restart", e.getName());
        assertEquals("Status Alarms",e.getCategory());
        assertEquals("Warning",e.getSeverity());
        assertEquals("www.notknownbyanyone.com", e.resolveNodeLabel(r));
        
        
    }
    
}
