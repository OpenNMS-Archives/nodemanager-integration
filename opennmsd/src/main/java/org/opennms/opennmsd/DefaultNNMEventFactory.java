package org.opennms.opennmsd;

import java.util.Date;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpVarBind;
import org.opennms.ovapi.OVsnmpPduUtils;

public class DefaultNNMEventFactory implements NNMEventFactory {
    
    private static Logger log = Logger.getLogger(DefaultNNMEventFactory.class);

    
    private EventConfiguration m_eventConfiguration;


    protected static long s_startTime = System.currentTimeMillis();
    public void setEventConfiguation(EventConfiguration eventConfiguration) {
        m_eventConfiguration = eventConfiguration;
    }

    public NNMEvent createEvent(OVsnmpPdu trap) {
        log.debug(OVsnmpPduUtils.toString(trap));
        
        NNMEvent e = new NNMEvent();
        e.setSourceAddress(trap.getAgentAddress());
        e.setTimeStamp(new Date(DefaultNNMEventFactory.s_startTime+trap.getTime()*10));
        e.setCommunity(trap.getCommunity());
        e.setEnterpriseId(trap.getEnterpriseObjectId());
        e.setGeneric(trap.getGenericType());
        e.setSpecific(trap.getSpecificType());
        e.setSnmpHost(trap.getIpAddress());
        e.setVersion(1);
        
        
        OVsnmpVarBind varBind = trap.getVarBinds();
        
        while(varBind != null) {
            e.addVarBind(varBind.getObjectId(), 
                    OVsnmpPduUtils.getTypeString(varBind.getType()),
                    OVsnmpPduUtils.getVarbindValue(varBind));
     
            varBind = varBind.getNextVarBind();
        }
        
        String key = e.getEventConfigurationKey();
        e.setName(m_eventConfiguration.getName(key));
        e.setCategory(m_eventConfiguration.getCategory(key));
        e.setSeverity(m_eventConfiguration.getSeverity(key));
        return e;
    }

}
