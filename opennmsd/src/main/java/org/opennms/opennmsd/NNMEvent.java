package org.opennms.opennmsd;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.nnm.SnmpObjId;

public class NNMEvent {

    // fields populated directly from the trap
    private EventIdentity m_eventIdentity;
    private String m_agentAddress;
    private Date m_timeStamp;
    private String m_community;
    private String m_snmpHost;
    private int m_version;
    private List m_varBinds;

    // fields populated from the eventFormatter (trapd.conf)
    private String m_category;
    private String m_name;
    private String m_severity;
    
    // This fields caches the resolved agentAddress for using in forwarding
    private String m_nodeLabel;
    
    public NNMEvent(String enterpriseObjectId, int genericType, int specificType) {
        m_eventIdentity = new EventIdentity(enterpriseObjectId, genericType, specificType);
    }
    
    public NNMEvent(EventIdentity identity) {
        m_eventIdentity = identity;
    }

    public void addVarBind(String objectId, String type, String varbind) {
        addVarBind(new DefaultNNMVarBind(objectId, type, varbind));
    }

    protected void addVarBind(NNMVarBind varBind) {
        if (m_varBinds == null) {
            m_varBinds = new LinkedList();
        }
        
        m_varBinds.add(varBind);
        
    }
    
    public void setEventIdentity(EventIdentity id) {
        m_eventIdentity = id;
    }
    
    public EventIdentity getEventIdentity() {
        return m_eventIdentity;
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

    public String getAgentAddress() {
        return m_agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        m_agentAddress = agentAddress;
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
    
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    public List getVarBinds() {
        return m_varBinds;
    }

    public void setVarBinds(List varBinds) {
        m_varBinds = varBinds;
    }

    public SnmpObjId getEventObjectId() {
        return m_eventIdentity.getEventObjectId();
    }
    
    public String toString() {
        return "NNMEvent[name="+getName()+", address="+getAgentAddress()+", category="+getCategory()+", severity="+getSeverity()+", oid="+getEventObjectId()+"]";
    }

    public static NNMEvent createEvent(String category, String severity,
            String name, String address) {
        NNMEvent event = new NNMEvent(null);
        event.setCategory(category);
        event.setName(name);
        event.setAgentAddress(address);
        event.setSeverity(severity);
        event.setTimeStamp(new Date());
        return event;
    }

    public String resolveNodeLabel(Resolver r) {
        if (m_nodeLabel == null) {
            m_nodeLabel = r.resolveAddress(getAgentAddress());
        }
        return m_nodeLabel;
    }
    
    public String getVarBindValue(String oid) {
        for(Iterator it = m_varBinds.iterator(); it.hasNext(); ) {
            NNMVarBind varBind = (NNMVarBind)it.next();
            if (oid.equals(varBind.getObjectId())) {
                return varBind.getValue();
            }
        }
        return null;
    }

}
