package protocol;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import simulator.Packet;

public class SendWindow {
	private List<Packet> unackedPackets = new LinkedList<Packet>();
	private SelectiveRepeat.Timer timer_a;
	private int windowSize;
	
	public SendWindow(SelectiveRepeat.Timer t, int windowSize) {
		this.timer_a = t;
		this.windowSize = windowSize;
	}
	
	private boolean isFull() {
		return !(unackedPackets.size() < windowSize);
	}
	
	private void addUnackedPacket(Packet p) {

		if (!isFull()) {
			// add p if it has correct sequence number
    		int size = unackedPackets.size();
    		if (size > 0) {
        		Packet last = unackedPackets.get(size - 1);
        		int nextSeqNumInWindow = SelectiveRepeat.getNextSequenceNumber(last.getSeqnum());
        		if (p.getSeqnum() != nextSeqNumInWindow) {
        			throw new IllegalArgumentException("Out of order sequence number");
        		}
    		} else {
    			timer_a.restart();
    		}
    		
			unackedPackets.add(p);
		} else {
			throw new IllegalStateException("Window is full");
		}
	}
	
    public List<Packet> fillWindow(Queue<Packet> unsentQueue) {
    	List<Packet> newPackets = new LinkedList<Packet>();
    	
		// send head of packet queue if exists
    	while (!isFull()) {
    		if (unsentQueue.peek() != null) {
    			Packet p = unsentQueue.remove();
    			addUnackedPacket(p);
    			newPackets.add(p);
    		} else {
    			break;
    		}
    	}
    	
    	return newPackets;
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
			unackedPackets.remove(0);
		}
		
		int endSize = unackedPackets.size();
		
		if (endSize == 0) {
			timer_a.stop();
		} else if ((startSize - endSize) > 0) {
			timer_a.restart();
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
