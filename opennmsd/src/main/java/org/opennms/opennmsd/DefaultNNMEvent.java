package org.opennms.opennmsd;

import java.util.Date;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpVarBind;
import org.opennms.ovapi.OVsnmpPduUtils;

public class DefaultNNMEvent extends AbstractNNMEvent {
    
    private static Logger log = Logger.getLogger(DefaultNNMEvent.class);

    public DefaultNNMEvent(OVsnmpPdu trap) {
        log.debug(OVsnmpPduUtils.toString(trap));
        
        setSourceAddress(trap.getAgentAddress());
        setTimeStamp(new Date(m_startTime+trap.getTime()*10));
        setCommunity(trap.getCommunity());
        setEnterpriseId(trap.getEnterpriseObjectId());
        setGeneric(trap.getGenericType());
        setSpecific(trap.getSpecificType());
        setSnmpHost(trap.getIpAddress());
        setVersion(1);
        

        OVsnmpVarBind varBind = trap.getVarBinds();
        
        while(varBind != null) {
            addVarBind(new DefaultNNMVarBind(varBind));
            varBind = varBind.getNextVarBind();
        }
        
    }

}
