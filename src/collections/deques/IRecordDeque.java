/**
 * 
 */
package collections.deques;

import java.util.Iterator;

import collections.elements.RecordElement;

/**
 * @author ForehandJA
 * 
 */
public interface IRecordDeque
{
    public void setMaxSize( int maxSize ); // Sets maximum number of Strings in StringDeque

    public int size(); // get number of Strings in StringDeque

    public void addRecord( RecordElement rcd ); // Add String to end of RecordDeque; if the RecordDeque is full, the
                                                // head entry
    // is removed to make room.

    public void clear(); // clears the deck.

    public Iterator<RecordElement> iterator(); // Returns an iterator which goes from
    // head to tail

    public Iterator<RecordElement> iterator( RecordElement rcdToMatch ); // Returns an iterator which goes from head to
                                                                         // tail and only
    // returns those strings that contain the 'rcdToMatch'.

    public Iterator<RecordElement> descendingIterator(); // Returns an iterator which
    // goes from tail to head
}
