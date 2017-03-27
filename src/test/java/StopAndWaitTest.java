import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        double delay = 0;
        
		new StopAndWaitSimulator(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
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
        double delay = 0;
        
		new StopAndWaitSimulator(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		assertTrue(DiffTool.diff());
	}
	
	@Test
	public void rdt3_0Test() {
		
		int numMessages = 100;
        double loss = .25;
        double corrupt = .25;
        double avgDelay = 100;
        int trace = 0;
        int seed = 1;
        int winsize = 0;
        double delay = 0;
        
		new StopAndWaitSimulator(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		assertTrue(DiffTool.diff());
	}
	
	@Test
	public void seqNumIncrementTestA() {
		assertEquals(StopAndWaitSimulator.getNextSequenceNumber(0), 1);
		assertEquals(StopAndWaitSimulator.getNextSequenceNumber(1), 0);
	}
}
