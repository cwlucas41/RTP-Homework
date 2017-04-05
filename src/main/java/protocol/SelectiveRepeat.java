package protocol;
import java.util.*;

import simulator.Message;
import simulator.NetworkSimulator;
import simulator.Packet;

public class SelectiveRepeat extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    public final int FirstSeqNo = 0;
    
    private  int windowSize;
    private double rxmtInterval;
    private int limitSeqNo;
    
    private int sendSeqNum_A = FirstSeqNo;
    private Queue<Packet> unsentPacketQueue_A = new LinkedList<Packet>();
    private SendWindow sendWindow = new SendWindow();
    private Timer timer_a = new Timer();
    
    private int recvSeqNum_B = FirstSeqNo;
    private int ackNum_B = -1;
    private ReceiveWindow receiveWindow = new ReceiveWindow();
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // This is the constructor.  Don't touch!
    public SelectiveRepeat(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
		windowSize = winsize;
		limitSeqNo = 2*winsize;
		rxmtInterval = delay;
    }
    
    boolean first = true;

    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	Packet p = newDataPacket(sendSeqNum_A, message.getData());
    	unsentPacketQueue_A.add(p);
    	
    	sendSeqNum_A = getNextSequenceNumber(sendSeqNum_A);
    	    	
		fillWindow();
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
		// if valid ack that matched packet queue head's seq num, remove head
    	
		if (
			packet != null
			&& packet.getChecksum() == calculateChecksum(packet) 
		) {
			if (packet.getAcknum() == -1) {
				retransmitOldestUnacked();
			} else {
				System.out.println("rcvd ack #" + packet.getAcknum());
				
				int before = sendWindow.size();
				sendWindow.markAsAcked(packet.getAcknum());
				
				// if none acked, resend oldest
				if ((before - sendWindow.size()) == 0 ) {
					retransmitOldestUnacked();
				}
			}
		} 
		
		fillWindow();
    }
    
    private void fillWindow() {
		// send head of packet queue if exists
    	while (!sendWindow.isFull()) {
    		if (unsentPacketQueue_A.peek() != null) {
    			Packet p = unsentPacketQueue_A.remove();
    			
    			System.out.println("A sent new: " + p.getPayload() + " #" + p.getSeqnum());
    			
    			sendWindow.addUnackedPacket(p);
    			toLayer3(A, p);
    		} else {
    			break;
    		}
    	}
    }
    
    private void retransmitOldestUnacked() {
    	Packet p = sendWindow.getOldestPacket();
    	if (p != null) {
    		System.out.println("A sent again: " + p.getPayload() + " #" + p.getSeqnum());
    		
	    	toLayer3(A, p);
	    	timer_a.reset();
    	}
    }
    
    private class Timer {
    	
    	boolean timerIsSet = false;
    	
    	public void reset() {
    		stop();
    		start();
    	}
    	
    	public void stop() {
    		if (timerIsSet) { stopTimer(A); }
    		timerIsSet = false;
    	}
    	
    	public void start() {
    		startTimer(A, rxmtInterval);
    		timerIsSet = true;
    	}
    	
    }
    
    private class SendWindow {
    	private List<Packet> unackedPackets = new LinkedList<Packet>();
    	
    	
    	public boolean isFull() {
    		return !(unackedPackets.size() < windowSize);
    	}
    	
    	public int size() {
    		return unackedPackets.size();
    	}
    	
    	public void addUnackedPacket(Packet p) {

    		if (!isFull()) {
    			// add p if it has correct sequence number
        		int size = unackedPackets.size();
        		if (size > 0) {
	        		Packet last = unackedPackets.get(size - 1);
	        		System.out.println("ADDING : " + last);
	        		int nextSeqNumInWindow = getNextSequenceNumber(last.getSeqnum());
	        		if (p.getSeqnum() != nextSeqNumInWindow) {
	        			throw new IllegalArgumentException("Out of order sequence number");
	        		}
        		} else {
        			timer_a.reset();
        		}
        		
    			unackedPackets.add(p);
    		} else {
    			throw new IllegalStateException("Window is full");
    		}
    	}
    	
    	public void markAsAcked(int seq) {
    		int startSize = unackedPackets.size();
    		
    		int ackIdx = -1;
    		
    		for (int i = 0; i < unackedPackets.size(); i++) {
				Packet p = unackedPackets.get(i);
				if (p.getSeqnum() == seq) {
					ackIdx = i;
					break;
				}
				
			}
    		
    		for (int i = 0; i <= ackIdx; i++) {
    			System.out.println("REMOVING : " + unackedPackets.get(0));
    			unackedPackets.remove(0);
    		}
    		
    		int endSize = unackedPackets.size();
    		
    		if (endSize == 0) {
    			timer_a.stop();
    		} else if ((startSize - endSize) > 0) {
    			timer_a.reset();
    		}
    	}
    	
    	public Packet getOldestPacket() {
    		if (unackedPackets.size() > 0) {
    			return unackedPackets.get(0);
    		} else {
    			return null;
    		}
    	}
    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
		retransmitOldestUnacked();
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {

    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	System.out.println("B rcvd " + packet.getPayload() + " #" + packet.getSeqnum());
    	if (
			packet.getChecksum() == calculateChecksum(packet) 
    	) {
    		receiveWindow.addPacket(packet);
    	}
    	
    	sendAck(receiveWindow.getLastAckNumber());
    	
    	
    }
    
    private void sendAck(int ackNum) {
    	Packet p = newAckPacket(ackNum);
		System.out.println("B sent ack #" + p.getAcknum());
		toLayer3(B, p);
    }
    
    private class ReceiveWindow {
    	
    	
    	private int lastAckNumber = -1;
    	private int nextDeliveredNumber = 0;
    	private Map<Integer, Packet> map = new HashMap<Integer, Packet>();
    	
    	public int getLastAckNumber() {
    		return lastAckNumber;
    	}
    	
    	public void addPacket(Packet p) {
    		
    		if (p.getSeqnum() == getNextSequenceNumber(lastAckNumber)) {
    			lastAckNumber = getNextSequenceNumber(lastAckNumber);
    		}
   
    		// add packet to map if its seq num is in current window
    		int n = nextDeliveredNumber;
    		for (int i = 0; i < windowSize; i++) {
    			if (p.getSeqnum() == n) {
    				map.put(p.getSeqnum(), p);
    				break;
    			}
    			n = getNextSequenceNumber(n);
    		}
    		
    		deliver();
    	}
    	
    	private void deliver() {
    		if (map.containsKey(nextDeliveredNumber)) {
    			toLayer5(map.get(nextDeliveredNumber).getPayload());
    			map.remove(nextDeliveredNumber);
    			nextDeliveredNumber = getNextSequenceNumber(nextDeliveredNumber);
    			
    			deliver();
    		}
    	} 	
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {

    }

    // Use to print final statistics
    protected void Simulation_done()
    {
    	System.out.println("finished \n\n\n\n\n");
    }
        
    private Packet newDataPacket(int seq, String newPayload) {
    	Packet p = new Packet(seq, -1, 0, newPayload);
    	p.setChecksum(calculateChecksum(p));
    	return p;
    }
    
    private Packet newAckPacket(int ack) {
    	Packet p = new Packet(-1, ack, 0);
    	p.setChecksum(calculateChecksum(p));
    	return p;
    }
    
    private int calculateChecksum(Packet p) {
    	return p.getSeqnum() + p.getAcknum() + p.getPayload().chars().sum();
    }
    
    int getNextSequenceNumber(int sequenceNumber) {
    	return (sequenceNumber + 1) % limitSeqNo;
    }
}
