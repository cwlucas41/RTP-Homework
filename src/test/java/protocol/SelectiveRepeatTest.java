package protocol;

import org.junit.Test;

public class SelectiveRepeatTest {

	@Test
	public void rdt1_0Test() {
		
		int numMessages = 1000;
        double loss = 0;
        double corrupt = 0;
        double avgDelay = 10;
        int trace = 0;
        int seed = 2233;
        int winsize = 8;
        double delay = 20;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void rdt2_2Test() {
		
		int numMessages = 1000;
        double loss = 0;
        double corrupt = .2;
        double avgDelay = 10;
        int trace = 0;
        int seed = 2233;
        int winsize = 8;
        double delay = 20;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void rdt3_0Test() {
		
		int numMessages = 1000;
	    double loss = .2;
	    double corrupt = .2;
	    double avgDelay = 10;
	    int trace = 0;
	    int seed = 2233;
	    int winsize = 8;
	    double delay = 20;
       
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
}
