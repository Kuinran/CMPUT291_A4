//import com.sleepycat.je.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	static Main_States state;
	static Formats format;
	static String currLine;
	static Scanner scanner;
	static Pattern pattern;
	static List<Expression> expressions;
	private class Expression {
		public Query_Type type;
		public String op;
		public String arg;
		Expression(Query_Type type, String op, String arg) {
			this.type = type;
			this.op = op;
			this.arg = arg;
		}
	}
	
	public static void main(String[] args) {
		// initialize indicies and stuff
		scanner = new Scanner(System.in);
		state = Main_States.INPUT;
		format = Formats.BRIEF;
		expressions = new ArrayList<Expression>();
		pattern = Pattern.compile(createRegex());
		
		System.out.println("Welcome!");
		// start main runtime loop
		while(state != Main_States.QUIT) {
			switch(state) {
			case PARSING:
				// resets expressions
				expressions = new ArrayList<Expression>();
				// breaks down input line and saves each expression into a list
				processParse();
				break;
			case QUERY:
				// for each expression sends a query and then intersects the result
				processQuery();
				break;
			case PRINT:
				// outputs results
				processPrint();
				break;
			default:
				break;
			}
		}
		scanner.close();
		// close database?
	}
	
	private static void processParse() {
		String input = scanner.nextLine().toLowerCase();
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			if (matcher.group(1) != null) { // date match
				expressions.add(new Main().new Expression(Query_Type.DATE, matcher.group(2), matcher.group(3)));
			} else if (matcher.group(5) != null) { // price match
				expressions.add(new Main().new Expression(Query_Type.PRICE, matcher.group(5), matcher.group(6)));
			} else if (matcher.group(9) != null) { // location match
				expressions.add(new Main().new Expression(Query_Type.LOCATION, "=", matcher.group(8)));
			} else if (matcher.group(12) != null) { // cat match
				expressions.add(new Main().new Expression(Query_Type.CAT, "=", matcher.group(10)));
			} else { // term match
				if (matcher.group().endsWith("%")) { // partial match
					expressions.add(new Main().new Expression(Query_Type.TERM, "NA", matcher.group(12)));
				} else { // full match
					expressions.add(new Main().new Expression(Query_Type.PARTTERM,"NA", matcher.group(12)));
				}
			}
		}
	}
	
	private static void processQuery() {
		
	}
	
	private static void processPrint() {
		
	}
	
	private static String createRegex() {
		String alphaNumeric = "[0-9a-zA-Z_-]";
		String numeric = "[0-9]";
		String operator = "(=|>|<|>=|<=)";
		String date = "(" + numeric + numeric + numeric + numeric + "\\/" + numeric + numeric + "\\/" + numeric + numeric + ")";
		String datePrefix = "date\\s*" + operator;
		String dateQuery = datePrefix + "\\s*" + date;
		String price = "(" + numeric + "+)";
		String pricePrefix = "price\\s*" + operator;
		String priceQuery = pricePrefix + "\\s*" + price;
		String location = "(" + alphaNumeric + "+)";
		String locationPrefix = "location\\s*=";
		String locationQuery = locationPrefix + "\\s*" + location;
		String cat = "(" + alphaNumeric + "+)";
		String catPrefix = "cat\\s*=";
		String catQuery = catPrefix + "\\s*" + cat;
		String term = "(" + alphaNumeric + "+)";
		String termQuery = term + "%?";
		return  "(" + dateQuery + ")|(" + priceQuery + ")|(" + locationQuery + ")|(" + catQuery + ")|(" + termQuery + ")";
	}
}
