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
� * GNU General Public License for more details.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.NNM;
import org.opennms.nnm.swig.OVsPMDCommand;
import org.opennms.nnm.swig.fd_set;
import org.opennms.nnm.swig.timeval;

public abstract class OVsDaemon {
    
    Logger log = Logger.getLogger(getClass());
    
    public abstract static class ScheduledTask implements Runnable {
        
        long m_nextRuntimeInMillis = -1;
        long m_period = 0;

        public long getNextRuntimeMillis() {
            return m_nextRuntimeInMillis;
        }
        
        public void setNextRuntimeMillis(long nextRunTimeMillis) {
            m_nextRuntimeInMillis = nextRunTimeMillis;
        }
        
        public long getPeriod() {
            return m_period;
        }
        
        public void setPeriod(long period) {
            m_period = period;
        }
        
        public abstract void run();

        public void setTimeout(long now, timeval tm) {
            long remaining = (m_nextRuntimeInMillis - now);
            if (remaining < tm.getTimeInMillis()) {
                tm.setTimeInMillis(remaining);
            }
        }

        public boolean isReady(long now) {
            return now > m_nextRuntimeInMillis;
        }

        boolean setNextPeriod() {
            if (!isPeriodic()) {
                return false;
            } else {
                setNextRuntimeMillis(getNextRuntimeMillis()+getPeriod());
                return true;
            }
        }

        private boolean isPeriodic() {
            return getPeriod() > 0;
        }
        
    }
    
    static class DefaultOVsDaemon extends OVsDaemon {
        
        
        protected String onInit() {
            
            ScheduledTask statusReporter = new ScheduledTask() {
                
                long m_lastRun = System.currentTimeMillis();

                public void run() {
                    long now = System.currentTimeMillis();
                    setStatus("Timer run at "+now+" after " + (now - m_lastRun) + " millis.  Period is "+getPeriod());
                    m_lastRun = now;
                }
                
            };
            
            schedule(statusReporter, 10000, 20000);
            
            return "DefaultOVsDaemon has finished initializing.";
        }

        protected String onStop() {
            return "DefaultOVsDaemon has exited successfully.";
        }
        
    }
    
    private int m_ovspmdFd;
    private boolean m_finished = false;
    
    private List m_scheduledTasks = new LinkedList();
    private long m_selectedMillis;

    protected abstract String onInit();

    protected abstract String onStop();

    public int getPmdFd() {
        return m_ovspmdFd;
    }

    public void execute() {

        int[] sp = new int[1];

        if (NNM.OVsInit(sp) < 0) {
            log("error calling OVsInit");
        }
        
        m_ovspmdFd = sp[0];

        String initResponse = "";
        int success = NNM.OVS_RSP_FAILURE;
        try {
        
            initResponse = onInit();
            success = NNM.OVS_RSP_SUCCESS;
            
        } catch (Throwable t) {
            initResponse = "Exception occurred initializing "+this+": "+t;
            log(initResponse, t);
        }

        if (initComplete(initResponse, success) < 0) {
            log("error calling OVsInitComplete");
        }
        
        String callmsg;
        try {
            callmsg = (String)call();
        } catch (Throwable t) {
            callmsg = "Exception occurred calling "+this+": "+t;
            log(callmsg, t);
        }
        
        if (NNM.OVsDone(callmsg) < 0) {
            log("error occurred calling OVsDone");
        }
    }

    private int initComplete(String initResponse, int success) {
        log("OVsInitComlete(\""+initResponse+"\", "+success+")");
        return NNM.OVsInitComplete(success, initResponse);
    }
    
    public void setStatus(String message) {
        log("OVsResponse(OVS_RSP_LAST_MSG, \""+message+"\")");
        if (NNM.OVsResponse(NNM.OVS_RSP_LAST_MSG, message) < 0) {
            log("error calling OVsResponse");
        }
    }

    public int readPmdCmd() {
        OVsPMDCommand command = new OVsPMDCommand();
        
        if (NNM.OVsReceive(command) < 0) {
            log("error calling OVsReceive");
        }
        
        return command.getCode();
    }

    boolean isFinished() {
        return m_finished;
    }

    void setFinished(boolean finished) {
        m_finished = finished;
    }

    public Object call() throws Exception {
        long start = System.currentTimeMillis();
        long end = start;
        
    
        fd_set fdset = new fd_set();
        timeval tm = new timeval();

    
        while (!isFinished()) {
            fdset.zero();
            tm.setTimeInMillis(Integer.MAX_VALUE);
            int maxFDs = getRetryInfo(fdset, tm);
            m_selectedMillis = tm.getTimeInMillis();
            long selectStart = System.currentTimeMillis();
            int fds = NNM.select(maxFDs, fdset, null, null, tm);
            long selectEnd = System.currentTimeMillis();
            log("Returned from select after "+(selectEnd - selectStart)+" ms.");
            if (fds > 0) {
                processReads(fdset);
            }
            
            processTimeouts();
        }     
        
        end = System.currentTimeMillis();
        
        return onStop();
    }

    protected void processTimeouts() {

        // we clone the list so we don't have concurrent modifications if tasks add new tasks
        List tasks = new LinkedList(m_scheduledTasks);

        long now = System.currentTimeMillis();
        for(Iterator it = tasks.iterator(); it.hasNext(); ) {
            ScheduledTask task = (ScheduledTask)it.next();
            if (task.isReady(now)) {
                task.run();
                
                if (task.setNextPeriod()) {
                    // reschedule it for next time
                    m_scheduledTasks.add(task);
                }
            }
        }
    }

    protected void processReads(fd_set fdset) {
        if (fdset.isSet(getPmdFd())) {
            int code = readPmdCmd();
            setStatus("Received cmd code "+code+" from pmd");
    
            if (code == NNM.OVS_CMD_EXIT) {
                setFinished(true);
            }
        }
    
    }
    
    public void schedule(ScheduledTask task, long delay) {
        schedule(task, delay, 0);
    }
    
    public void schedule(ScheduledTask task, long delay, long period) {
        task.setNextRuntimeMillis(System.currentTimeMillis()+delay);
        task.setPeriod(period);
        m_scheduledTasks.add(task);
    }

    protected int getRetryInfo(fd_set fdset, timeval tm) {
        fdset.set(getPmdFd());
        setTimeout(tm);
        return getPmdFd()+1;
    }

    private void setTimeout(timeval tm) {
        long now = System.currentTimeMillis();
        for(Iterator it = m_scheduledTasks.iterator(); it.hasNext();) {
            ScheduledTask task = (ScheduledTask)it.next();
            task.setTimeout(now, tm);
        }
    }

    public void log(String msg) {
        log.info(msg);
    }

    public synchronized void log(String msg, Throwable t) {
        log.info(msg, t);
    }

}
