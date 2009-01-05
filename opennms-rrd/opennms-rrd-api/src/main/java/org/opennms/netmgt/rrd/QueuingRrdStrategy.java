/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Jun 23: More Java 5 generics, eliminate extra log(...) methods, and
 *              eliminate @deprecated method inside log(). - dj@opennms.org
 * 2007 Apr 05: Use Java 5 generics. - dj@opennms.org
 * 2007 Mar 19: Added createGraphReturnDetails. - dj@opennms.org
 * 2004 Jul 08: Created this file.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * Provides queuing implementation of RrdStrategy.
 * 
 * In order to provide a more scalable collector. We created a queuing
 * RrdStrategy that enabled the system to amortize the high cost of opening an
 * round robin database across multiple updates.
 * 
 * This RrdStrategy implementation enqueues the create and update operations on
 * a per file basis and maintains a set of threads that process enqueued work
 * file by file.
 * 
 * If the I/O system can keep up with the collection threads while performing
 * only a single update per file then eventually all the data is processed and
 * the threads sleep until there is more work to do.
 * 
 * If the I/O system is initially slower than than the collection threads then
 * work will enqueue here and the write threads will get behind. As this happens
 * each file will eventually have more than a single update enqueued and
 * therefore the number of updates pushed thru the system will increase because
 * more then one will be output per 'open' Eventually, the I/O system and the
 * collection system will balance out. When this happens all data will be
 * collected but will not be output to the rrd files until the next time the
 * file is processed by the write threads.
 * 
 * As another performance improving strategy. The queue distinguishes between
 * files with significant vs insignificant updates. Files with only insignificant
 * updates are put at the lowest priority and are only written when the highest
 * priority updates have been written
 * 
 * This implementation delegates all the actual writing to another RrdStrategy
 * implementation.
 * 
 * System properties effecting the operation:
 * 
 * org.opennms.rrd.queuing.writethreads: (default 2) The number of rrd write
 * threads that process the queue
 * 
 * org.opennms.rrd.queuing.queueCreates: (default false) indicates whether rrd
 * file creates should be queued or processed synchronously
 * 
 * org.opennms.rrd.queuing.maxInsigUpdateSeconds: (default 0) the number of
 * seconds over which all files with significant updates only should be promoted
 * onto the significant less. This is to ensure they don't stay unprocessed
 * forever. Zero means not promotion.
 * 
 * org.opennms.rrd.queuing.modulus: (default 10000) the number of updates the
 * get enqueued between statistics output
 * 
 * org.opennms.rrd.queuing.category: (default "OpenNMS.Queued") the log category
 * to place the statistics output in
 * 
 * 
 * 
 * TODO: Promote files when ZeroUpdate operations can't be merged. This may be a
 * collection miss which we want to push thru. It should also help with memory.
 * 
 * TODO: Set an upper bound on enqueued operations
 * 
 * TODO: Provide an event that will write data for a particular file... Say
 * right before we try to graph it.
 */
public class QueuingRrdStrategy implements RrdStrategy, Runnable {

    RrdStrategy m_delegate;

    static final int UPDATE = 0;

    static final int CREATE = 1;

    static final int WRITE_THREADS = RrdConfig.getProperty("org.opennms.rrd.queuing.writethreads", 2);

    private static final boolean QUEUE_CREATES = RrdConfig.getProperty("org.opennms.rrd.queuing.queuecreates", false);
    
    private static final boolean PRIORITIZE_SIGS = RrdConfig.getProperty("org.opennms.rrd.queuing.prioritizeSignificantUpdates", false);

    private static final long INSIG_HIGH_WATER_MARK = RrdConfig.getProperty("org.opennms.rrd.queuing.inSigHighWaterMark", 0L);

    private static final long SIG_HIGH_WATER_MARK = RrdConfig.getProperty("org.opennms.rrd.queuing.sigHighWaterMark", 0L);

    private static final long QUEUE_HIGH_WATER_MARK = RrdConfig.getProperty("org.opennms.rrd.queuing.queueHighWaterMark", 0L);

