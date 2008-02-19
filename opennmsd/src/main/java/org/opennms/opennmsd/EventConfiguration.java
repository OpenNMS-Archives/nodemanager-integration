package org.opennms.opennmsd;

public interface EventConfiguration {
    
    String getName(String key);
    
    String getCategory(String key);
    
    String getSeverity(String key);

}
