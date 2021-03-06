/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peernet.transport;

import peernet.core.Control;


/**
 * Initializes static singleton {@link RouterNetwork} by reading a king data set.
 *
 * @author Spyros Voulgaris
 * @version $Revision: 1.0$
 */
public class UniformLatencies implements Control {
// ---------------------------------------------------------------------
// Fields
// ---------------------------------------------------------------------

    /**
     * Prefix for reading parameters
     */
    private String prefix;

    /**
     * Number of nodes in data matrix.
     */
    private final int size = 1;

// ---------------------------------------------------------------------
// Initialization
// ---------------------------------------------------------------------

    /**
     * Read the configuration parameters.
     */
    public UniformLatencies(String prefix) {
        this.prefix = prefix;
    }

// ---------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------

    /**
     * Initializes static singleton {@link RouterNetwork} by reading a king data set.
     *
     * @return always false
     */
    public boolean execute() {
        RouterNetwork.reset(size, true);
        return false;
    }

}
