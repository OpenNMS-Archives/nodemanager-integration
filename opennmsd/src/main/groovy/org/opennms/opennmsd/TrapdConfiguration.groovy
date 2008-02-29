package org.opennms.opennmsd;

import org.apache.log4j.Logger;

import java.util.regex.*;


class TrapdConfiguration implements EventConfiguration {

    private static Logger log = Logger.getLogger(TrapdConfiguration.class)
    private def beginEventDescPattern = ~/^\s*EVENT\s+(\S+)\s+([.0-9*]+)\s+"(.*)"\s+(\S+)\s*$/;
    private def nodeLinePattern =  ~/^\s*NODES((:?\s+\S+)+)\s*$/
    
    private File m_trapdConf;
    private Resolver m_resolver;
    private Map m_names = [:];
    private Map m_categories = [:];
    private Map m_severity = [:];
    private Map m_defs = [:];
    
    private EventFormat[] m_formats;
    
    public void setTrapConf(File trapdConf) {
        m_trapdConf = trapdConf;
    }
    
    public void setResolver(Resolver resolver) {
        m_resolver = resolver;
    }
    
    public void load() {
        
        assert m_trapdConf != null
        assert m_resolver != null
        
        def eventFormat;
        int index = 0;
        List formats = [];
        m_trapdConf.eachLine { line ->
           switch (line) {
           case beginEventDescPattern:
               log.debug("MATCH: ${line}")

               Matcher matcher = Matcher.lastMatcher;
               assert matcher != null;

               def eventOid = matcher.group(2)

               eventFormat = new EventFormat(index++, eventOid);
               
               eventFormat.name = matcher.group(1);
               eventFormat.category = matcher.group(3);
               eventFormat.severity = matcher.group(4);

               formats.add(eventFormat);

               break;

           case nodeLinePattern: 
               log.debug("NODES: ${line}")
               def matcher = Matcher.lastMatcher;
               assert matcher != null;
               def hosts = matcher.group(1).trim().split();
               eventFormat.hosts = hosts;
               break;

           }
        
            
        }
        
        // these are sorted after the fact because the data necessary to sort is not
        // all available at the point the are added to the list so we cannot use a sorted collection
        m_formats = formats.toArray();
        
        Arrays.sort(m_formats);
        
        int i = 0;
        for(format in m_formats) {
            System.err.println((i++)+": "+format);
        }
        
    }
    
    public EventFormat getFormat(NNMEvent event) {
        for(format in m_formats) {
            if (format.matches(event, m_resolver)) {
                return format;
            }
        }
        return null;
    }
    
}