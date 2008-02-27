package org.opennms.opennmsd;


public class DefaultNNMVarBind implements NNMVarBind {
    
    private String m_encoding;
    private String m_type;
    private String m_objectId;
    private String m_value;

    public DefaultNNMVarBind(String oid, String type, String value) {
        setEncoding("text");
        setObjectId(oid);
        setType(type);
        setValue(value);
    }

    public String getEncoding() {
        return m_encoding;
    }

    public void setEncoding(String encoding) {
        m_encoding = encoding;
    }

    public String getObjectId() {
        return m_objectId;
    }

    public void setObjectId(String objectId) {
        m_objectId = objectId;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

}
