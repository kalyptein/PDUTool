/**
 * 
 */
package collections.arrays;

import java.util.Iterator;

import collections.elements.RecordElement;

/**
 * @author ForehandJA
 * 
 */
public interface IRecordArrayBlockingQueue
{

    /**
     * 
     */
    public void setMaxSize( int max );

    /**
     * 
     */
    public int size();

    /**
     * 
     */
    public void addRecord( RecordElement rcd );

    /**
     * 
     */
    public void clear();

    /**
     * 
     */
    public Iterator<RecordElement> iterator();

}
