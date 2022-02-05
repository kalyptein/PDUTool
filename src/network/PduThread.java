package network;

import mil.navy.nps.dis.ProtocolDataUnit;

public class PduThread extends Thread
{
	/** pdu to send repeatedly */
	private ProtocolDataUnit pdu;

	/** pdu to send at end of thread */
	private ProtocolDataUnit terminalPdu;

	/** flag to indicate if the thread should end */
	private boolean endThread = false;

	/** time between sending pdus, in milliseconds */
	private long interval;
	
	/** lifespan of thread, in milliseconds */
	private long threadDuration;

	public PduThread(long interval, ProtocolDataUnit pdu)
	{
		this(interval, pdu, -1, null);
	}

	public PduThread(long interval, ProtocolDataUnit pdu, ProtocolDataUnit terminal)
	{
		this(interval, pdu, -1, terminal);
	}

	public PduThread(long _interval, ProtocolDataUnit _pdu, long _duration, ProtocolDataUnit _terminal)
	{
		interval = _interval;
		pdu = _pdu;
		threadDuration = _duration;
		terminalPdu = _terminal;
	}

	/**
	 * Run method will send out designation pdu's on a regular basis, determined by the
	 * m_sendInterval value.
	 */
	@Override
    public void run()
	{
		long start = System.currentTimeMillis();
		long last = -1;
		
		// new Date, set at current time in milliseconds, used as continuous counter
//		Date timer = new Date();
		
		// used as "base" time
//		Date timerBase = new Date();
		
		// used as starting time
//		Date startTime = new Date();

		while(!endThread)
		{
			//set timer to current time
//			timer.setTime(new Date().getTime());
			long time = System.currentTimeMillis();
			
			// if time exceeds timeout duration
			if (threadDuration > 0 && time >= (start + threadDuration))
//			if (threadDuration > 0 && timer.getTime() >= startTime.getTime() + threadDuration)
			{
				setTimeToDie(true);
			}
			//if interval time is >= to the timer's time, send a pdu and reset timer
			else if (time >= (last + interval))
//			else if (timer.getTime() >= (pdusInterval + timerBase.getTime()))
			{
				//send pdu
		    	pdu.makeTimestampCurrent();
				PduProcessor.getInstance().sendPDU(pdu);

				// TODO send this to log or sysout
				//System.out.println("EntityState PDU Sent at " + (timer.getTime() - startTime.getTime()) + " milliseconds.");
				//System.out.println("EntityState PDU Sent at " + (time - start) + " milliseconds.");
				
				// reset timer to current time
//				timerBase.setTime(new Date().getTime());
				last = time;
			}
			else
			{
				try
				{
//					Thread.sleep((pduInterval + timerBase.getTime()) - timer.getTime());
					Thread.sleep(interval);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		// send shutoff PDU, if necessary
		if (terminalPdu != null)
		{
	    	terminalPdu.makeTimestampCurrent();
			PduProcessor.getInstance().sendPDU(terminalPdu);
		}
	}

	public void setTimeToDie(boolean b)
	{
		endThread = b;
	}
}