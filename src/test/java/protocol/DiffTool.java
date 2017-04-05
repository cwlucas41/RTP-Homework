package protocol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.*;

public class DiffTool {
	
	public static void diff() {
        int i = 0;
        
        String s1 = null, s2 = null;
        
        try {
        	s1 = new String(Files.readAllBytes(Paths.get("InputFile")));
			s2 = new String(Files.readAllBytes(Paths.get("OutputFile")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        Scanner sc1 = new Scanner(s1);
        Scanner sc2 = new Scanner(s2);
        
        while (sc1.hasNext() && sc2.hasNext()) {
        	i++;
        	if (!sc1.next().equals(sc2.next())) {
        		print(s1, s2);
        		fail("data does not match");
        	}
        }
        
        if (i < 20) {
//        	print(s1, s2);
        	fail("too few delivered only " + i + " were delivered");
        }
        
        sc1.close();
        sc2.close();
	}
	
	public static void print(String input, String output) {
		System.out.println("---------- Input ----------");
		System.out.println(input);
		System.out.println("---------- Output ----------");
		System.out.println(output);
	}
}
