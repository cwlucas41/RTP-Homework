package protocol;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import simulator.Packet;

public class ReceiveWindow {
	
	private int windowSize;
	
	public ReceiveWindow(int windowSize) {
		this.windowSize = windowSize;
	}
	
	private int lastAckNumber = -1;
	private int nextDeliveredNumber = 0;
	private Map<Integer, Packet> map = new HashMap<Integer, Packet>();
	
	public int getLastAckNumber() {
		return lastAckNumber;
	}
	
	public List<Packet> getDeliverablePacketsAfterAck(Packet ack) {
		if (ack.getSeqnum() == SelectiveRepeat.getNextSequenceNumber(lastAckNumber)) {
			lastAckNumber = SelectiveRepeat.getNextSequenceNumber(lastAckNumber);
		}

		// add packet to map if its seq num is in current window
		int n = nextDeliveredNumber;
		for (int i = 0; i < windowSize; i++) {
			if (ack.getSeqnum() == n) {
				map.put(ack.getSeqnum(), ack);
				break;
			}
			n = SelectiveRepeat.getNextSequenceNumber(n);
		}
		
		// calculate deliverable packets and return them
		List<Packet> deliverablePackets = new LinkedList<Packet>();
		deliverableHelper(deliverablePackets);
		return deliverablePackets;
	}
	
	private void deliverableHelper(List<Packet> deliverable) {
		if (map.containsKey(nextDeliveredNumber)) {
			deliverable.add(map.get(nextDeliveredNumber));
			map.remove(nextDeliveredNumber);
			nextDeliveredNumber = SelectiveRepeat.getNextSequenceNumber(nextDeliveredNumber);
			
			deliverableHelper(deliverable);
		}
	} 	
}
