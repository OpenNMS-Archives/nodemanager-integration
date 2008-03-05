/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.opennmsd;

import java.util.Arrays;

import junit.framework.TestCase;

import org.opennms.nnm.SnmpObjId;
import org.opennms.opennmsd.EventFormat.WildCardSnmpObjId;

/**
 * EventFormatTest
 *
 * @author brozow
 */
public class EventFormatTest extends TestCase {
    
    public void testWildCardSnmpObjIdOrder() {
        WildCardSnmpObjId a = new WildCardSnmpObjId(".1.2.3.4.5");
        WildCardSnmpObjId b = new WildCardSnmpObjId(".1.2.3.5.*");
        WildCardSnmpObjId c = new WildCardSnmpObjId(".1.2.3.4.*");
        WildCardSnmpObjId d = new WildCardSnmpObjId(".1.2.3.4.5.0");
        WildCardSnmpObjId e = new WildCardSnmpObjId(".1.2.3.4.6");
        WildCardSnmpObjId f = new WildCardSnmpObjId(".1.2.3.4.1");
        WildCardSnmpObjId g = new WildCardSnmpObjId(".1.2.3.*");
        
        
        WildCardSnmpObjId[] actualArray = { a, b, c, d, e, f, g};
        Arrays.sort(actualArray);
        
        WildCardSnmpObjId[] expectedArray = { f, a, d, e, c, b, g};
        
        for(int i = 0; i < actualArray.length; i++) {
            assertSame(expectedArray[i], actualArray[i]);
            //System.err.println(i+": "+actualArray[i]);
        }
            
    }
    
    public void testWildCardSnmpObjIdMatch() {
        WildCardSnmpObjId a = new WildCardSnmpObjId(".1.2.3.4.5");
        WildCardSnmpObjId b = new WildCardSnmpObjId(".1.2.3.5.*");
        WildCardSnmpObjId c = new WildCardSnmpObjId(".1.2.3.4.*");
        WildCardSnmpObjId d = new WildCardSnmpObjId(".1.2.3.4.5.0");
        WildCardSnmpObjId e = new WildCardSnmpObjId(".1.2.3.4.6");
        WildCardSnmpObjId f = new WildCardSnmpObjId(".1.2.3.4.1");
        WildCardSnmpObjId g = new WildCardSnmpObjId(".1.2.3.*");
        WildCardSnmpObjId h = new WildCardSnmpObjId(".1.2.3.4.5.*");
        
        SnmpObjId oid = SnmpObjId.get(".1.2.3.4.5");
        
        assertTrue(a.matches(oid));
        assertFalse(b.matches(oid));
        assertTrue(c.matches(oid));
        assertFalse(d.matches(oid));
        assertFalse(e.matches(oid));
        assertFalse(f.matches(oid));
        assertTrue(g.matches(oid));
        assertTrue(h.matches(oid));

        
    }
    
    public void testEventFormatOrder() {
        EventFormat a = new EventFormat(0, ".1.2.3.4.5"); // 4
        EventFormat b = new EventFormat(1, ".1.2.3.5.*"); // 9
        EventFormat c = new EventFormat(2, ".1.2.3.4.*"); // 8
        EventFormat d = new EventFormat(3, ".1.2.3.4.5.0"); // 5
        EventFormat e = new EventFormat(4, ".1.2.3.4.5.0"); // 2
        e.setHosts(new String[]{ "host1" });
        EventFormat f = new EventFormat(5, ".1.2.3.4.6"); // 6
        EventFormat g = new EventFormat(6, ".1.2.3.4.1"); // 3
        EventFormat h = new EventFormat(7, ".1.2.3.4.1"); // 1
        h.setHosts(new String[] { "host1" });
        EventFormat i = new EventFormat(8, ".1.2.3.*");   // 10
        EventFormat j = new EventFormat(9, ".1.2.3.4.6"); // 7
        
        EventFormat[] actualArray = {a, b, c, d, e, f, g, h, i, j};
        Arrays.sort(actualArray);
        
        EventFormat[] expectedArray = { h, e, g, a, d,f, j, c, b, i };

        System.err.println(EventFormat.toString(actualArray));
        for(int index = 0; index < actualArray.length; index++) {
            assertSame(expectedArray[index], actualArray[index]);
        }
    }
    
    public void testEventFormatMatch() {
        EventFormat a = new EventFormat(0, ".1.2.3.4.5"); // 4
        EventFormat b = new EventFormat(1, ".1.2.3.5.*"); // 9
        EventFormat c = new EventFormat(2, ".1.2.3.4.*"); // 8
        EventFormat d = new EventFormat(3, ".1.2.3.4.5.0"); // 5
        EventFormat e = new EventFormat(4, ".1.2.3.4.5"); // 2
        e.setHosts(new String[]{ "host1" });
        EventFormat f = new EventFormat(4, ".1.2.3.4.5"); // 2
        f.setHosts(new String[]{ "host2", "host3" });
        EventFormat g = new EventFormat(5, ".1.2.3.4.6"); // 6
        EventFormat h = new EventFormat(6, ".1.2.3.4.1"); // 3
        EventFormat i = new EventFormat(7, ".1.2.3.4.1"); // 1
        EventFormat j = new EventFormat(8, ".1.2.3.*");   // 10
        EventFormat k = new EventFormat(8, ".1.2.3.*");   // 10
        k.setHosts(new String[] { "host1" });
        EventFormat l = new EventFormat(8, ".1.2.3.*");   // 10
        l.setHosts(new String[] { "host2" });
        EventFormat m = new EventFormat(9, ".1.2.3.4.6"); // 7
        
        NNMEvent event = new NNMEvent(".1.2.3.4", 4, 0);
        event.setAgentAddress("192.168.1.1");
        
        Resolver r = new Resolver() {
            public String resolveAddress(String address) {
                return "host2";
            }
        };

        
        assertTrue(a.matches(event, r));
        assertFalse(b.matches(event, r));
        assertTrue(c.matches(event, r));
        assertFalse(d.matches(event, r));
        assertFalse(e.matches(event, r));
        assertTrue(f.matches(event, r));
        assertFalse(g.matches(event, r));
        assertFalse(h.matches(event, r));
        assertFalse(i.matches(event, r));
        assertTrue(j.matches(event, r));
        assertFalse(k.matches(event, r));
        assertTrue(l.matches(event, r));
        assertFalse(m.matches(event, r));

    }

}
