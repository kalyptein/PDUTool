package network;

import geotransform.coords.Gcc_Coord_3d;
import geotransform.coords.Gdc_Coord_3d;
import geotransform.transforms.Gdc_To_Gcc_Converter;
import main.PDUToolMain;
import mil.navy.nps.dis.EntityID;
import mil.navy.nps.dis.EntityStatePdu;
import mil.navy.nps.dis.EntityType;
import mil.navy.nps.dis.EulerAngle;
import mil.navy.nps.dis.WorldCoordinate;
import utilities.UTMToGccConverter;

public class EntityStateComposer
{
    static PduThread entityStateThread;

    // Method to send an entity state PDU to OneSAF based on the
    // settings in this device.
    //
//    @Override
//	public synchronized void sendEntityStatePDU( String translatedMsg, int msgID )
//    {
//    	// Update PDU info
//    	esPDU.setEntityID( entityID );
//    	esPDU.setEntityType( entityType );
//
//    	esPDU.setEntityLocation( entityLocation );
//    	esPDU.setEntityOrientation( entityOrientation );
//
//    	// Set device specific info
//    	setDeviceSpecificPDUInfo( esPDU );
//
//    	// Now set the timestamp in the PDU header
//    	esPDU.makeTimestampCurrent();
//
//    	// Set msgID in the Alternative Entity Type Category
//    	byte bytes[] = ByteBuffer.allocate( 4 ).putInt( msgID ).array();
//
//    	esPDU.setAlternativeEntityTypeCategory( bytes[0] & 0xFF );
//    	esPDU.setAlternativeEntityTypeSubcategory( bytes[1] & 0xFF );
//    	esPDU.setAlternativeEntityTypeSpecific( bytes[2] & 0xFF );
//    	esPDU.setAlternativeEntityTypeExtra( bytes[3] & 0xFF );
//
//    	// Send the PDU
//    	PduProcessor.getInstance().sendPDU( esPDU );
//
//    	timestampOfLastPDUSent = System.currentTimeMillis();
//
//    	// Write out the PDU to the log file
//    	if (translatedMsg != null)
//    		TranslateLogger.OutputPDUOutToLog( this, translatedMsg, msgID );
//    }


    public static void startEntityStatePDUs(int exID, EntityID id, EntityType type, int force, long interval, WorldCoordinate coords, EulerAngle orientation)
    {
    	//create designator pdu and set values
    	EntityStatePdu esPdu = new EntityStatePdu();
    	
    	esPdu.setProtocolVersion(PDUToolMain.disVersion);

    	esPdu.setExerciseID(exID);
    	esPdu.setEntityType(type);
    	esPdu.setEntityID(id);
    	esPdu.setForceID(force);

    	esPdu.setEntityLocation(coords);
    	esPdu.setEntityOrientation(orientation);

    	entityStateThread = new PduThread(interval, esPdu);
    	entityStateThread.start();
    }
    
    protected static PduThread getEntityStateThread()
    {
        return entityStateThread;
    }

    public static void killEntityStateThread()
    {
        if (entityStateThread != null)
        {
            entityStateThread.setTimeToDie(true);
            
            while (entityStateThread.isAlive());
            
            entityStateThread = null;
        }
    }
    
    public static WorldCoordinate getCoords(double lat, double lng, double elev)
    {
    	Gdc_Coord_3d gdc = new Gdc_Coord_3d(lng, lat, elev);
    	Gcc_Coord_3d gcc = new Gcc_Coord_3d();                
    	Gdc_To_Gcc_Converter.Convert(gdc, gcc);
    	
    	return new WorldCoordinate(gcc.x, gcc.y, gcc.z);
    }
    
	public static EulerAngle getOrientation(Gdc_Coord_3d gdc, double yaw, double pitch, double roll)
	{
		yaw = (Math.PI / 2) - Math.toRadians(yaw);
		pitch = Math.toRadians(pitch);
		roll = Math.toRadians(roll);

		float psi = (float) UTMToGccConverter.computePsi(Math.toRadians(gdc.latitude), Math.toRadians(gdc.longitude), yaw, pitch);
		float theta = (float) UTMToGccConverter.computeTheta(Math.toRadians(gdc.latitude), pitch, yaw);
		float phi = (float) UTMToGccConverter.computePhi(Math.toRadians(gdc.latitude), pitch, roll, yaw);
		
		return new EulerAngle(psi, theta, phi);
	}		

    public static EntityID getEntityID(String entityIDStr)
    {
    	short[] entityIDFields = { 0, 0, 0 };
    	int i = 0;
    	for (String val: entityIDStr.split("\\."))
    	{
    		entityIDFields[i++] = Short.valueOf(val);
    		if (i > 2)
    			break;
    	}
    	
    	return new EntityID(entityIDFields[0], entityIDFields[1], entityIDFields[2]);
    }

    public static EntityType getEntityType(String entityTypeStr)
    {
        String[] fields = entityTypeStr.split( "\\." );

        EntityType enType = new EntityType();

        enType.setKind( Byte.parseByte(fields[0]));
        enType.setDomain( Byte.parseByte(fields[1]));
        enType.setCountry( Short.parseShort(fields[2]));
        enType.setCategory( Byte.parseByte(fields[3]));
        enType.setSubCategory( Byte.parseByte(fields[4]));
        enType.setSpecific( Byte.parseByte(fields[5]));
        enType.setExtra( Byte.parseByte(fields[6]));
        
        return enType;
    }
}
