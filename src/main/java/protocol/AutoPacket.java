package protocol;

import simulator.Packet;

public class AutoPacket extends Packet {
	
    public AutoPacket(int seq, String newPayload) {
    	super(seq, -1, 0, newPayload);
    	setChecksum(calculateChecksum(this));
    }
    
    public AutoPacket(int ack) {
    	super(-1, ack, 0);
    	setChecksum(calculateChecksum(this));
    }
	
    private static int calculateChecksum(Packet p) {
    	return p.getSeqnum() + p.getAcknum() + p.getPayload().chars().sum();
    }
    
    public static boolean isCorrupt(Packet p) {
    	return calculateChecksum(p) != p.getChecksum();
    }

}
