package network;

import java.util.Arrays;

import mil.navy.nps.dis.DatumSpecification;
import mil.navy.nps.dis.EntityID;
import mil.navy.nps.dis.SetDataPdu;
import mil.navy.nps.dis.VariableDatum;

public class SetDataComposer
{
    // Static variable containing the current "message ID"
    private static int messageID = 0;
    
    protected SetDataPdu sdpdu;

	// Send a Set Data PDU to VBS to tell it what UAV to attach to; NOTE: this may be obsolete now
//	public static void sendUAVMessage( EntityID entityID, String translatedMsg )
//	{
////        ConsoleLogger.OutputConsoleText( "SENT THAT UAV MSG" );
//
//		// Put the VBS2 script name we want to run into the first and only variable datum field
//		String scriptCmd = "[";
//		scriptCmd += entityID.getSiteID();
//		scriptCmd += ",";
//		scriptCmd += entityID.getApplicationID();
//		scriptCmd += ",";
//		scriptCmd += entityID.getEntityID();
//		scriptCmd += ", 0] call fn_ecp_setUAVEntityID;";
//
////		Translator.getUAVDevice().sendSetDataPDU( scriptCmd, translatedMsg, Device.generateMessageID() );
//	}

    private static DatumSpecification addStrToDatumSpec(int datumID, String scriptCmd)
    {
        long[] longArr = putStringIntoLongArray( scriptCmd );
        
        // Create the variable datum field to hold the VBS2 script command.
        VariableDatum varDatum = new VariableDatum();
        
        // Must equal ScriptDatumID set in vbsClient.config
//        varDatum.setVariableDatumID( 2 );
        varDatum.setVariableDatumID(datumID);

        varDatum.setVariableDatumValue( longArr );

        // The following length is in 'bits', not 'bytes';
        // there are 8 bytes in a long and 8 bits in a byte.
        varDatum.setVariableDatumLength( 64 * longArr.length );

        DatumSpecification datumSpec = new DatumSpecification();
        datumSpec.addVariableDatum( varDatum );

        return datumSpec;
    }

    // The 'scriptCmd' is what will be sent to VBS2, e.g. a script name, a script function, etc.
    public static synchronized void sendSetDataPDU(EntityID entityID, int datumID, String scriptCmd, String translatedMsg, int msgID )
    {
    	SetDataPdu sdpdu = new SetDataPdu();

    	sdpdu.setOriginatingEntityID(entityID);

//    	  // Put the VBS2 script name we want to run into the first and only variable datum field;
//        if(scriptCmd.startsWith("["))
//        {
//            String scriptCmdSplit[] = scriptCmd.split(" ]");
//            scriptCmd = scriptCmdSplit[0] + ", " + msgID + " ]" + scriptCmdSplit[1];
//        }
//        else
//        {
//            scriptCmd = "[ " + msgID + " ] " + scriptCmd;
//        }

    	// Put the scriptCmd into the variable datum
    	sdpdu.setDatumInformation(addStrToDatumSpec(datumID, scriptCmd));

    	// Now set the timestamp in the PDU header
    	sdpdu.makeTimestampCurrent();

    	// Send the PDU
    	PduProcessor.getInstance().sendPDU(sdpdu);

    	// Write out the PDU to the log file
    	System.out.println("SetDataPDU [" + msgID + "]  " + translatedMsg);
    }

    // Method to set device specific setting in an entity state PDU
    //
//    public void setDeviceSpecificPDUInfo( EntityStatePdu esPDU )
//    {
//        // This method should be implemented in each device class
//    }
    
    // Copy byte-by-byte data from a long array to a byte array and then return a String
    public static String putLongArrayIntoString( long[] longArr, int dataLen )
    {
        if( longArr == null || dataLen < 1 )
            return null;
        
        int    numBytes   = dataLen / 8;
        byte[] byteArr    = new byte[numBytes];
        int    longNdx    = 0;
        int    byteOffset = 7;
        
        long longTmp = longArr[longNdx];
        
        for( int i = 0; i < numBytes; i++ )
        {
            byteArr[i] = (byte)((longTmp >> byteOffset*8) & 0x000000ff);
    
            if( byteOffset == 0 )
            {
                byteOffset = 7;
                longNdx++;
                if( i+1 < numBytes)
                    longTmp = longArr[longNdx];
            }
            else
            {
                byteOffset--;
            }
        }
        
        return new String(byteArr);
    }
    
    // Put the name of a script (including arguments) into a long[]
    public static long[] putStringIntoLongArray(String string)
    {
        int scriptCmdLen = string.length();
        byte[] scriptCmdByteArr = string.getBytes();
        long[] scriptCmdLongArr = new long[(scriptCmdLen / 8) + 1];
        Arrays.fill(scriptCmdLongArr, 0);

        int byteOffset = 7, longNdx = 0;
        long longTmp = 0;

        // Copy bytes of string into array of longs
        for (int i = 0; i < scriptCmdLen; i++)
        {
            longTmp = 0;
            longTmp = scriptCmdByteArr[i];
            longTmp = longTmp << byteOffset * 8;
            scriptCmdLongArr[longNdx] = scriptCmdLongArr[longNdx] | longTmp;

            if (byteOffset == 0)
            {
                byteOffset = 7;
                longNdx++;
            }
            else
                byteOffset--;
        }
        return scriptCmdLongArr;
    }
    
    // Generates a message number
    public static synchronized int generateMessageID()
    {
        messageID++;
        if (messageID < 0)
            messageID = 1;
        return messageID;
    }

    public static void resetMessageID()
    {
        messageID = 0;
    }
}
