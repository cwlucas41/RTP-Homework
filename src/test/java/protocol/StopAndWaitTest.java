package protocol;

import org.junit.Test;

public class StopAndWaitTest {

	int numMessages = 1000;
    double avgDelay = 10;
    int trace = 0;
    int seed = 2233;
    int winsize = 1;
    double delay = 20;
	
	@Test
	public void rdt1_0Test() {
		
        double loss = 0;
        double corrupt = 0;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void rdt2_2Test() {
		
        double loss = 0;
        double corrupt = .2;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void rdt3_0Test() {
		
	    double loss = .2;
	    double corrupt = .2;
       
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
}
