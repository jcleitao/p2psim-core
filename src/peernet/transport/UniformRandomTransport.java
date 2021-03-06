package peernet.transport;

import java.nio.file.FileSystemNotFoundException;

import peernet.config.Configuration;
import peernet.config.IllegalParameterException;
import peernet.core.CommonState;
import peernet.core.Engine;
import peernet.core.Node;


/**
 * Implement a transport layer that reliably delivers messages with a random
 * delay, that is drawn from the configured interval according to the uniform
 * distribution.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.12 $
 */
public class UniformRandomTransport extends Transport {
    /**
     * String name of the parameter used to configure the minimum latency.
     *
     * @config
     */
    private static final String PAR_MINDELAY = "mindelay";
    /**
     * String name of the parameter used to configure the maximum latency.
     * Defaults to {@value #PAR_MINDELAY}, which results in a constant delay.
     *
     * @config
     */
    private static final String PAR_MAXDELAY = "maxdelay";
    /**
     * Minimum delay for message sending
     */
    private final long min;
    /**
     * Difference between the max and min delay plus one. That is, max delay is
     * min+range-1.
     */
    private final long range;


    /**
     * Reads configuration parameter.
     */
    public UniformRandomTransport(String prefix) {
        min = Configuration.getLong(prefix + "." + PAR_MINDELAY, 0);
        long max = Configuration.getLong(prefix + "." + PAR_MAXDELAY, min);
        if (max < min)
            throw new IllegalParameterException(prefix + "." + PAR_MAXDELAY,
                    "The maximum latency cannot be smaller than the minimum latency");
        range = max - min + 1;
    }


    /**
     * Returns <code>this</code>. This way only one instance exists in the system
     * that is linked from all the nodes. This is because this protocol has no
     * node specific state.
     */
    public Object clone() {
        return this;
    }


    /**
     * Delivers the message with a random delay, that is drawn from the configured
     * interval according to the uniform distribution.
     */
    public void send(Node src, Address dest, int pid, Object payload) {
        /**
         NetworkMessage m = (NetworkMessage) payload;
         if(m.getSender().equals(m.getDestination())) {
         System.err.println("ERROR sender==dest on message: " + payload.getClass().getCanonicalName() + " by process " + m.getDestination() + " @time " + CommonState.getTime());
         System.exit(1);
         }
         */
        // avoid calling nextLong if possible
        long delay = (range == 1 ? min : min + CommonState.r.nextLong(range));
        Address senderAddress = new AddressSim(src);
        //System.err.println("Delivery sceduled for " + dest + " of type " + payload.getClass().getCanonicalName() + " @time " + (CommonState.getTime() + delay));
        addEventIn(delay, senderAddress, ((AddressSim) dest).node, pid,
                payload);
    }
}
