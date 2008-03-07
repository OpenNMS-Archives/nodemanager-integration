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

import java.util.List;

import org.apache.log4j.Logger;

/**
 * AbstractEventForwarder
 * 
 * The purpose of this class is manage the queue of events that need to be forward to opennms.
 * 
 * It passes them on to the forwardEvents method implemented by base classes in batches as they are 
 * added to the queue.  The forwardEvents method does the actual work of sending them to opennms.
 * 
 * persist, accept and discard are called to add the events to the queue as appropriate.  
 * 
 *
 * @author brozow
 */
public abstract class AbstractEventForwarder implements EventForwarder, Runnable {
    
    private static Logger log = Logger.getLogger(AbstractEventForwarder.class);
    
    private Thread m_thread;
    private boolean m_stopped = false;

    private EventQueue m_queue = new EventQueue();
    private long m_retryInterval = 1000;
    
    public void setRetryInterval(int retryInterval) {
        m_retryInterval = retryInterval;
    }
    
    public void setMaxBatchSize(int maxBatchSize) {
        m_queue.setMaxBatchSize(maxBatchSize);
    }
    
    public void setMaxPersistentEvents(int maxPersistentEvents) {
        m_queue.setMaxPersistentEvents(maxPersistentEvents);
    }
    
    public void start() {
        m_stopped = false;
        m_queue.init();
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
        m_queue.accept(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#discard(org.opennms.opennmsd.NNMEvent)
     */
    public void discard(NNMEvent event) {
        m_queue.discard(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#preserve(org.opennms.opennmsd.NNMEvent)
     */
    public void preserve(NNMEvent event) {
        m_queue.persist(event);
    }

    public void run() {
        
        try {

            while(!m_stopped) {

                List eventsToForward = m_queue.getEventsToForward();
                
                try {
                    forwardEvents(eventsToForward);
                    m_queue.forwardSuccessful(eventsToForward);
                } catch (Exception e) {
                    log.error("Unable to forward events", e);
                    m_queue.forwardFailed(eventsToForward);
                    if (!m_stopped) {
                        // a failure occurred so sleep a moment and try again
                        Thread.sleep(m_retryInterval);
                    }
                }
            
            }
        
        } catch (InterruptedException e) {
            // thread interrupted so complete it
        }
        
    }

    protected abstract void forwardEvents(List events) throws Exception;

}
