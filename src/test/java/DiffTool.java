import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class DiffTool {
	public static boolean diff() {
		
		byte[] f1 = null;
		byte[] f2 = null;
		
		try {
			f1 = Files.readAllBytes(Paths.get("InputFile"));
			f2 = Files.readAllBytes(Paths.get("OutputFile"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean same = Arrays.equals(f1, f2) && f1.length>0 && f2.length>0;
		
		if (!same) {
			System.out.println("----------In----------");
			System.out.print(new String(f1));
			
			System.out.println();
			
			System.out.println("----------Out----------");
			System.out.print(new String(f2));
		}
		
		return same;
	}
}
