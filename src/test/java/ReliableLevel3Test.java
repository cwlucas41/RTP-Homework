import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

public class ReliableLevel3Test {

	@Test
	public void test() {
		NetworkSimulator simulator = new StopAndWaitSimulator(10, 0, 0, 1000,
                0, 1, 8, 15);
                
        simulator.runSimulator();
        assertTrue(DiffTool.diff());
	}
}
