package org.opennms.opennmsd;

import org.apache.log4j.Logger;

import org.opennms.opennmsd.AbstractConfiguration

class DefaultConfiguration extends AbstractConfiguration {
    
    private static Logger log = Logger.getLogger(DefaultConfiguration.class);
    
    private FilterChain m_chain;
    private String m_opennmsHost;
    private int m_port;
    
    public String getOpenNmsHost() {
        return m_opennmsHost;
    }
    
    public int getPort() {
        return m_port;
    }
    
    public void load() throws IOException {

        super.load();
        
        log.info("Parsing config file "+getConfigFile())
        
        Node config = new XmlParser().parse(getConfigFile());
        
        m_opennmsHost = config.'opennms-host'.'@host-address'[0];
        m_port = config.'opennms-host'.'@port'[0].toInteger();
        
        log.debug("opennmsHost is ${m_opennmsHost}")
        log.debug("port is ${m_port}")
        
        FilterChainBuilder bldr = new FilterChainBuilder();
        
        config.filters.filter.each { filter ->
            bldr.newFilter();
            bldr.setAction(filter.'@action')
            bldr.setAddressMatchPattern(filter.'@address');
            bldr.setCategoryMatchPattern(filter.'@category');
            bldr.setSeverityMatchPattern(filter.'@severity');
            bldr.setEventNameMatchPattern(filter.'@type');
            
            log.debug(bldr.getCurrentFilter())
        }
        
        m_chain = bldr.getChain();



        log.info("Config file parsed successfully")
        
    }

    public FilterChain getFilterChain() {
     	return m_chain;   
    }

    



}