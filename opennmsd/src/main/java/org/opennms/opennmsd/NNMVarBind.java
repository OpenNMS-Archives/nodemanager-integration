package org.opennms.opennmsd;

public interface NNMVarBind {
    
    public String getObjectId();
    
    // one of: (int|string|Int32|OctetString|Null|ObjectIdentifier|Sequence|IpAddress|Counter32|Gauge32|TimeTicks|Opaque|Counter64)
    public String getType();
    
    // one of: (text|base64)
    public String getEncoding();
    
    public String getValue();

}