    private static final long MODULUS = RrdConfig.getProperty("org.opennms.rrd.queuing.modulus", 10000L);

    private static final String LOG4J_CATEGORY = RrdConfig.getProperty("org.opennms.rrd.queuing.category", "OpenNMS.Queued");

    private static final long MAX_INSIG_UPDATE_SECONDS = RrdConfig.getProperty("org.opennms.rrd.queuing.maxInsigUpdateSeconds", 0L);

    private static final long WRITE_THREAD_SLEEP_TIME = RrdConfig.getProperty("org.opennms.rrd.queuing.writethread.sleepTime", 50L);

    private static final long WRITE_THREAD_EXIT_DELAY = RrdConfig.getProperty("org.opennms.rrd.queuing.writethread.exitDelay", 60000L);

    LinkedList<String> filesWithSignificantWork = new LinkedList<String>();

    LinkedList<String> filesWithInsignificantWork = new LinkedList<String>();

    Map<String, LinkedList<Operation>> pendingFileOperations = new HashMap<String, LinkedList<Operation>>();

    Map<Thread, String> fileAssignments = new HashMap<Thread, String>();

    Set<String> reservedFiles = new HashSet<String>();

    private long m_totalOperationsPending = 0;

    private long m_enqueuedOperations = 0;

    private long m_dequeuedOperations = 0;

    private long m_significantOpsEnqueued = 0;

    private long m_significantOpsDequeued = 0;

    private long m_significantOpsCompleted = 0;

    private long m_dequeuedItems = 0;

    private long m_createsCompleted = 0;

    private long m_updatesCompleted = 0;

    private long m_errors = 0;

    int threadsRunning = 0;

    private long m_startTime = 0;

    private long m_promotionCount = 0;

    long lastLap = System.currentTimeMillis();

    long lastStatsTime = 0;

    long lastEnqueued = 0;

    long lastDequeued = 0;

    long lastSignificantEnqueued = 0;

    long lastSignificantDequeued = 0;

    long lastSignificantCompleted = 0;

    long lastDequeuedItems = 0;

    long lastOpsPending = 0;

    /**
     * This is the base class for an enqueue able operation
     */
    static abstract class Operation {
        String fileName;

        int type;

        Object data;

        boolean significant;

        Operation(String fileName, int type, Object data, boolean significant) {
            this.fileName = fileName;
            this.type = type;
            this.data = data;
            this.significant = significant;
        }

        int getCount() {
            return 1;
        }

        String getFileName() {
            return this.fileName;
        }

        int getType() {
            return this.type;
        }

        Object getData() {
            return this.data;
        }

        boolean isSignificant() {
            return significant;
        }

        void addToPendingList(LinkedList<Operation> pendingOperations) {
            pendingOperations.add(this);
        }

        abstract Object process(Object rrd) throws Exception;

    }

    /**
     * This class represents an operation to create an rrd file
     */
    public class CreateOperation extends Operation {

        CreateOperation(String fileName, Object rrdDef) {
            super(fileName, CREATE, rrdDef, true);
        }

        Object process(Object rrd) throws Exception {
            // if the rrd is already open we are confused
            if (rrd != null) {
                log().debug("WHAT! rrd open but not created?");
                m_delegate.closeFile(rrd);
                rrd = null;
            }

            // create the file
            m_delegate.createFile(getData());

            // keep stats
            setCreatesCompleted(getCreatesCompleted() + 1);

            // return the file
            return rrd;

        }

    }

    /**
     * Represents an update to a rrd file.
     */
    public class UpdateOperation extends Operation {

        UpdateOperation(String fileName, String data) {
            super(fileName, UPDATE, data, true);
        }

        UpdateOperation(String fileName, String data, boolean significant) {
            super(fileName, UPDATE, data, significant);
        }

        Object process(Object rrd) throws Exception {
            // open the file if we need to
            if (rrd == null)
                rrd = m_delegate.openFile(getFileName());

            String update = (String) getData();

            try {
                // process the update
                m_delegate.updateFile(rrd, "", update);
            } catch (Exception e) {
                throw new Exception("Error processing update for file " + getFileName() + ": " + update, e);
            }

            // keep stats
            setUpdatesCompleted(getUpdatesCompleted() + 1);
            if (getUpdatesCompleted() % MODULUS == 0) {
                logStats();
            }
            // return the open rrd for further processing
            return rrd;

        }

    }

