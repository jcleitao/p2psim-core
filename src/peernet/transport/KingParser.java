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
package peernet.transport;

import java.io.*;
import java.util.*;

import peernet.config.*;
import peernet.core.Control;


/**
 * Initializes static singleton {@link RouterNetwork} by reading a king data
 * set.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.7 $
 */
public class KingParser implements Control {
    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------
    /**
     * The file containing the King measurements.
     *
     * @config
     */
    private static final String PAR_FILE = "file";

    /**
     * The number of time units in which a second is subdivided.
     *
     * @config
     */
    private static final String PAR_TICKS_PER_SEC = "ticks_per_sec";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------
    /**
     * Name of the file containing the King measurements.
     */
    private String filename;

    /**
     * Ratio between the time units used in the configuration file and the time
     * units used in the Peersim simulator.
     */
    private double ratio;

    /**
     * Prefix for reading parameters
     */
    private String prefix;


    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    /**
     * Read the configuration parameters.
     */
    public KingParser(String prefix) {
        this.prefix = prefix;
        int ticks_per_sec = Configuration.getInt(prefix + "." + PAR_TICKS_PER_SEC);
        ratio = ((double) ticks_per_sec) / 1000000;  // since King trace is in microseconds
        filename = Configuration.getString(prefix + "." + PAR_FILE, null);
    }


    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    /**
     * Initializes static singleton {@link RouterNetwork} by reading a king data
     * set.
     *
     * @return always false
     */
    public boolean execute() {
        BufferedReader in = null;
        if (filename != null) {
            try {
                in = new BufferedReader(new FileReader(filename));
            } catch (FileNotFoundException e) {
                throw new IllegalParameterException(prefix + "." + PAR_FILE, filename + " does not exist");
            }
        } else {
            in = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("t-king.map")));
        }

        String line = null;
        // Skip initial lines
        int size = 0;
        try {
            while ((line = in.readLine()) != null && !line.startsWith("node")) ;

            while (line != null && line.startsWith("node")) {
                size++;
                line = in.readLine();
            }
        } catch (IOException e) {
        }

        RouterNetwork.reset(size, true);
        System.err.println("KingParser: read " + size + " entries");
        try {
            do {
                StringTokenizer tok = new StringTokenizer(line, ", ");
                int n1 = Integer.parseInt(tok.nextToken()) - 1;
                int n2 = Integer.parseInt(tok.nextToken()) - 1;
                int latency = (int) (Double.parseDouble(tok.nextToken()) * ratio);
                RouterNetwork.setLatency(n1, n2, latency);
                line = in.readLine();
            }
            while (line != null);
        } catch (IOException e) {
        }
        return false;
    }
}
