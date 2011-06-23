/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.concurrent;

//import org.apache.log4j.Category;
//import org.opennms.core.utils.ThreadCategory;

/**
 * <P>
 * QuantumSemaphore class is similar to a semaphore, but slightly different. The
 * class provides for a way to acquire a resource, but not to release one. The
 * idea is that the value of the <EM>semaphore</EM> is reset each quantum to
 * it's maximum value.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 */
public final class QuantumSemaphore extends Object {
    /**
     * <P>
     * The reset quantum in milliseconds.
     * </P>
     */
    final private long m_lQuantum;

    /**
     * <P>
     * The actual value that the semaphore is reset to after each elasped
     * quantum.
     * </P>
     */
    final private long m_lMaxValue;

    /**
     * <P>
     * The current value of the semaphore.
     * </P>
     */
    private long m_lCurValue;

    /**
     * <P>
     * The number of TimeTicks when the semaphore was last reset. If the
     * difference between the current time and this value exceed the time
     * quantium then the semaphore should be reset.
     * </P>
     */
    private long m_lastReset;

    /**
     * <P>
     * Returns the number of milliseconds since the last reset of the object.
     * </P>
     */
    private long timeSinceReset() {
        long now = System.currentTimeMillis();
        return now - m_lastReset;
    }

    /**
     * <P>
     * Resets the value of the semaphore.
     * </P>
     */
    private void reset() {
        m_lCurValue = m_lMaxValue;
        m_lastReset = System.currentTimeMillis();

        return;
    }

    /**
     * <P>
     * Constructs a new QuantumSempahore object with the specified maximum value
     * and time quantum. The object's values cannot be change once the object is
     * created.
     * </P>
     *
     * @param maxValue
     *            The maximum value of the semaphore
     * @param quantum
     *            The time quantum between resets, in milliseconds.
     */
    public QuantumSemaphore(long maxValue, long quantum) {
        m_lQuantum = quantum;
        m_lMaxValue = maxValue;
        m_lCurValue = maxValue;

        m_lastReset = System.currentTimeMillis();
    }

    /**
     * <P>
     * Returns a true value if the semaphore is successfully acquired by the
     * application. A false value is returned if the acquisition does not work.
     * </P>
     *
     * @return True if the semaphore is acquired, false otherwise.
     * @throws java.lang.InterruptedException if any.
     */
    public synchronized boolean acquire() throws InterruptedException {
        // Category log = ThreadCategory.getInstance(getClass());
        // if(log.isDebugEnabled())
        // log.debug("acquire: thread attempt to gain semaphore access");

        /*
         * Part of acquiring the resource is to calculate if the time quantum
         * has expired. If so then the maximum value is reset for the class, and
         * the time is noted. This may not be the optimal situation, but it
         * should guarentee that no more than _maxValue is used in any one
         * quantum!
         */
        boolean bRC = false;
        long ms = (long) m_lQuantum - timeSinceReset();
        if (ms < 0) {
            reset();
            // if(log.isDebugEnabled())
            // log.debug("acquire: semaphore value reset");
        } else if (m_lCurValue == 0 && ms > 0) {
            // has the effect of blocking without
            // releasing the lock!
            //
            // if(log.isDebugEnabled())
            // log.debug("acquire: semaphore sleeping thread for " + ms + "ms");

            Thread.sleep(ms);
            reset();

            // if(log.isDebugEnabled())
            // log.debug("acquire: semaphore value reset");
        }

        if (m_lCurValue > 0) {
            --m_lCurValue;
            bRC = true;
        }

        // if(log.isDebugEnabled())
        // log.debug("acquire: returning value " + bRC);

        return bRC;
    }

    /**
     * <P>
     * Returns the maximum value of the QuantumSemaphore.
     * </P>
     *
     * @return a long.
     */
    public long getMaxValue() {
        return m_lMaxValue;
    }
}
