package protocol;

import org.junit.Test;

public class SelectiveRepeatTest {

	int numMessages = 1000;
    double avgDelay = 10;
    int trace = 0;
    int seed = 2233;
    int winsize = 8;
    double delay = 20;
	
	@Test
	public void noCorruptionNoLoss() {
		
		System.out.println("\nnoCorruptionNoLoss");
		
        double loss = 0;
        double corrupt = 0;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void CorruptionNoLoss() {

		System.out.println("\nCorruptionNoLoss");
		
        double loss = 0;
        double corrupt = .15;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void NoCorruptionLoss() {

		System.out.println("\nNoCorruptionLoss");
		
        double loss = .1;
        double corrupt = 0;
        
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
	
	@Test
	public void CorruptionLoss() {

		System.out.println("\nCorruptionLoss");
		
	    double loss = .1;
	    double corrupt = .15;
       
		new SelectiveRepeat(numMessages, loss, corrupt, avgDelay, trace, seed, winsize, delay)
		.runSimulator();
		
		DiffTool.diff();
	}
}
