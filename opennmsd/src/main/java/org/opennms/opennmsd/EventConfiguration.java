package org.opennms.opennmsd;

public interface EventConfiguration {
    
    EventDescription getDescription(EventIdentity identity, String agentAddress);

}
