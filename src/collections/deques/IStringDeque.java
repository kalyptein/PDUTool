/**
 * 
 */
package collections.deques;

import java.util.Iterator;

/**
 * @author VarwigTA
 * 
 */
public interface IStringDeque
{
    public void setMaxSize( int maxSize ); // Sets maximum number of Strings in StringDeque

    public int size(); // get number of Strings in StringDeque

    public void addString( String str ); // Add String to end of StringDeque; if the StringDeque is full, the head entry
                                         // is removed to make room.

    public Iterator<String> iterator(); // Returns an iterator which goes from
                                        // head to tail

    public Iterator<String> iterator( String strToMatch ); // Returns an iterator which goes from head to tail and only
                                                           // returns those strings that contain the 'strToMatch'.

    public Iterator<String> descendingIterator(); // Returns an iterator which
                                                  // goes from tail to head

}
