package collections.elements;

/**
 * @author ForehandJA
 * 
 * 
 * 
 */
public class RecordElement
{
    String str;
    String entityID;
    String entityType;
    String comPort;
    String actualName;

    /**
     * Record constructor
     */
    public RecordElement( String s, String eid, String et, String an, String cp )
    {
        str = s;
        entityID = eid;
        entityType = et;
        actualName = an;
        comPort = cp;
    }

    /**
     * Sets the string field
     * 
     * @param s replacement str
     */
    public void setStr( String s )
    {
        str = s;
    }

    /**
     * Gets the string field
     * 
     * @return str
     */
    public String getStr()
    {
        return str;
    }

    /**
     * Sets the EntityID
     * 
     * @param s replacement entityID
     */
    public void setEntityID( String s )
    {
        entityID = s;
    }

    /**
     * Gets the EntityID
     * 
     * @return entityID
     */
    public String getEntityID()
    {
        return entityID;
    }
    
    /**
     * Sets the EntityType
     * 
     * @param s replacement EntityType
     */
    public void setEntityType(String s){
        entityType = s;
    }
    
    /**
     * Gets the EntityType
     * 
     * @return entityType
     */
    public String getEntityType(){
        return entityType;
    }
    
    /**
     * Sets the ActualName
     * 
     * @param s replacement actualName
     */
    public void setActualName(String s){
        actualName = s;
    }
    
    /**
     * Gets the ActualName
     * 
     * @return actualName
     */
    public String getActualName(){
        return actualName;
    }
    
    /**
     * Sets the ComPort
     * 
     * @param s replacement comPort
     */
    public void setComPort(String s){
        comPort = s;
    }
    
    /**
     * Gets the ComPort
     * 
     * @return comPort
     */
    public String getComPort(){
        return comPort;
    }
}
