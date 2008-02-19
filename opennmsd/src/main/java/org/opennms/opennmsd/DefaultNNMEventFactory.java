package org.opennms.opennmsd;

import org.opennms.nnm.swig.OVsnmpPdu;

public class DefaultNNMEventFactory implements NNMEventFactory {
    
    private EventConfiguration m_eventConfiguration;
    public void setEventConfiguation(EventConfiguration eventConfiguration) {
        m_eventConfiguration = eventConfiguration;
    }

    public NNMEvent createEvent(OVsnmpPdu trap) {
        DefaultNNMEvent event = new DefaultNNMEvent(trap);
        String key = event.getEventConfigurationKey();
        event.setName(m_eventConfiguration.getName(key));
        event.setCategory(m_eventConfiguration.getCategory(key));
        event.setSeverity(m_eventConfiguration.getSeverity(key));
        return event;
    }

}
