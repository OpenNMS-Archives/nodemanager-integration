package org.opennms.opennmsd;

import java.util.Arrays;

import org.opennms.nnm.SnmpObjId;

public class EventFormat implements Comparable {
    
    public static class WildCardSnmpObjId implements Comparable {
        
        private SnmpObjId m_base;
        private boolean m_wildCard;

        public WildCardSnmpObjId(String oid) {
            if (oid.endsWith("*")) {
                m_wildCard = true;
                m_base = SnmpObjId.get(oid.substring(0, oid.lastIndexOf('.')));
            } else {
                m_wildCard = false;
                m_base = SnmpObjId.get(oid);
            }
        }
        
        public boolean matches(SnmpObjId oid) {
            if (isWildCard()) {
                return m_base.isPrefixOf(oid);
            } else {
                return m_base.equals(oid);
            }
        }
        
        private boolean isWildCard() {
            return m_wildCard;
        }

        /**
         * These are ordered so as to define a 'best match' for event object ids.  
         * This ordering is as follows:
         * 1.  Exact (non wild card) oids preceed wild card ones are are ordered lexicographically
         * 2.  Wildcard urls are ordered so that more specific ones preceed less specific ones
         *       otherwise they are ordered lexicographically
         */
        public int compareTo(Object arg0) {
            WildCardSnmpObjId other = (WildCardSnmpObjId)arg0;
            if (isWildCard() && other.isWildCard()) {
                // if both are wildcards the the most specific should preceed the least specific
                if (m_base.isPrefixOf(other.m_base)) {
                    return 1;
                } else if (other.m_base.isPrefixOf(m_base)) {
                    return -1;
                } else {
                    return m_base.compareTo(other.m_base);
                }
            }
            else if (isWildCard() && !other.isWildCard()) {
                return 1;
            } else if (!isWildCard() && other.isWildCard()) {
                return -1;
            } else {
                return m_base.compareTo(other.m_base);
            }
        }
        
        public String toString() {
            return (m_wildCard ? m_base+".*" : m_base.toString());
        }
        
    }

    // these are used for matching
    private WildCardSnmpObjId m_eventObjectId;
    private String[] m_hosts;
    
    // this is used to order and represents the order in the file
    private int m_index;

    // these are filled in from the formatting information
    private String m_name;
    private String m_category;
    private String m_severity;
    
    public EventFormat(int index, String eventObjectId) {
        m_index = index;
        m_eventObjectId = new WildCardSnmpObjId(eventObjectId);
    }
    
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    
    public String getCategory() {
        return m_category;
    }
    public void setCategory(String category) {
        m_category = category;
    }
    
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    
    public String getEventObjectId() {
        return m_eventObjectId.toString();
    }
    public void setEventObjectId(String objectId) {
        m_eventObjectId = new WildCardSnmpObjId(objectId);
    }
    
    public String toString() {
         return "EVENT "+m_name+" "+m_eventObjectId+" \""+m_category+"\" "+m_severity + (m_hosts == null ? "" : " NODES "+Arrays.toString(m_hosts));
    }

    public String[] getHosts() {
        return m_hosts;
    }
    public void setHosts(String[] hosts) {
        Arrays.sort(hosts, String.CASE_INSENSITIVE_ORDER);
        m_hosts = hosts;
    }
    
    public boolean matches(NNMEvent e, Resolver r) {
        if (m_eventObjectId.matches(e.getEventObjectId())) {
            if (m_hosts != null) {
                // make sure the host is in the list
                String nodeLabel = e.resolveNodeLabel(r);
                return Arrays.binarySearch(m_hosts, nodeLabel, String.CASE_INSENSITIVE_ORDER) >= 0;
            }
            // no host so it matches by default
            return true;
        }
        // it doesn't match the oid so it doesn't match the event
        return false;
    }
    
    
    public void apply(NNMEvent e) {
        e.setName(m_name);
        e.setCategory(m_category);
        e.setSeverity(m_severity);
    }

    /**
     * This ordering is used to define the 'best match' format.  EventFormat are ordered in such
     * a way so that the first one that matches an event if checked in order would be the one that
     * should be selected for forwarding.  This order works as follows:
     * 1.  Events that have NODES defined preceed those that don't
     * 2.  With in those grouped together as above, the are ordered according to WildCardSnmpObjid order
     *     which is:  
     *     a. non wild card preceed wild card,
     *     b. more specific wild cards preceed less specific (ie 1.2.3.* comes AFTER 1.2.3.4.*)
     *     c. then they are ordered by object id lexicographical ordering
     * 3.  Lastly those that would be the same by the above are ordered accoring the their index
     *     which is assumed to be order by there file order so early ones preceed later ones.
     *
     */
    public int compareTo(Object arg) {
        EventFormat other = (EventFormat)arg;
        
        // formats with included hosts come before non specific formats
        if (m_hosts == null && other.m_hosts != null) {
            return 1;
        } else if (other.m_hosts == null && m_hosts != null) {
            return -1;
        } else {
            // if they are the same wrt host lists then use oid to order
            // with all wild cards following specific entries
            int oidCmp = m_eventObjectId.compareTo(other.m_eventObjectId);
            if (oidCmp != 0){
                return oidCmp;
            } else {
                // if the both are the same wrt to hosts and have the same oid then use
                // the order in the file (the index)
                return m_index - other.m_index;
            }
        }
        
        
    }
    
    
    
}
