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

import java.util.List;

import junit.framework.TestCase;

/**
 * EventQueueTest
 *
 * @author brozow
 */
public class EventQueueTest extends TestCase {
    
    public int eventNumber = 0;
    
    NNMEvent createEvent() {
        NNMEvent e = new NNMEvent(".1.2.3.4", 5, 0);
        e.setName(""+(++eventNumber));
        return e;
    }
    
    public void testRegularForwarding() throws InterruptedException {
        EventQueue queue = new EventQueue();
        queue.setMaxBatchSize(3);
        queue.init();
    
        queue.accept(createEvent());
        queue.persist(createEvent());
        queue.accept(createEvent());
        
        List events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        queue.forwardSuccessful(events);
        
        queue.persist(createEvent());
        queue.accept(createEvent());
        queue.persist(createEvent());
        queue.accept(createEvent());
        
        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        queue.forwardSuccessful(events);
        
        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(1, events.size());
        
        queue.forwardSuccessful(events);

        
    }

    public void testFailure() throws InterruptedException {
        EventQueue queue = new EventQueue();
        queue.setMaxBatchSize(3);
        queue.init();
    
        queue.accept(createEvent());  // 1
        queue.persist(createEvent()); // 2
        queue.accept(createEvent());  // 3
        
        List events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        queue.forwardSuccessful(events);
        
        queue.persist(createEvent()); // 4
        queue.accept(createEvent());  // 5
        queue.persist(createEvent()); // 6
        queue.accept(createEvent());  // 7
        
        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        queue.forwardFailed(events);
        
        queue.accept(createEvent()); // 8
        
        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(2, events.size());
        assertPersistentEvent(events, 0, "4");
        assertPersistentEvent(events, 1, "6");
        
        queue.forwardFailed(events);
        
        queue.persist(createEvent()); // 9
        queue.accept(createEvent()); // 10
        queue.persist(createEvent()); // 11
        queue.accept(createEvent()); // 12
        
        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(3, events.size());
        assertPersistentEvent(events, 0, "4");
        assertPersistentEvent(events, 1, "6");
        assertPersistentEvent(events, 2, "9");
        
        queue.forwardSuccessful(events);

        queue.persist(createEvent()); // 13
        queue.accept(createEvent()); // 14

        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(1, events.size());
        assertPersistentEvent(events, 0, "11");

        queue.forwardSuccessful(events);

        events = queue.getEventsToForward();
        assertNotNull(events);
        assertEquals(2, events.size());
        assertPersistentEvent(events, 0, "13");
        assertEquals("14", getEvent(events, 1).getName());

        queue.forwardSuccessful(events);
        
        
    }

    private void assertPersistentEvent(List events, int index, String name) {
        assertTrue(getEvent(events, index).isPersistent());
        assertEquals(name, getEvent(events, index).getName());
    }

    private NNMEvent getEvent(List events, int index) {
        return ((NNMEvent)events.get(index));
    }

}
