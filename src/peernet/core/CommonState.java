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
import peernet.core.Engine.Type;
import peernet.util.ExtendedRandom;


/**
 * This is the common state of the simulation all objects see. Static singleton.
 * One of its purposes is simplification of parameter structures and increasing
 * efficiency by putting state information here instead of passing parameters.
 * <p>
 * <em>The set methods should not be used by applications</em>, they are for
 * system components. Use them only if you know exactly what you are doing, eg
 * if you are so advanced that you can write your own simulation engine.
 * Ideally, they should not be visible, but due to the lack of more flexibility
 * in java access rights, we are forced to make them public.
 */
public class CommonState
{
	/**
	 * Current time. Note that this value is simulator independent, all simulation
	 * models have a notion related to time. For example, in the cycle based
	 * model, the cycle id gives time, while in even driven simulations there is a
	 * more realistic notion of time.
	 */
	private static long time = 0;

	/**
	 * The maxival value {@link #time} can ever take.
	 */
	private static long endTime = -1;
	private static long zeroTime = -1;

	/**
	 * Number of used bits in the long representation of time, calculated based on
	 * the endtime.
	 */
	//  private static int toshift = -1;

	/**
	 * Experience name
	 */
	private static String experienceName;

	/**
	 * This source of randomness should be used by all components. This field is
	 * public because it doesn't matter if it changes during an experiment
	 * (although it shouldn't) until no other sources of randomness are used
	 * within the system. Besides, we can save the cost of calling a wrapper
	 * method, which is important because this is needed very often.
	 */
	public static ExtendedRandom r = null;


	// ======================== initialization =========================
	// =================================================================

	/**
	 * Configuration parameter used to define which random generator class should
	 * be used. If not specified, the default implementation
	 * {@link ExtendedRandom} is used. User-specified random generators must
	 * extend class {@link ExtendedRandom}.
	 * 
	 * @config
	 */
	public static final String PAR_RANDOM = "random";

	/**
	 * Configuration parameter used to initialize the random seed. If it is not
	 * specified the current time is used.
	 * 
	 * @config
	 */
	public static final String PAR_SEED = "random.seed";

	/**
	 * Initializes the field {@link r} according to the configuration. Assumes
	 * that the configuration is already loaded.
	 */
	static
	{
		long seed = Configuration.getLong(PAR_SEED, System.currentTimeMillis());
		initializeRandom(seed);
	}



	/** Does nothing. To avoid construction but allow extension. */
	protected CommonState()
	{
	}



	// ======================= methods =================================
	// =================================================================
	/**
	 * Returns current time.
	 * 
	 * In SIM mode, this is the simulation time (in ticks).
	 * 
	 * In EMU and NET mode, this is the number of milliseconds elapsed since
	 * the start of the experiment. Note that time starts counting 
	 * 
	 * In event-driven simulations, returns the current time
	 * (a long-value). In cycle-driven simulations, returns the current cycle (a
	 * long that can safely be cast into an integer).
	 */
	public static long getTime()
	{
		Type t = Engine.getType();
		if (t==Type.SIM || t==Type.SIM_CUSTOM )
			return time;
		else
		{
			if (zeroTime==-1)
				return 0;
			else
				return System.currentTimeMillis()-zeroTime;
		}
	}


	static void timeStartsNow()
	{
		if (zeroTime!=-1)
			throw new IllegalStateException("Cannot reset time to zero for a second time");

		zeroTime = System.currentTimeMillis();
	}

	/**
	 * Sets the current time.
	 */
	public static void setTime(long t)
	{
		time = t;
	}



	/**
	 * Returns endtime. It is the maximal value {@link #getTime} ever returns. If
	 * it's negative, it means the endtime is not known.
	 */
	public static long getEndTime()
	{
		return endTime;
	}



	/**
	 * Sets the endtime.
	 */
	public static void setEndTime(long t)
	{
		if (endTime>=0)
			throw new RuntimeException("You can set endtime only once");

		if (t<0)
			throw new RuntimeException("No negative values are allowed");

		endTime = t;
	}



	public static void initializeRandom(long seed)
	{
		if (r==null)
			r = (ExtendedRandom) Configuration.getInstance(PAR_RANDOM, new ExtendedRandom(seed));

		r.setSeed(seed);
	}



	public static long getPendingEvents()
	{
		return Engine.instance().pendingEvents();
	}



	public static String getExperienceName() {
		return experienceName;
	}



	public static void setExperienceName(String experienceName) {
		CommonState.experienceName = experienceName;
	}
}
