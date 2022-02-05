/**
 * 
 */
package collections.arrays;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;


import collections.elements.RecordElement;

/**
 * @author ForehandJA
 * 
 */
public class RecordArrayBlockingQueue implements IRecordArrayBlockingQueue, Iterable<RecordElement>
{
    ArrayBlockingQueue<RecordElement> arrayQueue;
    int                               maxNumRecords;

    public RecordArrayBlockingQueue( int max )
    {
        maxNumRecords = max;
        arrayQueue = new ArrayBlockingQueue<RecordElement>( maxNumRecords );
    }

    public RecordArrayBlockingQueue( int max, RecordArrayBlockingQueue rabq )
    {
        maxNumRecords = max;
        arrayQueue = new ArrayBlockingQueue<RecordElement>( maxNumRecords, true, rabq.arrayQueue );
    }

    @Override
	public void setMaxSize( int max )
    {
        synchronized( arrayQueue )
        {
            maxNumRecords = max;
            ArrayBlockingQueue<RecordElement> tempArrayQueue = new ArrayBlockingQueue<RecordElement>( maxNumRecords );

            if( maxNumRecords < arrayQueue.size() )
            {
                for( int i = arrayQueue.size(); i > maxNumRecords; i-- )
                {
                    arrayQueue.poll();
                }
            }
            arrayQueue.drainTo( tempArrayQueue );
            tempArrayQueue.drainTo( arrayQueue, maxNumRecords );
        }

    }

    @Override
	public int size()
    {
        return arrayQueue.size();
    }

    @Override
	public void addRecord( RecordElement rcd )
    {
        synchronized( arrayQueue )
        {
            if( arrayQueue.size() == maxNumRecords )
            {
                arrayQueue.poll();
            }
            arrayQueue.add( rcd );
        }
    }

    @Override
	public void clear()
    {
        synchronized( arrayQueue )
        {
            arrayQueue.clear();
        }
    }

    @Override
	public Iterator<RecordElement> iterator()
    {
        synchronized( arrayQueue )
        {
            return arrayQueue.iterator();
        }
    }

}
