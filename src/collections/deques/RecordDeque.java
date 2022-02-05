/**
 * 
 */
package collections.deques;

import java.util.ArrayDeque;
import java.util.Iterator;

import collections.elements.RecordElement;

/**
 * @author ForehandJA
 * 
 */

public class RecordDeque implements IRecordDeque
{
    ArrayDeque<RecordElement> deque;        // ArrayDeque containing the records.
    int                       maxNumRecords; // Max number of records in ArrayDeque.

    public RecordDeque()
    {
        deque = new ArrayDeque<RecordElement>();
    }

    public RecordDeque( RecordDeque ad )
    {
        deque = new ArrayDeque<RecordElement>( ad.deque );

    }

    /*
     * Set maximum number of records in ArrayDeque.
     * 
     * @see Deques.IRecordDeque#setMaxSize(int)
     */
    @Override
    public void setMaxSize( int maxSize )
    {
        maxNumRecords = maxSize;

        // Reduce the number of elements in the ArrayDeque if necessary;
        // records will be removed from the head.
        //
        if( maxNumRecords < deque.size() )
        {
            for( int i = deque.size(); i > maxNumRecords; i-- )
            {
                deque.pollFirst();
            }
        }
    }

    /*
     * Return number of records in the ArrayDeque
     * 
     * @see Deques.IRecordDeque#size()
     */
    @Override
    public int size()
    {
        return deque.size();
    }

    /*
     * Add Record to end of RecordDeque; if the RecordDeque is full, the head entry is removed to make room.
     * 
     * @see Deques.IRecordDeque#addRecord(java.lang.Record)
     */
    @Override
    public void addRecord( RecordElement rcd )
    {
        // If ArrayDeque is full remove the head element before
        // adding a new record.
        //
        if( deque.size() == maxNumRecords )
        {
            deque.pollFirst();
        }
        deque.add( rcd );
    }

    /**
     * 
     */
    @Override
    public void clear()
    {
        deque.clear();
    }

    /*
     * Returns an iterator which goes from head to tail
     * 
     * @see Deques.IRecordDeque#iterator()
     */
    @Override
    public Iterator<RecordElement> iterator()
    {
        return deque.iterator();
    }

    /*
     * Returns an iterator which goes from tail to head
     * 
     * @see Deques.IRecordDeque#descendingIterator()
     */
    @Override
    public Iterator<RecordElement> descendingIterator()
    {
        return deque.descendingIterator();
    }

    /*
     * Returns an iterator which goes from head to tail and only returns those records that contain the 'strToMatch'.
     * 
     * @see Deques.IRecordDeque#iterator(java.lang.Record)
     */
    @Override
    public Iterator<RecordElement> iterator( RecordElement rcdToMatch )
    {

        // TODO Auto-generated method stub
        return null;
    }

}