    /**
     * Represents an update whose value is 0. These operations can be merged
     * together and take up less memory
     */
    public class ZeroUpdateOperation extends UpdateOperation {

        long timeStamp;

        long interval = 0;

        int count;

        ZeroUpdateOperation(String fileName, long intitialTimeStamp) {
            super(fileName, "0", false);
            timeStamp = intitialTimeStamp;
            count = 1;
        }

        Object process(Object rrd) throws Exception {
            long ts = getFirstTimeStamp();
            for (int i = 0; i < count; i++) {
                // open the file if we need to
                if (rrd == null)
                    rrd = m_delegate.openFile(getFileName());

                String update = ts + ":0";
                try {
                    // process the update
                    m_delegate.updateFile(rrd, "", update);
                } catch (Exception e) {
                    throw new Exception("Error processing update " + i + " for file " + getFileName() + ": " + update, e);
                }
                ts += getInterval();

                // keep stats
                setUpdatesCompleted(getUpdatesCompleted() + 1);
                if (getUpdatesCompleted() % MODULUS == 0) {
                    logStats();
                }
            }
            return rrd;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int newCount) {
            this.count = newCount;
        }

        public long getFirstTimeStamp() {
            return timeStamp;
        }

        public long getLastTimeStamp() {
            return timeStamp + interval * (count - 1);
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long newInterval) {
            interval = newInterval;
        }

        public void mergeUpdates(ZeroUpdateOperation op) throws IllegalArgumentException {
            long opSpacing = op.getFirstTimeStamp() - getLastTimeStamp();
            long tolerance = getInterval() / 5;

            if (opSpacing == 0) {
                throw new IllegalArgumentException("unable to merge op because the spacing " + opSpacing + " is 0");

            }
            if (getInterval() > 0 && Math.abs(opSpacing - getInterval()) >= tolerance) {
                throw new IllegalArgumentException("unable to merge op because the spacing " + opSpacing + " is different than the current interval " + getInterval());
            }
            if (getInterval() > 0 && op.getInterval() > 0 && Math.abs(op.getInterval() - getInterval()) >= tolerance) {
                throw new IllegalArgumentException("unable to merge op because the new op interval " + op.getInterval() + " is different than the current interval " + getInterval());
            }

            int newCount = getCount() + op.getCount();
            long newInterval = ((getCount() - 1) * getInterval() + (op.getCount() - 1) + op.getInterval() + opSpacing) / (newCount - 1);

            setCount(newCount);
            setInterval(newInterval);

        }

        void addToPendingList(LinkedList<Operation> pendingOperations) {
            if (pendingOperations.size() > 0 && pendingOperations.getLast() instanceof ZeroUpdateOperation) {
                ZeroUpdateOperation zeroOp = (ZeroUpdateOperation) pendingOperations.getLast();
                try {
                    zeroOp.mergeUpdates(this);
                } catch (IllegalArgumentException e) {
                    log().debug(e.getMessage());
                    super.addToPendingList(pendingOperations);
                }
            } else {
                super.addToPendingList(pendingOperations);
            }
        }
    }

    public Operation makeCreateOperation(String fileName, Object rrdDef) {
        return new CreateOperation(fileName, rrdDef);
    }

    public Operation makeUpdateOperation(String fileName, String owner, String update) {
        try {
            int colon = update.indexOf(':');
            if ((colon >= 0) && (Double.parseDouble(update.substring(colon + 1)) == 0.0)) {
                long initialTimeStamp = Long.parseLong(update.substring(0, colon));
                if (initialTimeStamp == 0)
                    log().debug("ZERO ERROR: created a zero update with ts=0 for file: " + fileName + " data: " + update);

                return new ZeroUpdateOperation(fileName, initialTimeStamp);
            }
        } catch (NumberFormatException e) {

        }
        return new UpdateOperation(fileName, update);
    }

