package org.opennms.opennmsd;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
        
        String enterpriseId = trap.getEnterpriseObjectId();
        int generic = trap.getGenericType();
        int specific = trap.getSpecificType();
        String agentAddress = trap.getAgentAddress();

        EventIdentity eId = new EventIdentity(enterpriseId, generic, specific);
        
        
        EventDescription descr = m_eventConfiguration.getDescription(eId, agentAddress);
        
        if (descr == null) {
            log.info("Could not find trap with id "+eId+" in trapd.conf");
            return null;
        }
        
        
        NNMEvent e = new NNMEvent();

        e.setTimeStamp(new Date(DefaultNNMEventFactory.s_startTime+trap.getTime()*10));
        e.setCommunity(trap.getCommunity());
        e.setEventIdentity(eId);
        e.setSnmpHost(trap.getIpAddress());
        e.setVersion(1);
        
        e.setName(descr.getName());
        e.setCategory(descr.getCategory());
        e.setSeverity(descr.getSeverity());

        OVsnmpVarBind varBind = trap.getVarBinds();
        
        while(varBind != null) {
            String objectId = varBind.getObjectId();
            String varbindValue = OVsnmpPduUtils.getVarbindValue(varBind);
            if (".1.3.6.1.4.1.11.2.17.2.2.0".equals(objectId)) {
                /* then this is possibly the hostname of ip address sent in an internal
                 * node manager event.  if it can resolve to ip address and host name the
                 * use them in 'nodelabel' parm in interface rather than agent address
                 */
                try {
                    InetAddress addr = InetAddress.getByName(varbindValue);
                    String iface = addr.getHostAddress();
                    String nodeLabel = addr.getHostName();
                    descr.setAddress(iface);
                    descr.setNodeLabel(nodeLabel);
                    
                } catch (UnknownHostException ex) {
                    // this is the normal case so do nothing
                    log.info("Unable to resolve "+varbindValue+" as hostname/ipAddress for event "+e.getName()+" using trap supplied values");
                }
            }
            e.addVarBind(objectId, 
                    OVsnmpPduUtils.getTypeString(varBind.getType()),
                    varbindValue);
     
            varBind = varBind.getNextVarBind();
        }
        
        e.setSourceAddress(descr.getAddress());
        
        e.addVarBind("nodelabel", "OctetString", descr.getNodeLabel());
        
        return e;
    }

}
