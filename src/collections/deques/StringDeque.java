/**
 * 
 */
package collections.deques;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * @author VarwigTA
 * 
 */
public class StringDeque implements IStringDeque
{
    ArrayDeque<String> deque;        // ArrayDeque containing the strings.
    int                maxNumStrings; // Max number of strings in ArrayDeque.

    /*
     * Set maximum number of strings in ArrayDeque.
     * 
     * @see Deques.IStringDeque#setMaxSize(int)
     */
    @Override
    public void setMaxSize( int maxSize )
    {
        maxNumStrings = maxSize;

        // Reduce the number of elements in the ArrayDeque if necessary;
        // strings will be removed from the head.
        //
        if( maxNumStrings < deque.size() )
        {
            for( int i = deque.size(); i > maxNumStrings; i-- )
            {
                deque.pollFirst();
            }
        }
    }

    /*
     * Return number of strings in the ArrayDeque
     * 
     * @see Deques.IStringDeque#size()
     */
    @Override
    public int size()
    {
        return deque.size();
    }

    /*
     * Add String to end of StringDeque; if the StringDeque is full, the head entry is removed to make room.
     * 
     * @see Deques.IStringDeque#addString(java.lang.String)
     */
    @Override
    public void addString( String str )
    {
        // If ArrayDeque is full remove the head element before
        // adding a new string.
        //
        if( deque.size() == maxNumStrings )
        {
            deque.pollFirst();
        }
        deque.add( str );
    }

    /*
     * Returns an iterator which goes from head to tail
     * 
     * @see Deques.IStringDeque#iterator()
     */
    @Override
    public Iterator<String> iterator()
    {
        return deque.iterator();
    }

    /*
     * Returns an iterator which goes from tail to head
     * 
     * @see Deques.IStringDeque#descendingIterator()
     */
    @Override
    public Iterator<String> descendingIterator()
    {
        return deque.descendingIterator();
    }

    /*
     * Returns an iterator which goes from head to tail and only returns those strings that contain the 'strToMatch'.
     * 
     * @see Deques.IStringDeque#iterator(java.lang.String)
     */
    @Override
    public Iterator<String> iterator( String strToMatch )
    {

        // TODO Auto-generated method stub
        return null;
    }

}
