package org.opennms.opennmsd;

public class EventDescription {
    
    private String m_address;
    private String m_name;
    private String m_category;
    private String m_severity;
    private String m_eventObjectId;
    private String m_nodeLabel;
    
    public String getAddress() {
        return m_address;
    }
    public void setAddress(String address) {
        m_address = address;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public String getCategory() {
        return m_category;
    }
    public void setCategory(String category) {
        m_category = category;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    public String getEventObjectId() {
        return m_eventObjectId;
    }
    public void setEventObjectId(String objectId) {
        m_eventObjectId = objectId;
    }
    public String getNodeLabel() {
        return m_nodeLabel;
    }
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }
    

}
