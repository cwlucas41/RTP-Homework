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

	// shared variables
    public final static int FirstSeqNo = 0;
    private  int windowSize;
    private double rxmtInterval;
    private static int limitSeqNo;
    
    
    // A variables
    private static int sendSeqNum_A = FirstSeqNo;
    private int lastReceivedAckNum = -1;
    private int duplicateAckCounter = 0;
    private Queue<Packet> unsentPacketQueue_A = new LinkedList<Packet>();
    private Timer timer_a = new Timer();
    private SendWindow sendWindow;
    private int corruptAckCounter = 0;
    private int retransmitCounter = 0;
    private int transmitDataCounter = 0;
    private int interruptCounter = 0;
    
    
    // B variables
    private ReceiveWindow receiveWindow;
    private int transmitAckCounter = 0;
    private int corruptDataCounter = 0;
    private int deliveredCounter = 0;
    
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
    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	// add packets to unsent queue
    	Packet p = new AutoPacket(sendSeqNum_A, message.getData());
    	unsentPacketQueue_A.add(p);
    	sendSeqNum_A = getNextSequenceNumber(sendSeqNum_A);
    	
    	// makes sure new packets get to window if not full
    	sendToFillWindow();
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
		// if valid ack that matched packet queue head's seq num, remove head
    	
    	System.out.println("A received: " + packet);
    	
		if (!AutoPacket.isCorrupt(packet)) {
			
			if (packet.getAcknum() == lastReceivedAckNum) {
				duplicateAckCounter++;
				
				// resend oldest packet in window if third ack
				if (duplicateAckCounter > 3) {
					retransmitOldestUnacked();
				}
			} else {
				// ack packets in window
				sendWindow.markAsAcked(packet.getAcknum());
				lastReceivedAckNum = packet.getAcknum();
				duplicateAckCounter = 1;
			}
		} else {
			corruptAckCounter++;
		}
		
		// send as many new packets as possible
		sendToFillWindow();
    }
    
    public void sendToFillWindow() {
    	List<Packet> toSend = sendWindow.fillWindow(unsentPacketQueue_A);
    	for (Packet p : toSend) {
    		System.out.println("A sending: " + p);
    		toLayer3(A, p);
    		transmitDataCounter++;	
    	}
    }
    
    public void retransmitOldestUnacked() {
    	Packet p = sendWindow.getOldestPacket();
    	if (p != null) {   
    		System.out.println("A resending: " + p);
	    	toLayer3(A, p);
	    	timer_a.restart();
	    	retransmitCounter++;
    	}
    }
      
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	interruptCounter++;
		retransmitOldestUnacked();
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
		sendSeqNum_A = FirstSeqNo;
		sendWindow = new SendWindow(timer_a, windowSize);
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	System.out.println("B received: " + packet);
    	
    	if (!AutoPacket.isCorrupt(packet)) {
    		List<Packet> deliverable = receiveWindow.getDeliverablePacketsAfterAck(packet);
    		for (Packet p : deliverable) {
    			deliveredCounter++;
    			toLayer5(p.getPayload());
    		}
    	} else {
    		corruptDataCounter++;
    	}
    	
    	Packet p = new AutoPacket(receiveWindow.getLastAckNumber());
    	System.out.println("B sending: " + p);
		toLayer3(B, p);
		transmitAckCounter++;
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	receiveWindow = new ReceiveWindow(windowSize);
    }

    // Use to print final statistics
    protected void Simulation_done()
    {
    	System.out.println();
    	System.out.println("---------- Statistics ----------");
    	System.out.println("sent data packets: " + transmitDataCounter);
    	System.out.println("corrupt data packets: " + corruptDataCounter);
    	System.out.println("resent data packets: " + retransmitCounter);
    	System.out.println("interrupts: " + interruptCounter);
    	System.out.println("total sent data packets: " + (transmitDataCounter + retransmitCounter));
    	System.out.println();
    	System.out.println("sent ack packets: " + transmitAckCounter);
    	System.out.println("corrupt ack packets: " + corruptAckCounter);
    	System.out.println();
    	System.out.println("delivered packets: " + deliveredCounter);
    }
    
    public static int getNextSequenceNumber(int sequenceNumber) {
    	return (sequenceNumber + 1) % limitSeqNo;
    }
    
    public class Timer {
    
    	public void restart() {
    		stop();
    		startTimer(A, rxmtInterval);
    	}
    	
    	public void stop() {
    		stopTimer(A);
    	}
    }
}
