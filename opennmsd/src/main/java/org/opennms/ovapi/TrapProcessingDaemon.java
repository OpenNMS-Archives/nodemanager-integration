package org.opennms.ovapi;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.nnm.swig.SnmpCallback;
import org.opennms.nnm.swig.fd_set;
import org.opennms.nnm.swig.timeval;

public abstract class TrapProcessingDaemon extends OVsDaemon {

    
    private OVsnmpSession m_trapSession;
    private SnmpCallback m_callback;
    
    protected String onInit() {
        
        m_callback = new SnmpCallback() {

            public void callback(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
                Logger.getLogger(getClass()).debug("callback called");
                try {
                    onEvent(reason, session, pdu);
                } finally {
                    Logger.getLogger(getClass()).debug("callback returning");
                }
            }
        };
        
        m_trapSession = OVsnmpSession.eventOpen("opennmsd", m_callback, ".*");

        return "TrapProcessingDaemon has initialized successfully.";
    }

    protected abstract void onEvent(int reason, OVsnmpSession session, OVsnmpPdu pdu);

    protected String onStop() {
        
        
        m_trapSession.close();

        return "TrapProcessingDaemon has exited successfully.";
    }

    protected int getRetryInfo(fd_set fdset, timeval tm) {
        int maxSnmpFDs = OVsnmpSession.getRetryInfo(fdset, tm);
        int maxSuperFDs = super.getRetryInfo(fdset, tm);
        return Math.max(maxSnmpFDs, maxSuperFDs);
    }

    protected void processReads(fd_set fdset) {
        OVsnmpSession.read(fdset);
        super.processReads(fdset);
    }

    protected void processTimeouts() {
        OVsnmpSession.doRetry();
        super.processTimeouts();
    }

    
}
