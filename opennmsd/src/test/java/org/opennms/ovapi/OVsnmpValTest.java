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
package org.opennms.ovapi;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.opennms.nnm.swig.OVsnmpVal;

public class OVsnmpValTest extends TestCase {
    
    public void testCounter64() {

        OVsnmpVal val = new OVsnmpVal();
        
        val.setCounter64Value(BigInteger.valueOf(27));
        
        BigInteger twentySeven = val.getCounter64Value();
        assertEquals(27, twentySeven.intValue());

        long v = (long)(Integer.MAX_VALUE)+1L;

        val.setCounter64Value(BigInteger.valueOf(v));
        
        BigInteger actual = val.getCounter64Value();
        assertEquals("2147483648", actual.toString());
        assertEquals(v, actual.longValue());
        
        byte[] bytes = { (byte)0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };  
        
        BigInteger bv = new BigInteger(bytes);
        
        val.setCounter64Value(bv);
        
        BigInteger big = val.getCounter64Value();
        assertEquals(bv, big);
        
        
    }
    
    public void testInteger() {
        OVsnmpVal val = new OVsnmpVal();
        
        val.setIntValue(3);
        
        assertEquals(3, val.getIntValue());
    }
    
    public void testUnsigned32() {
        OVsnmpVal val = new OVsnmpVal();
        
        long v = ((long)Integer.MAX_VALUE)+100L;
        
        val.setUnsigned32Value(v);
        
        assertEquals(v, val.getUnsigned32Value());
    }
    
    public void testObjectId() {
        OVsnmpVal val = new OVsnmpVal();
        
        assertEquals(6, val.setObjectId(".1.2.3.4.5.6"));
        
        assertEquals(".1.2.3.4.5.6", val.getObjectId(6));
        
    }
    
    public void testOctetString() {
        
        String expected = "a display string";
        
        OVsnmpVal val = new OVsnmpVal();
        
        val.setOctetString(expected.getBytes());
        
        byte[] bytes = new byte[expected.getBytes().length];
        
        val.getOctetString(bytes);
        
        String actual = new String(bytes);
        
        assertEquals(expected, actual);
        
    }
    

}
