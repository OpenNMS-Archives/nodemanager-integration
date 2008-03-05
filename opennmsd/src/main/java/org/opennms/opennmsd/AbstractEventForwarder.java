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
package org.opennms.opennmsd;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;

/**
 * DefaultEventForwarder
 *
 * @author brozow
 */
public abstract class AbstractEventForwarder implements EventForwarder, Runnable {
    
    private static Logger log = Logger.getLogger(AbstractEventForwarder.class);
    
    private BlockingQueue m_queue = new LinkedBlockingQueue();
    private Thread m_thread;
    private boolean m_stopped = false;
    
    public void start() {
        m_stopped = false;
        m_thread = new Thread(this, "EventForwarderThread");
        m_thread.start();
        log.debug("Starting event forwarding");
    }
    
    public void stop() {
        log.debug("Stopping event forwarding");
        m_stopped = true;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#accept(org.opennms.opennmsd.NNMEvent)
     */
    public void accept(NNMEvent event) {
        log.debug("accepting event "+event);
        m_queue.offer(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#discard(org.opennms.opennmsd.NNMEvent)
     */
    public void discard(NNMEvent event) {
        log.debug("discarding event "+event);
        // do nothing when we discard an event
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#preserve(org.opennms.opennmsd.NNMEvent)
     */
    public void preserve(NNMEvent event) {
        log.debug("preserving event "+event);
        m_queue.offer(event);
    }

    public void run() {
        
        try {

            while(!m_stopped) {
                NNMEvent event = (NNMEvent)m_queue.take();
                
                log.debug("Event available to forward");
                
                List events = new LinkedList();
                events.add(event);
                
                m_queue.drainTo(events);
                
                log.debug("forwarding "+events.size()+" events."+events);
            
                forwardEvents(events);
            
            }
        
        } catch (InterruptedException e) {
            // thread interrupted so complete it
        }
        
    }

    protected abstract void forwardEvents(List events);

}
