package org.opennms.opennmsd;

import org.apache.log4j.Logger;

class TrapdConfiguration implements EventConfiguration {

    private static Logger log = Logger.getLogger(TrapdConfiguration.class)
    private def linePattern = ~/^\s*EVENT\s+(\S+)\s+([.0-9]+)\s+"(.*)"\s+(\S+)\s*$/;
    
    private File m_trapdConf;
    private Map m_names = [:];
    private Map m_categories = [:];
    private Map m_severity = [:];
    
    public void setTrapConf(File trapdConf) {
        m_trapdConf = trapdConf;
    }
    
    public void load() {
        
        m_trapdConf.eachLine { line ->
           def matcher = line =~ linePattern;
           if (matcher) {
               log.debug("MATCH: ${line}")
               def name = matcher.group(1)
               def key = matcher.group(2)
               def category = matcher.group(3)
               def severity = matcher.group(4)
               m_names.put(key, name)
               m_categories.put(key, category)
               m_severity.put(key, severity)
           } else {
               //log.debug("NOMCH: ${line}")
           }
        
            
        }
        
    }
    
    String getName(String key) {
        return m_names.get(key)
    }
    
    String getCategory(String key) {
        return m_categories.get(key)
    }
    
    String getSeverity(String key) {
        return m_severity.get(key)
    }


}