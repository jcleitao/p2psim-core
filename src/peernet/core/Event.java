package peernet.core;

import peernet.transport.Address;

public class Event implements Comparable<Event> {
	public long time;
	public Address src;
	public Node node;
	public byte pid;
	public Object event;

	public Event() {
	}
	
	public Event(long time, Address src, Node node, byte pid, Object event) {
		this.time = time;
		this.src = src;
		this.node = node;
		this.pid = pid;
		this.event = event;
	}
	
	public String toString()
    {
      return this.event+" to node "+this.node+"prot "+this.pid+"at "+this.time;
    }

	@Override
	public int compareTo(Event o) {
		return Long.compare(this.time, o.time);
	}
}