package network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import mil.navy.nps.dis.EntityID;
import mil.navy.nps.dis.ProtocolDataUnit;
import mil.navy.nps.disEnumerations.PduTypeField;


public class PduProcessor
{
    private static PduProcessor m_oInstance = null;
    
    public static final int MAX_PDU_SIZE = 8192;
    
    private static DisThread       disThread = null;
    private static DatagramSocket  disSendSocket = null;
    private static int             disExerciseID = -1;
    private static String          disBroadcastAddr = null;
    private static int             disRecPort = -1;
    private static int             disSendPort = -1;

    private static InetAddress     ourOwnIPAddress = null;
    private static boolean         bProcessingPDUs = false;
    
    // Array to contain InetAddresses of machines we're interested in getting DIS PDUs from
    private static ArrayList<InetAddress> disProviders = new ArrayList<InetAddress>();
    
    
    /**
     * Member function to create the only instance of this class
     */
    public static synchronized PduProcessor getInstance()
    {
        if( m_oInstance == null )
        {
            m_oInstance = new PduProcessor();
        }
        
        return m_oInstance;
    }
    
    private PduProcessor()
    {
    }
    
    public void initialize(int exerciseID, String broadcastAddress, int recPort, int sendPort)
    {
        disExerciseID    = exerciseID;
        disBroadcastAddr = broadcastAddress;
        disRecPort          = recPort;
        disSendPort          = sendPort;
        
        try {
            ourOwnIPAddress = InetAddress.getByName( disBroadcastAddr );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        bProcessingPDUs = false;
    }

    public void startProcessing()
    {
        try
        {
            disSendSocket = new DatagramSocket(disRecPort);
            
            // Set socket timeout to be 1 second
            disSendSocket.setSoTimeout(1000);
            System.out.println("Connected [" + disBroadcastAddr + ", sendPort: " + disSendPort + ", receivePort: " + disRecPort + "] Exercise " + disExerciseID);
        }
        catch( IOException e )
        {
            e.printStackTrace();
            System.out.println("Unable to start DIS socket.");
            return;
        }
        
        disThread = new DisThread();
        disThread.start ();
        bProcessingPDUs = true;
    }

    public void stopProcessing()
    {
        bProcessingPDUs = false;
        
        while (disThread != null && disThread.isAlive())
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        disThread = null;
        
        if (disSendSocket != null)
        {
        	disSendSocket.close();
        	disSendSocket.disconnect();
        	disSendSocket = null;
            System.out.println("Disconnected");
        }

        m_oInstance = null;
    }

    // Add machine to a list of machines from which we are listening for
    // DIS traffic; DIS traffic from any other machines will be ignored.
    //
    static public void AddToDISProviderList( String machine )
    {
        try
        {
            InetAddress address = InetAddress.getByName( machine );
            if( address != null )
            {
                InetAddress disProviderAddr = InetAddress.getByAddress( address.getAddress() );
                if( disProviderAddr != null )
                {
                    disProviders.add( disProviderAddr );
                }
            }
        }
        catch (UnknownHostException e1)
        {
            e1.printStackTrace();
        }
    }

    //internal class
    public class DisThread extends Thread {
        
        public DisThread() {
            setName( "DisThread" );
        }
        
        @Override
		public void run() {
            
            EntityID entityID = null;
            
            while( bProcessingPDUs )
            {
                byte buffer[] = new byte[MAX_PDU_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                if( (disSendSocket == null) || disSendSocket.isClosed() )
                    continue;
                
                try
                {
                    disSendSocket.receive( packet );
                }
                catch( SocketTimeoutException e )
                {
                    // Timeout on socket occurred.
                    continue;
                }
                catch( IOException e )
                {
                    System.out.println("Discarding incoming PDU. Cannot convert to byte array.");
                    e.printStackTrace();
                    continue;
                }

                // Only process those that are coming from the IP addresses that are providing DIS for us
                if( !(disProviders.contains(packet.getAddress())) ) {
                    continue;
                }
                    
                byte[] datagramData = packet.getData();
                    
                // Convert datagram into PDU
                ProtocolDataUnit pdu = ProtocolDataUnit.byteArrayToPdu( datagramData );

                if( pdu != null )
                {
                    // Filter out PDUs that are not a part of our exercise
                    if( pdu.getExerciseID().intValue() != disExerciseID )
                    {
                        continue;
                    }
                        
                    switch (pdu.getPduType().shortValue())
                    {
                    case PduTypeField.DATA:

//                        //--------------------------------------------------------
//                        // The DATA PDUs we're interested in are those coming from
//                        // VBS2 that have camera data in them.
//                        //--------------------------------------------------------
//                        DataPdu dataPdu = (DataPdu)pdu;
//                        entityID = dataPdu.getReceivingEntityID();                        
//
//                        // Check to see if this entityID matches one of the devices in the device list
//                        Device device = Device.deviceMap.get( entityID );
//                        if( device == null )
//                        {
//                            continue;
//                        }
//
//                        // Get the script return value sent from VBS2 but only for cameras
//                        if( device.isCamera() )
//                        {
//                            ((PelcoDCamera)device).processCameraDataPduFromVBS2( dataPdu );
//                            continue;
//                        }
//                                
//                        // Get the variable data from the PDU
//                        DatumSpecification datumSpec = dataPdu.getDatumInformation();
//                                
//                        int varDatumCnt = datumSpec.getVariableDatumCount();
//                        if( varDatumCnt != 1 )
//                            return;
//                                
//                        VariableDatum varDatum = null;
//                                
//                        varDatum = datumSpec.variableDatumAt( 0 );
//                        if( varDatum.getVariableDatumID() != 2 )    // just making sure it came from VBS2
//                            return;
//
//                        int dataLen = varDatum.getVariableDatumLength();
//                        long[] longArr = varDatum.getVariableDatumValue();
//                                        
//                        String result = PelcoDCamera.putLongArrayIntoString( longArr, dataLen );
//                        if( result.equalsIgnoreCase("scalar bool array string nothing 0xe0ffffff") )  // why is this needed? tav;
//                            return;
//                                
//                        String resultSplit[] = result.split(",");
//                        int numResult = resultSplit.length;
//                        int dmsgID = (int)(Float.parseFloat(resultSplit[numResult-1].substring(0, resultSplit[numResult-1].length() - 1)));
//                                
//                        if( result.startsWith( "[\"fn_ecp_animateBarriers\"" ) )
//                        {
//                            if( device.isPopUpBarrier() || device.isDropArmGate() )
//                                TranslateLogger.OutputPDUInToLog(device,"BarrierAnimated", dmsgID);                                
//                        }
                        break;
                                
                    case PduTypeField.ENTITYSTATE:
                    	// send to entity state handler
                    case PduTypeField.SETDATA:
                    	// send to set data handler
                    default:
                        break;
                    }
                } // end while
            }
        }
    }
    
    public int sendPDU(ProtocolDataUnit pdu)
    {
        int returnValue = -1;

        if( !bProcessingPDUs )
            return returnValue;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        pdu.serialize(dos);

        byte [] pduByteArray = new byte [pdu.length()];
        pduByteArray = baos.toByteArray();
        try {
            dos.flush(); //not sure if the flush is necessary and/or correct
            baos.flush (); //not sure if the flush is necessary and/or correct
        }
        catch (IOException e) {
            System.out.println("flush threw exception");
            e.printStackTrace ();
            return returnValue;
        }

        return sendPDUBytes(pduByteArray);
    }

    private int sendPDUBytes( byte[] msg)
    { 
        if (disSendSocket == null)
            return -1;
        
        try
        {
            DatagramPacket packet = new DatagramPacket(msg, msg.length, ourOwnIPAddress, disSendPort);
            disSendSocket.send(packet);
        }
        catch (IOException e)
        {
            System.out.println( "No I/O" );
        }
        catch (NullPointerException e)
        {
        	System.out.println("null pointer exception");
            if (disSendSocket != null)
            {
                disSendSocket.close();
                disSendSocket = null;
            }
        }
        
        return msg.length;
    }
}
