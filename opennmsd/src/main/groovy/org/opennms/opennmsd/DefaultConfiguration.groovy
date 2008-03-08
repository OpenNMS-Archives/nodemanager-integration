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

import org.opennms.opennmsd.AbstractConfiguration

class DefaultConfiguration extends AbstractConfiguration {
    
    private static Logger log = Logger.getLogger(DefaultConfiguration.class);
    
    private FilterChain m_chain;
    private String m_opennmsHost;
    private int m_port;
    private int m_maxBatchSize = 100;
    private int m_maxPreservedEvents = 300000;
    private int m_retryInterval = 1000;
    
    public String getOpenNmsHost() {
        return m_opennmsHost;
    }
    
    public int getPort() {
        return m_port;
    }
    
    public int getMaxBatchSize() {
        return m_maxBatchSize;
    }
    
    public int getMaxPreservedEvents() {
        return m_maxPreservedEvents;
    }


    public int getRetryInterval() {
        return m_retryInterval;
    }
    
    public void load() throws IOException {

        super.load();
        
        log.info("Parsing config file "+getConfigFile())
        
        Node config = new XmlParser().parse(getConfigFile());

        m_opennmsHost = config.'opennms-host'.'@host-address'[0];
        m_port = config.'opennms-host'.'@port'[0].toInteger();

        log.debug("opennmsHost is ${m_opennmsHost}")
        log.debug("port is ${m_port}")
        
        if (config.'opennms-host'.'@max-batch-size' && config.'opennms-host'.'@max-batch-size'[0]) {
            m_maxBatchSize = config.'opennms-host'.'@max-batch-size'[0].toInteger();
        }
        log.debug("maxBatchSize is ${m_maxBatchSize} default is 100")
        if (config.'opennms-host'.'@max-preserved-events' && config.'opennms-host'.'@max-preserved-events'[0]) {
            m_maxPreservedEvents = config.'opennms-host'.'@max-preserved-events'[0].toInteger();
        }
        log.debug("maxPreservedEvents is ${m_maxPreservedEvents} default is 300000")
        if (config.'opennms-host'.'@retry-interval' && config.'opennms-host'.'@retry-interval'[0]) {
            m_retryInterval = config.'opennms-host'.'@retry-interval'[0].toInteger();
        }
        log.debug("retryInterval is ${m_retryInterval} default is 1000")
        
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
