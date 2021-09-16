package peernet.core;

import peernet.transport.Address;

public interface EventQueue {

	public void add(long time, Address src, Node node, byte pid, Object event);

	public Event removeFirst();
	
	public Events removeMany();

	public long getNextTime();

	/**
	 * Prints the time values contained in the heap.
	 */
	public String toString();
	
	/**
	 * 
	 * @return number of events in the queue
	 */
    public long size();
}
