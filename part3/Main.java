//import com.sleepycat.je.*;

import java.util.Scanner;

public class Main {
	static Scanner scanner;
	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		Driver driver = new Driver(scanner);
		driver.run();
		scanner.close();
		// close database?
	}
}
