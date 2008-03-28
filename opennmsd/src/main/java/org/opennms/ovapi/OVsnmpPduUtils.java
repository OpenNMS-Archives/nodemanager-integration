package org.opennms.ovapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.tools.ant.util.Base64Converter;
import org.opennms.nnm.swig.NNM;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpVarBind;
import org.opennms.opennmsd.DefaultNNMVarBind;


public abstract class OVsnmpPduUtils {
    
    private static class StringCreator {
        
        private StringBuffer buf = new StringBuffer();
        private boolean m_first = true;

        public StringCreator(Object o) {
            buf.append(o.getClass().getName());
        }

        public void append(String name, Object val) {
            appendFieldPrefix(name);
            buf.append(val);
        }
        
        public void append(String name, int val) {
            appendFieldPrefix(name);
            buf.append(val);
        }

        private void appendFieldPrefix(String name) {
            if (!m_first) {
                buf.append(", ");
            } else {
                m_first = false;
            }
            buf.append(name);
            buf.append(" = ");
        }
        
        public String toString() {
            return buf.toString();
        }
        
    }
    
    public static String toString(OVsnmpPdu pdu) {
        StringCreator buf = new StringCreator(pdu);
        buf.append("address", pdu.getIpAddress());
        buf.append("command", pdu.getCommand());
        buf.append("agentAddr", pdu.getAgentAddress());
        buf.append("enterprise", pdu.getEnterpriseObjectId());
        buf.append("generic", pdu.getGenericType());
        buf.append("specific", pdu.getSpecificType());
        buf.append("time", pdu.getTime()+" cs");
        
        int count = 1;
        OVsnmpVarBind varbind = pdu.getVarBinds();
        while(varbind != null) {
            buf.append(count+": oid", varbind.getObjectId());
            buf.append(count+": type", varbind.getType());
            buf.append(count+": val", getVarbindValue(varbind));
            varbind = varbind.getNextVarBind();
            count++;
        }
        
        return buf.toString();
    }
    
    public static String getVarbindValue(OVsnmpVarBind varBind) {
        int type = varBind.getType();
        if (type == NNM.ASN_BOOLEAN) {
            return (varBind.getValue().getIntValue() == 0 ? "false" : "true");
        } else if (type == NNM.ASN_INTEGER) {
            return Integer.toString(varBind.getValue().getIntValue());
        } else if (type == NNM.ASN_OCTET_STR) {
            byte[] bytes = new byte[varBind.getValLength()];
            varBind.getValue().getOctetString(bytes);
            return new String(bytes);
        } else if (type == NNM.ASN_U_INTEGER) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_OBJECT_ID) {
            return varBind.getValue().getObjectId(varBind.getValLength());
        } else if (type == NNM.ASN_TIMETICKS) {
            int centis = varBind.getValue().getIntValue();
            return ""+centis/100+"."+centis%100+" s";
        } else if (type == NNM.ASN_COUNTER32) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_COUNTER64) {
            return ""+varBind.getValue().getCounter64Value();
        } else if (type == NNM.ASN_GAUGE) {
            return ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_IPADDRESS) {
            byte[] bytes = new byte[4];
            varBind.getValue().getOctetString(bytes);
            try {
                return InetAddress.getByAddress(bytes).getHostAddress();
            } catch (UnknownHostException e) {
                return "UnknownHost that can't happen";
            }
        } else {
            return "UNKNOWN TYPE: "+type;
        }
    }

    // one of (int|string|Int32|OctetString|Null|ObjectIdentifier|Sequence|IpAddress|Counter32|Gauge32|TimeTicks|Opaque|Counter64)
    public static String getTypeString(int type) {
        if (type == NNM.ASN_BOOLEAN) {
            return "int";
        } else if (type == NNM.ASN_INTEGER) {
            return "int";
        } else if (type == NNM.ASN_OCTET_STR) {
            return "OctetString";
        } else if (type == NNM.ASN_U_INTEGER) {
            return "int";
        } else if (type == NNM.ASN_OBJECT_ID) {
            return "ObjectIdentifier";
        } else if (type == NNM.ASN_TIMETICKS) {
            return "TimeTicks";
        } else if (type == NNM.ASN_COUNTER32) {
            return "Counter32";
        } else if (type == NNM.ASN_COUNTER64) {
            return "Counter64";
        } else if (type == NNM.ASN_GAUGE) {
            return "Gauge32";
        } else if (type == NNM.ASN_IPADDRESS) {
            return "IpAddress";
        } else {
            return "Opaque";
        }
    }
    
    public static boolean allBytesDisplayable(byte[] bytes) {
        for(int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if ((b < 32 && b != 9 && b != 10 && b != 13 && b != 0) || b == 127)
                return false;
        }
        return true;
    }

    public static DefaultNNMVarBind constructNNMVarBind(OVsnmpVarBind varBind) {
        
        String encoding = "text";
        String val = null;
        int type = varBind.getType();
        if (type == NNM.ASN_BOOLEAN) {
            val = (varBind.getValue().getIntValue() == 0 ? "false" : "true");
        } else if (type == NNM.ASN_INTEGER) {
            val = Integer.toString(varBind.getValue().getIntValue());
        } else if (type == NNM.ASN_OCTET_STR) {
            byte[] bytes = new byte[varBind.getValLength()];
            varBind.getValue().getOctetString(bytes);
            if (allBytesDisplayable(bytes)) {
                val = new String(bytes);
            } else {
                encoding = "base64";
                Base64Converter b64 = new Base64Converter();
                val = b64.encode(bytes);
            }
        } else if (type == NNM.ASN_U_INTEGER) {
            val = ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_OBJECT_ID) {
            val =  varBind.getValue().getObjectId(varBind.getValLength());
        } else if (type == NNM.ASN_TIMETICKS) {
            int centis = varBind.getValue().getIntValue();
            val = ""+centis/100+"."+centis%100+" s";
        } else if (type == NNM.ASN_COUNTER32) {
            val = ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_COUNTER64) {
            val = ""+varBind.getValue().getCounter64Value();
        } else if (type == NNM.ASN_GAUGE) {
            val = ""+varBind.getValue().getUnsigned32Value();
        } else if (type == NNM.ASN_IPADDRESS) {
            byte[] bytes = new byte[4];
            varBind.getValue().getOctetString(bytes);
            try {
                val = InetAddress.getByAddress(bytes).getHostAddress();
            } catch (UnknownHostException e) {
                val = "UnknownHost that can't happen";
            }
        } else {
            val = "UNKNOWN TYPE: "+type;
        }

        String oid = varBind.getObjectId();
        String typeString = getTypeString(varBind.getType());
        return new DefaultNNMVarBind(encoding, oid, typeString, val);
    }


}
