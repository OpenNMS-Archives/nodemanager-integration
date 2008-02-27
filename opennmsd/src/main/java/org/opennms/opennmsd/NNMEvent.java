package org.opennms.opennmsd;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NNMEvent {

    private String m_category;
    private String m_name;
    private String m_severity;
    private String m_sourceAddress;
    private Date m_timeStamp;
    private String m_community;
    private String m_snmpHost;
    private int m_version;
    private List m_varBinds;
    private EventIdentity m_eventIdentity;

    protected void addVarBind(NNMVarBind varBind) {
        if (m_varBinds == null) {
            m_varBinds = new LinkedList();
        }
        
        m_varBinds.add(varBind);
        
    }
    
    protected void addVarBind() {
        
    }

    public String getCategory() {
        return m_category;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getSeverity() {
        return m_severity;
    }

    public void setSeverity(String severity) {
        m_severity = severity;
    }

    public String getSourceAddress() {
        return m_sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        m_sourceAddress = sourceAddress;
    }

    public Date getTimeStamp() {
        return m_timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        m_timeStamp = timeStamp;
    }

    public String getCommunity() {
        return m_community;
    }

    public void setCommunity(String community) {
        m_community = community;
    }
    
    public String getEnterpriseId() {
        return m_eventIdentity.getEnterpriseId();
    }

    public int getGeneric() {
        return m_eventIdentity.getGeneric();
    }

    public int getSpecific() {
        return m_eventIdentity.getSpecific();
    }

    public String getSnmpHost() {
        return m_snmpHost;
    }

    public void setSnmpHost(String snmpHost) {
        m_snmpHost = snmpHost;
    }

    public int getVersion() {
        return m_version;
    }

    public void setVersion(int version) {
        m_version = version;
    }

    public List getVarBinds() {
        return m_varBinds;
    }

    public void setVarBinds(List varBinds) {
        m_varBinds = varBinds;
    }

    public String getEventConfigurationKey() {
        return m_eventIdentity.getEventObjectId();
    }
    
    public String toString() {
        return "NNMEvent[name="+getName()+", address="+getSourceAddress()+", category="+getCategory()+", severity="+getSeverity()+", key="+getEventConfigurationKey()+"]";
    }

    public static NNMEvent createEvent(String category, String severity,
            String name, String address) {
        NNMEvent event = new NNMEvent();
        event.setCategory(category);
        event.setName(name);
        event.setSourceAddress(address);
        event.setSeverity(severity);
        event.setTimeStamp(new Date());
        return event;
    }

    public void addVarBind(String objectId, String type, String varbind) {
        addVarBind(new DefaultNNMVarBind(objectId, type, varbind));
    }

    public void setEventIdentity(EventIdentity id) {
        m_eventIdentity = id;
    }
    
    public EventIdentity getEventIdentity() {
        return m_eventIdentity;
    }
    
    
    


}
