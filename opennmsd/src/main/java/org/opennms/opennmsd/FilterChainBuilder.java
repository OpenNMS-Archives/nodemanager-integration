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

public class FilterChainBuilder {
    
    private FilterChain m_chain;
    private Filter m_currentFilter;
    
    public FilterChainBuilder() {
        m_chain = new FilterChain();
    }
    
    public FilterChain getChain() {
        return m_chain;
    }
    
    public FilterChainBuilder newFilter() {
        m_currentFilter = new Filter();
        m_chain.addFilter(m_currentFilter);
        return this;
    }
    
    public FilterChainBuilder setCategoryMatchPattern(String regexp) {
        m_currentFilter.setCategoryMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setSeverityMatchPattern(String regexp) {
        m_currentFilter.setSeverityMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setEventNameMatchPattern(String regexp) {
        m_currentFilter.setEventNameMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setAddressMatchPattern(String iplikeStyleAddressPattern) {
        m_currentFilter.setAddressMatchSpec(iplikeStyleAddressPattern);
        return this;
    }
    
    public FilterChainBuilder setAction(String action) {
        assertValidAction(action);
        m_currentFilter.setAction(action);
        return this;
    }
    
    public Filter getCurrentFilter() {
        return m_currentFilter;
    }
    
    public void assertValidAction(String action) {
        Assert.isTrue("Action must be one of accept, preserve or discard",
                (Filter.ACCEPT.equals(action) || Filter.DISCARD.equals(action) || Filter.PRESERVE.equals(action)));
    }

}
