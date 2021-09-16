package peernet.core;

/**
 * @author Margarida Mamede
 */


public class Events {

    // -----------------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------------

    // The events returned by the method removeMany are stored in the array
    // from position 0 to position size-1.
    public Event[] array;

    // Number of events in the array.
    public int size;


    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /** 
     * Creates an instance whose array has the specified capacity.
     *
     * @param capacity: the capacity of the array.
     */
    public Events( int capacity ) {
        array = new Event[capacity];
        for ( int i = 0; i < capacity; i++ )
            array[i] = new Event();
        size = 0;
    }

    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /** 
     * Returns the number of events in the array.
     */
    public int size( ) {
        return size;
    }

    /** 
     * Returns the array of events.
     */
    public Event[] array( ) {
        return array;
    }

}