    // 
    // Queue management functions.
    //
    // TODO: Put this all in a class of its own. This is really ugly.
    //

    /**
     * Add an operation to the queue.
     */
    public void addOperation(Operation op) {
        synchronized (this) {
            if (queueIsFull()) {
                log().error("RRD Data Queue is Full!! Discarding operation for file "+op.getFileName());
                return;
            }
            
            if (op.isSignificant() && sigQueueIsFull()) {
                log().error("RRD Data Significant Queue is Full!! Discarding operation for file "+op.getFileName());
                return;
            }
            
            if (!op.isSignificant() && inSigQueueIsFull()) {
                log().error("RRD Insignificant Data Queue is Full!! Discarding operation for file "+op.getFileName());
                return;
            }
            
            storeAssignment(op);

            setTotalOperationsPending(getTotalOperationsPending() + 1);
            setEnqueuedOperations(getEnqueuedOperations() + 1);
            if (op.isSignificant())
                setSignificantOpsEnqueued(getSignificantOpsEnqueued() + 1);
            notifyAll();
            ensureThreadsStarted();
        }
    }

    
    private Category log() {
        return Logger.getLogger(LOG4J_CATEGORY);
    }

    private boolean queueIsFull() {
        if (QUEUE_HIGH_WATER_MARK <= 0)
            return false;
        else
            return getTotalOperationsPending() >= QUEUE_HIGH_WATER_MARK;
    }

    private boolean sigQueueIsFull() {
        if (SIG_HIGH_WATER_MARK <= 0)
            return false;
        else
            return getTotalOperationsPending() >= SIG_HIGH_WATER_MARK;
    }

    private boolean inSigQueueIsFull() {
        if (INSIG_HIGH_WATER_MARK <= 0)
            return false;
        else
            return getTotalOperationsPending() >= INSIG_HIGH_WATER_MARK;
    }

    /**
     * Ensure that we have threads started to process the queue.
     * 
     */
    public synchronized void ensureThreadsStarted() {
        if (threadsRunning < WRITE_THREADS) {
            threadsRunning++;
            Thread t = new Thread(this);
            t.start();
        }
    }

    /**
     * Get the operations for the next file that should be worked on.
     * 
     * @return a linkedList of operations to be processed all for the same file.
     */
    public LinkedList<Operation> getNext() {
        LinkedList<Operation> ops = null;
        synchronized (this) {

            // turn in our previous assignment
            completeAssignment();

            String newAssignment;
            // wait until there is work to do
            while ((newAssignment = selectNewAssignment()) == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // initialize start time for stats
            if (getStartTime() == 0)
                setStartTime(System.currentTimeMillis());

            // reserve the assignment and take work items
            ops = takeAssignment(newAssignment);

            // keep stats
            if (ops != null) {
                for(Operation op : ops) {
                    setTotalOperationsPending(getTotalOperationsPending()-op.getCount());
                    setDequeuedOperations(getDequeuedOperations() + op.getCount());
                    if (op.isSignificant()) {
                    	setSignificantOpsDequeued(getSignificantOpsDequeued() + op.getCount());
                    }
                }
                setDequeuedItems(getDequeuedItems() + 1);
            }
        }

        return ops;

    }

    /**
     * We need to track which files are being processed by which threads so that
     * we don't try to process updates for the same file on more than one
     * thread.
     */
    private synchronized void storeAssignment(Operation op) {
        // look and see if there a pending ops list for this file
        LinkedList<Operation> pendingOperations = pendingFileOperations.get(op.getFileName());

        // if not then we create an ops list for the file and add the file to
        // the work items list
        if (pendingOperations == null) {
            pendingOperations = new LinkedList<Operation>();
            pendingFileOperations.put(op.getFileName(), pendingOperations);

            // add the file to the correct list based on what type of work we
            // are adding.  (if we aren't prioritizing then every file is counted as
            // signficant
            if (!PRIORITIZE_SIGS || op.isSignificant())
                filesWithSignificantWork.addLast(op.getFileName());
            else
                filesWithInsignificantWork.addLast(op.getFileName());
        } else if (PRIORITIZE_SIGS && op.isSignificant() && hasOnlyInsignificant(pendingOperations)) {
            // only do this when we are prioritizing as this bumps files from inSig
            // up to insig
            // promote the file to the significant list if this is the first
            // significant
            filesWithSignificantWork.addLast(op.getFileName());
        }

        promoteAgedFiles();

        op.addToPendingList(pendingOperations);
    }

    /**
     * Ensure that files with insignificant changes are getting promoted if
     * necessary
     * 
     */
    private synchronized void promoteAgedFiles() {
        
        // no need to do this is we aren't prioritizing
        if (!PRIORITIZE_SIGS) return;

        // the num seconds to update files is 0 then use unfair prioritization
        if (MAX_INSIG_UPDATE_SECONDS == 0 || filesWithInsignificantWork.isEmpty())
            return;

        // calculate the elapsed time we first queued updates
        long now = System.currentTimeMillis();
        long elapsedMillis = Math.max(now - getStartTime(), 1);

        // calculate the milliseconds between promotions necessary to age
        // insignificant files into
        // the significant queue
        double millisPerPromotion = ((MAX_INSIG_UPDATE_SECONDS * 1000.0) / filesWithInsignificantWork.size());

        // calculate the number of millis since start until the next file needs
        // to be promotoed
        long nextPromotionMillis = (long) (millisPerPromotion * getPromotionCount());

        // if more time has elapsed than the next promotion time then promote a
        // file
        if (elapsedMillis > nextPromotionMillis) {
            String file = filesWithInsignificantWork.removeFirst();
            filesWithSignificantWork.addFirst(file);
            setPromotionCount(getPromotionCount() + 1);
        }

    }
    
    public synchronized void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        filesWithSignificantWork.addAll(0, rrdFiles);
    }

