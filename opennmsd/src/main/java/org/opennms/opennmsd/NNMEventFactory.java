package org.opennms.opennmsd;

import org.opennms.nnm.swig.OVsnmpPdu;

public interface NNMEventFactory {
    
    NNMEvent createEvent(OVsnmpPdu event);

}
