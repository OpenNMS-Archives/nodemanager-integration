package org.opennms.opennmsd;

public interface EventConfiguration {
    
    EventFormat getFormat(NNMEvent event);

}
