import com.sleepycat.db.*;

public class Main {
	
	public static void Main() {
		Database all[] = null;
		try {
			all = OpenDB.Open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
