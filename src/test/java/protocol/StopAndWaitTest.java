package protocol;
import static org.junit.Assert.*;

import org.junit.Test;

import protocol.StopAndWait;

public class StopAndWaitTest {

	@Test
	public void rdt1_0Test() {
		
		int numMessages = 100;
        double loss = 0;
        double corrupt = 0;
        double avgDelay = 100;
        int trace = 0;
        int seed = 1;
        int winsize = 0;
        double delay = 10;
        
		new StopAndWait(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		assertTrue(DiffTool.diff());
	}
	
	@Test
	public void rdt2_2Test() {
		
		int numMessages = 100;
        double loss = 0;
        double corrupt = .25;
        double avgDelay = 100;
        int trace = 0;
        int seed = 1;
        int winsize = 0;
        double delay = 10;
        
		new StopAndWait(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		assertTrue(DiffTool.diff());
	}
	
	@Test
	public void rdt3_0Test() {
		
		for (int i = 0; i < 100; i++) {
			int numMessages = 100;
	        double loss = .25;
	        double corrupt = .25;
	        double avgDelay = 100;
	        int trace = 0;
	        int seed = 1;
	        int winsize = 0;
	        double delay = 10;
	        
			new StopAndWait(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
			.runSimulator();
			
			assertTrue(DiffTool.diff());
		}
	}
	
	@Test
	public void seqNumIncrementTestA() {
		assertEquals(StopAndWait.getNextSequenceNumber(0), 1);
		assertEquals(StopAndWait.getNextSequenceNumber(1), 0);
	}
}