    /**
     * Return true if and only if all the operations in the list are
     * insignificant
     */
    private boolean hasOnlyInsignificant(LinkedList<Operation> pendingOps) {
        for(Operation op : pendingOps) {
            if (op.isSignificant()) {
                return false;
            }
        }
        return true;
    }

    /**
     * register the file that the currentThread is be working on. This enables
     * us to ensure that another thread doesn't try to work on operations for
     * that file.  Note: this is not synchronized as it is called from getNext which
     * is thread safe
     */
    private LinkedList<Operation> takeAssignment(String newAssignment) {

        // make the file as reserved by the current thread
        fileAssignments.put(Thread.currentThread(), newAssignment);
        reservedFiles.add(newAssignment);

        // get the assignments work list and return it
        return pendingFileOperations.remove(newAssignment);
    }

    /**
     * Return the name of the next file with available work
     */
    private String selectNewAssignment() {
        for (Iterator<String> it = filesWithSignificantWork.iterator(); it.hasNext();) {
            String fn = it.next();
            if (!reservedFiles.contains(fn)) {
                it.remove();
                return fn;
            }
        }
        for (Iterator<String> it = filesWithInsignificantWork.iterator(); it.hasNext();) {
            String fn = it.next();
            if (!reservedFiles.contains(fn)) {
                it.remove();
                return fn;
            }
        }
        return null;
    }

    /**
     * Record that fact that the current thread has finished process operations
     * for its current assignment
     */
    private synchronized void completeAssignment() {
        // remove any existing reservation of the current thread
        String previousAssignment = fileAssignments.remove(Thread.currentThread());
        if (previousAssignment != null)
            reservedFiles.remove(previousAssignment);
    }

    public QueuingRrdStrategy(RrdStrategy delegate) {
        m_delegate = delegate;
    }

    public RrdStrategy getDelegate() {
        return m_delegate;
    }

    //
    // RrdStrategy Implementation.. These methods just enqueue the calls as
    // operations
    //

    /*
     * (non-Javadoc)
     * 
     * @see RrdStrategy#closeFile(java.lang.Object)
     */
    public void closeFile(Object rrd) throws Exception {
        // no need to do anything here
    }

