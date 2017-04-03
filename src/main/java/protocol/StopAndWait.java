package protocol;
import java.util.*;

import simulator.Message;
import simulator.NetworkSimulator;
import simulator.Packet;

import java.io.*;

public class StopAndWait extends NetworkSimulator
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

    public static final int FirstSeqNo = 0;
    public static final int SEQ_NUM_MAX_SIZE = 2;
    
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    
    private static int sendSeqNum_A = FirstSeqNo;
    private Queue<Packet> unsentPacketQueue_A = new LinkedList<Packet>();
    private Packet unackedPacket_A = null;
    private boolean timerSet_A = false;
    
    private static int recvSeqNum_B = FirstSeqNo;
    private int ackNum_B = -1;
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // This is the constructor.  Don't touch!
    public StopAndWait(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
		WindowSize = winsize;
		LimitSeqNo = 2*winsize;
		RxmtInterval = delay;
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
    	
    	if (unackedPacket_A == null && unsentPacketQueue_A.size() == 1) {
    		sendNextIfExists();;
    	}
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
			&& unackedPacket_A != null 
			&& packet.getAcknum() == unackedPacket_A.getSeqnum()
		) {
			unackedPacket_A = null;
			sendNextIfExists();
		} else {
			transmitUnacked();
		}
    }
    
    private void sendNextIfExists() {
		// send head of packet queue if exists
		if (unackedPacket_A == null && unsentPacketQueue_A.peek() != null) {
			unackedPacket_A = unsentPacketQueue_A.remove();
			transmitUnacked();
		}
    }
    
    private void transmitUnacked() {
    	if (unackedPacket_A != null) {
	    	toLayer3(A, unackedPacket_A);
	    	if (timerSet_A) { stopTimer(A); }
			startTimer(A, RxmtInterval);
			timerSet_A = true;
    	}
    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	timerSet_A = false;
		transmitUnacked();
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
    	if (
			packet.getChecksum() == calculateChecksum(packet) 
			&& packet.getSeqnum() == recvSeqNum_B
    	) {
    		toLayer5(packet.getPayload());
    		recvSeqNum_B = getNextSequenceNumber(recvSeqNum_B);
    		ackNum_B = packet.getSeqnum();
    	}
    	
		Packet p = newAckPacket(ackNum_B);
		
		toLayer3(B, p);
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
    
    static int getNextSequenceNumber(int sequenceNumber) {
    	return (sequenceNumber + 1) % SEQ_NUM_MAX_SIZE;
    }
}
