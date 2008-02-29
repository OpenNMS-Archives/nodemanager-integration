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