    /*
     * (non-Javadoc)
     * 
     * @see RrdStrategy#createDefinition(java.lang.String)
     */
    public Object createDefinition(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List<String> rraList) throws Exception {
        return createDefinition(creator, directory, dsName, step, Collections.singletonList(new RrdDataSource(dsName, dsType, dsHeartbeat, dsMin, dsMax)), rraList);
    }
    
    public Object createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        String fileName = directory + File.separator + rrdName + RrdUtils.getExtension();
        Object def = m_delegate.createDefinition(creator, directory, rrdName, step, dataSources, rraList);
        return makeCreateOperation(fileName, def);
    }


    /*
     * (non-Javadoc)
     * 
     * @see RrdStrategy#createFile(java.lang.Object)
     */
    public void createFile(Object op) throws Exception {
        if (QUEUE_CREATES)
            addOperation((Operation) op);
        else {
            m_delegate.createFile(((Operation) op).getData());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.rrd.RrdStrategy#initialize()
     */
    public void initialize() throws Exception {
        m_delegate.initialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.rrd.RrdStrategy#graphicsInitialize()
     */
    public void graphicsInitialize() throws Exception {
        m_delegate.graphicsInitialize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see RrdStrategy#openFile(java.lang.String)
     */
    public Object openFile(String fileName) throws Exception {
        return fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see RrdStrategy#updateFile(java.lang.Object, java.lang.String, java.lang.String)
     */
    public void updateFile(Object rrdFile, String owner, String data) throws Exception {
        addOperation(makeUpdateOperation((String) rrdFile, owner, data));
    }

    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValue(rrdFile, ds, interval);
    }
    
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }
    
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        return m_delegate.createGraph(command, workDir);
    }

    //
    // These methods are run by the write threads the process the queues.
    //

    public void run() {
        try {

            long waitStart = -1L;
            long delayed = 0;
            while (delayed < WRITE_THREAD_EXIT_DELAY) {
                if (getTotalOperationsPending() > 0) {
                    delayed = 0;
                    waitStart = -1L;
                    processPendingOperations();
                } else {
                    if (waitStart < 0) {
                        waitStart = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(WRITE_THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                    }
                    long now = System.currentTimeMillis();
                    delayed = now - waitStart;
                }

            }
        } finally {
            synchronized (this) {
                threadsRunning--;
                completeAssignment();
            }
        }
    }

    /**
     * Actually process the operations be calling the underlying delegate
     * strategy
     */
    private void processPendingOperations() {
        Object rrd = null;
        String fileName = null;

        try {
            LinkedList<Operation> ops = getNext();
            if (ops == null)
                return;
            // update stats correctly we update them even if an exception occurs
            // while we are processing
            for(Operation op : ops) {
                if (op.isSignificant()) {
                	setSignificantOpsCompleted(getSignificantOpsCompleted() + 1);
                }

            }
            // now we actually process the events
            for(Operation op : ops) {
                fileName = op.getFileName();
                rrd = op.process(rrd);
            }
        } catch (Exception e) {
            setErrors(getErrors() + 1);
            logLapTime("Error updating file " + fileName + ": " + e.getMessage());
            log().debug("Error upading file " + fileName + ": " + e.getMessage(), e);
        } finally {
            processClose(rrd);
        }
    }

    /**
     * close the rrd file
     */
    private void processClose(Object rrd) {
        if (rrd != null) {
            try {
                m_delegate.closeFile(rrd);
            } catch (Throwable t) {
                logLapTime("Throwable received while closing file", t);
            }
        }
    }

    /**
     * Print queue statistics.
     */
    public String getStats() {
        long now = System.currentTimeMillis();

        long currentElapsedMillis = Math.max(now - lastStatsTime, 1);
        long totalElapsedMillis = Math.max(now - getStartTime(), 1);

        long currentEnqueuedOps = (getEnqueuedOperations() - lastEnqueued);
        long currentDequeuedOps = (getDequeuedOperations() - lastDequeued);
        long currentDequeuedItems = (getDequeuedItems() - lastDequeuedItems);

        long currentSigOpsEnqueued = (getSignificantOpsEnqueued() - lastSignificantEnqueued);
        long currentSigOpsDequeued = (getSignificantOpsDequeued() - lastSignificantDequeued);
        //long currentSigOpsCompleted = (significantOpsCompleted - lastSignificantCompleted);

        long currentEnqueueRate = (long) (currentEnqueuedOps * 1000.0 / currentElapsedMillis);
        long currentSigEnqueueRate = (long) (currentSigOpsEnqueued * 1000.0 / currentElapsedMillis);
        long currentInsigEnqueueRate = (long) ((currentEnqueuedOps - currentSigOpsEnqueued) * 1000.0 / currentElapsedMillis);
        long overallEnqueueRate = (long) (getEnqueuedOperations() * 1000.0 / totalElapsedMillis);
        long overallSigEnqueueRate = (long) (getSignificantOpsEnqueued() * 1000.0 / totalElapsedMillis);
        long overallInsigEnqueueRate = (long) ((getEnqueuedOperations() - getSignificantOpsEnqueued()) * 1000.0 / totalElapsedMillis);

        long currentDequeueRate = (long) (currentDequeuedOps * 1000.0 / currentElapsedMillis);
        long currentSigDequeueRate = (long) (currentSigOpsDequeued * 1000.0 / currentElapsedMillis);
        long currentInsigDequeueRate = (long) ((currentDequeuedOps - currentSigOpsDequeued) * 1000.0 / currentElapsedMillis);
        long overallDequeueRate = (long) (getDequeuedOperations() * 1000.0 / totalElapsedMillis);
        long overallSigDequeueRate = (long) (getSignificantOpsDequeued() * 1000.0 / totalElapsedMillis);
        long overallInsigDequeueRate = (long) ((getDequeuedOperations() - getSignificantOpsDequeued()) * 1000.0 / totalElapsedMillis);

        long currentItemDequeueRate = (long) (currentDequeuedItems * 1000.0 / currentElapsedMillis);
        long overallItemDequeueRate = (long) (getDequeuedItems() * 1000.0 / totalElapsedMillis);

        String stats = "\nQS:\t" + "totalOperationsPending=" + getTotalOperationsPending() + 
        ", significantOpsPending=" + (getSignificantOpsEnqueued() - getSignificantOpsCompleted()) + 
        ", filesWithSignificantWork=" + filesWithSignificantWork.size() + 
        ", filesWithInsignificantWork=" + filesWithInsignificantWork.size()

        + "\nQS:\t" + ", createsCompleted=" + getCreatesCompleted() + 
        ", updatesCompleted=" + getUpdatesCompleted() + 
        ", errors=" + getErrors() + 
        ", promotionRate=" + ((double) (getPromotionCount() * 1000.0 / totalElapsedMillis)) + 
        ", promotionCount=" + getPromotionCount()

        + "\nQS:\t" + ", currentEnqueueRates=(" + currentSigEnqueueRate + "/" + currentInsigEnqueueRate + "/" + currentEnqueueRate + ")" + 
        ", currentDequeueRate=(" + currentSigDequeueRate + "/" + currentInsigDequeueRate + "/" + currentDequeueRate + ")" + 
        ", currentItemDequeRate=" + currentItemDequeueRate + 
        ", currentOpsPerUpdate=" + (currentDequeuedOps / Math.max(currentDequeuedItems, 1.0)) + 
        ", currentPrcntSignificant=" + (currentSigOpsEnqueued * 100.0 / Math.max(currentEnqueuedOps, 1.0)) + "%" + ", elapsedTime=" + ((currentElapsedMillis + 500) / 1000)

        + "\nQS:\t" + ", overallEnqueueRate=(" + overallSigEnqueueRate + "/" + overallInsigEnqueueRate + "/" + overallEnqueueRate + ")" + 
        ", overallDequeueRate=(" + overallSigDequeueRate + "/" + overallInsigDequeueRate + "/" + overallDequeueRate + ")" + 
        ", overallItemDequeRate=" + overallItemDequeueRate + 
        ", overallOpsPerUpdate=" + (getDequeuedOperations() / Math.max(getDequeuedItems(), 1.0)) + 
        ", overallPrcntSignificant=" + (getSignificantOpsEnqueued() * 100.0 / Math.max(getEnqueuedOperations(), 1.0)) + "%" + 
        ", totalElapsedTime=" + ((totalElapsedMillis + 500) / 1000);

        lastStatsTime = now;
        lastEnqueued = getEnqueuedOperations();
        lastDequeued = getDequeuedOperations();
        lastDequeuedItems = getDequeuedItems();
        lastSignificantEnqueued = getSignificantOpsEnqueued();
        lastSignificantDequeued = getSignificantOpsDequeued();
        lastSignificantCompleted = getSignificantOpsCompleted();
        lastOpsPending = getTotalOperationsPending();

        return stats;
    }

    public void logStats() {
        logLapTime(getStats());
    }

    void logLapTime(String message) {
        log().debug(message + " " + getLapTime());
    }
    
    void logLapTime(String message, Throwable t) {
        log().debug(message + " " + getLapTime(), t);
    }

    public String getLapTime() {
        long newLap = System.currentTimeMillis();
        double seconds = (newLap - lastLap) / 1000.0;
        lastLap = newLap;
        return "[" + seconds + " sec]";
    }

    public int getGraphLeftOffset() {
        return m_delegate.getGraphLeftOffset();
    }

    public int getGraphRightOffset() {
        return m_delegate.getGraphRightOffset();
    }

    public int getGraphTopOffsetWithText() {
        return m_delegate.getGraphTopOffsetWithText();
    }

    public String getDefaultFileExtension() {
        return m_delegate.getDefaultFileExtension();
    }

    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, RrdException {
        return m_delegate.createGraphReturnDetails(command, workDir);
    }

	public long getTotalOperationsPending() {
		return m_totalOperationsPending;
	}

	public void setTotalOperationsPending(long totalOperationsPending) {
		m_totalOperationsPending = totalOperationsPending;
	}

	public long getCreatesCompleted() {
		return m_createsCompleted;
	}

	public void setCreatesCompleted(long createsCompleted) {
		m_createsCompleted = createsCompleted;
	}

	public long getUpdatesCompleted() {
		return m_updatesCompleted;
	}

	public void setUpdatesCompleted(long updatesCompleted) {
		m_updatesCompleted = updatesCompleted;
	}

	public long getErrors() {
		return m_errors;
	}

	public void setErrors(long errors) {
		m_errors = errors;
	}

	public long getPromotionCount() {
		return m_promotionCount;
	}

	public void setPromotionCount(long promotionCount) {
		m_promotionCount = promotionCount;
	}

	public long getSignificantOpsEnqueued() {
		return m_significantOpsEnqueued;
	}

	public void setSignificantOpsEnqueued(long significantOpsEnqueued) {
		m_significantOpsEnqueued = significantOpsEnqueued;
	}

	public long getSignificantOpsDequeued() {
		return m_significantOpsDequeued;
	}

	public void setSignificantOpsDequeued(long significantOpsDequeued) {
		m_significantOpsDequeued = significantOpsDequeued;
	}

	public long getEnqueuedOperations() {
		return m_enqueuedOperations;
	}

	public void setEnqueuedOperations(long enqueuedOperations) {
		m_enqueuedOperations = enqueuedOperations;
	}

	public long getDequeuedOperations() {
		return m_dequeuedOperations;
	}

	public void setDequeuedOperations(long dequeuedOperations) {
		m_dequeuedOperations = dequeuedOperations;
	}

	public long getDequeuedItems() {
		return m_dequeuedItems;
	}

	public void setDequeuedItems(long dequeuedItems) {
		m_dequeuedItems = dequeuedItems;
	}

	public long getSignificantOpsCompleted() {
		return m_significantOpsCompleted;
	}

	public void setSignificantOpsCompleted(long significantOpsCompleted) {
		m_significantOpsCompleted = significantOpsCompleted;
	}

	public long getStartTime() {
		return m_startTime;
	}

	public void setStartTime(long updateStart) {
		m_startTime = updateStart;
	}


}
