/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package peernet.core;

import peernet.config.Configuration;
import peernet.config.IllegalParameterException;


// XXX a quite primitive scheduler, should be able to be configured
// much more flexibly using a simple syntax for time ranges.

/**
 * A binary function over the time points. That is, for each time point returns
 * a boolean value.
 * <p>
 * The concept of time depends on the simulation model. Current time has to be
 * set by the simulation engine, irrespectively of the model, and can be read
 * using {@link CommonState#getTime()}. This scheduler is interpreted over those
 * time points.
 *
 * <p>
 * In this simple implementation the valid times will be
 * <tt>from, from+step, from+2*step, etc,</tt> where the last element is
 * strictly less than <tt>until</tt>. Alternatively, if <tt>at</tt> is defined,
 * then the schedule will be a single time point. If <tt>final</tt> is defined, it is
 * also added to the set of active time points. It refers to the time after the
 * simulation has finished (see {@link CommonState#getPhase}).
 */
public class Schedule {
    // ========================= fields =================================
    // ==================================================================
    /**
     * The period of execution.
     *
     * @config
     */
    public static final String PAR_STEP = "step";

    /**
     * Defaults to -1. That is, defaults to be ineffective.
     *
     * @config
     */
    public static final String PAR_AT = "at";

    /**
     * Defaults to 0.
     *
     * @config
     */
    public static final String PAR_FROM = "from";

    /**
     * Defaults to <tt>Long.MAX_VALUE</tt>.
     *
     * @config
     */
    public static final String PAR_UNTIL = "until";

    /**
     * Defines if component is active after the simulation has finished. Note that
     * the exact time the simulation finishes is not know in advance because other
     * components can stop the simulation at any time. By default not set.
     *
     * @config
     * @see CommonState#getPhase
     */
    private static final String PAR_FINAL = "final";

    /**
     * If set, it means that the initial execution of the given protocol is
     * scheduled for a different random time for all nodes. The random time is a
     * sample between the current time (inclusive) and the cycle length
     * (exclusive), the latter being specified by the step parameter (see
     * {@link Schedule}) of the assigned protocol.
     *
     * @config
     * @see #execute
     */
    private static final String PAR_RANDOMSTART = "randstart";


    public int schedId;
    public long step;
    public long from;
    public long until;
    public boolean fin;
    public boolean randomStart;


    /**
     * The next scheduled time point.
     */
    protected long next;


    // ==================== initialization ==============================
    // ==================================================================

    /**
     * Reads configuration parameters from the component defined by
     * <code>prefix</code>.
     * <p>
     * Either parameter {@value #PAR_AT} or {@value #PAR_STEP} must be defined.
     */
    public Schedule(String prefix) {
        fin = Configuration.contains(prefix + "." + PAR_FINAL);

        if (Configuration.contains(prefix + "." + PAR_AT)) // AT defined
        {
            // FROM, UNTIL, and STEP should *not* be defined
            if (Configuration.contains(prefix + "." + PAR_FROM) ||
                    Configuration.contains(prefix + "." + PAR_UNTIL) ||
                    Configuration.contains(prefix + "." + PAR_STEP))
                throw new IllegalParameterException(prefix, "Cannot use \"" + PAR_AT +
                        "\" and \"" + PAR_FROM +
                        "\"/\"" + PAR_UNTIL +
                        "\"/\"" + PAR_STEP +
                        "\" together");
            long at = Configuration.getLong(prefix + "." + PAR_AT);
            from = at;
            until = at;
            step = 1;
        } else // AT not defined
        {
            from = Configuration.getLong(prefix + "." + PAR_FROM, 0);
            until = Configuration.getLong(prefix + "." + PAR_UNTIL, Long.MAX_VALUE);
            step = Configuration.getLong(prefix + "." + PAR_STEP, -1);
            randomStart = Configuration.contains(prefix + "." + PAR_RANDOMSTART);

            if (step == -1) // STEP not defined
            {
                from = until = step = -1;
                if (Configuration.contains(prefix + "." + PAR_FROM) || Configuration.contains(prefix + "." + PAR_UNTIL))
                    System.err.println("Warning: Control " + prefix + " defines \"" + PAR_FROM + "\"/\"" + PAR_UNTIL + "\" but not \"" + PAR_STEP + "\"");
                if (!fin) // FINAL not defined either
                    System.err.println("Warning: Control " + prefix + " will not execute at all!");
            }
        }
        next = from;
    }


    /**
     * Returns true if the given time point is covered by this scheduler
     */
    public boolean active(long time) {
        if (time < from || time > until)
            return false;
        return (time - from) % step == 0;
    }


    // -------------------------------------------------------------------

    /**
     * Returns the next time point. If the returned value is negative, there are
     * no more time points. As a side effect, it also updates the next time point,
     * so repeated calls to this method return the scheduled times.
     */
    public long getNext() {
        long ret = next;
        next += step;
        if (next >= until)
            ret = -1;
        return ret;
    }


    /*package*/ long initialDelay() {
        if (randomStart)
            return from + CommonState.r.nextLong(step);
        else
            return from;
    }


  /**
   * Change Log: Joao Leitao: made this method publix to allow access by classes outside this pacakge. 
   * @param time
   * @return
   */
  public long nextDelay(long time)
  {
    if (time+step <= until)
      return step;
    else
      return -1;
  }

}
