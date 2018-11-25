import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepycat.db.*;

public class Driver {
	private enum Formats {BRIEF, FULL};
	private enum Main_States {PARSING, QUERY, PRINT, QUIT};
	private enum Query_Type {LOCATION, PRICE, CAT, DATE, TERM, PARTTERM};
	Main_States state;
	Formats format;
	String currLine;
	Scanner scanner;
	Pattern pattern;
	List<Expression> expressions;
	Set<HashMap<String, String>> results;
	Database[] dbs;
	
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
	
	Driver(Scanner scanner) {
		// initialize indicies and stuff
		this.state = Main_States.PARSING;
		this.format = Formats.BRIEF;
		this.scanner = scanner;
		this.dbs = OpenDB.Open();
		expressions = new ArrayList<Expression>();
		pattern = Pattern.compile(createRegex());
	}
	
	public void run() {
		System.out.println("Welcome!");
		// start main runtime loop
		while(state != Main_States.QUIT) {
			switch(state) {
			case PARSING:
				// resets expressions
				expressions = new ArrayList<Expression>();
				// breaks down input line and saves each expression into a list
				if (processParse()) {
					state = Main_States.QUERY;
				} 
				break;
			case QUERY:
				// for each expression sends a query and then intersects the result
				processQuery();
				state = Main_States.PRINT;
				break;
			case PRINT:
				// outputs results
				processPrint();
				state = Main_States.PARSING;
				break;
			default:
				break;
			}
		}
		for (Database db : dbs) {
			OpenDB.Close(db);
		}
	}
	
	
	private boolean processParse() {
		System.out.println("Enter your query or type <Quit> to exit the program");
		String input = scanner.nextLine().toLowerCase();
		if (input.compareTo("quit") == 0) {
			state = Main_States.QUIT;
			return false;
		}
		Pattern outType = Pattern.compile("output=(brief|full)");
		Matcher matcher = outType.matcher(input);
		if (matcher.matches()) {
			if (matcher.group(1).compareTo("brief") == 0) {
				this.format = Formats.BRIEF;
				System.out.println("Brief mode");
			} else {
				this.format = Formats.FULL;
				System.out.println("Full mode");
			}
			state = Main_States.PARSING;
			return false;
		} else {
			matcher = pattern.matcher(input);
			while (matcher.find()) {
				if (matcher.group(1) != null) { // date match
					expressions.add(this.new Expression(Query_Type.DATE, matcher.group(2), matcher.group(3)));
				} else if (matcher.group(4) != null) { // price match
					expressions.add(this.new Expression(Query_Type.PRICE, matcher.group(5), matcher.group(6)));
				} else if (matcher.group(7) != null) { // location match
					expressions.add(this.new Expression(Query_Type.LOCATION, "=", matcher.group(8)));
				} else if (matcher.group(9) != null) { // cat match
					expressions.add(this.new Expression(Query_Type.CAT, "=", matcher.group(10)));
				} else { // term match
					if (matcher.group().endsWith("%") && matcher.group(12).length()>2) { // partial match
						expressions.add(this.new Expression(Query_Type.PARTTERM, "NA", matcher.group(12)));
					} else { // full match
						if (matcher.group(12).length()>2) {
							expressions.add(this.new Expression(Query_Type.TERM,"NA", matcher.group(12)));
						}
					}
				}
			}
			return true;
		}
	}
	
	private void processQuery() {
		System.out.println("\nSearching for results...\n");
		results = new HashSet<HashMap<String, String>>();
		Set<HashMap<String, String>> temp = new HashSet<HashMap<String, String>>();
		for (Expression expression : expressions) {
			// uses a function to search db then saves results in a set
			switch(expression.type) {
			case PRICE:
				//System.out.println("Sending price query " + expression.arg);
				if (format == Formats.FULL) {
					temp = HandleQuerry.getPrice(expression.op, Integer.parseInt(expression.arg), dbs, true);
					//System.out.println(temp.toString() + temp.isEmpty());
				} else {
					temp = HandleQuerry.getPrice(expression.op, Integer.parseInt(expression.arg), dbs, false);
					//System.out.println(temp.toString() + temp.isEmpty());
				}
				break;
			case LOCATION:
				//System.out.println("Sending location query " + expression.arg);
				if (format == Formats.FULL) {
					temp = HandleQuerry.getLocation(expression.arg, dbs, true);
				} else {
					temp = HandleQuerry.getLocation(expression.arg, dbs, false);
				}
				break;
			case DATE:
				//System.out.println("Sending date query " + expression.arg);
				if (format == Formats.FULL) {

				} else {
					
				}
				break;
			case CAT:
				//System.out.println("Sending cat query " + expression.arg);
				if (format == Formats.FULL) {
					temp = HandleQuerry.getCategory(expression.arg, dbs, true);
				} else {
					temp = HandleQuerry.getCategory(expression.arg, dbs, false);
				}
				break;
			case TERM:
				//System.out.println("Sending term query " + expression.arg);
				if (format == Formats.FULL) {
					temp = HandleQuerry.getTerm(expression.arg, dbs, true, false);
				} else {
					temp = HandleQuerry.getTerm(expression.arg, dbs, false, false);
				}
				break;
			case PARTTERM:
				//System.out.println("Sending partial term query " + expression.arg);
				if (format == Formats.FULL) {
					temp = HandleQuerry.getTerm(expression.arg, dbs, true, true);
				} else {
					temp = HandleQuerry.getTerm(expression.arg, dbs, false, true);
				}
				break;
			}
			if (results.isEmpty()) { // if no results results are what were returned
				results = temp;
			} else { // else find the intersection of results
				results.retainAll(temp);
			}
		}
	}
	
	private void processPrint() {
		if (results.isEmpty()) {
			System.out.println("No results were found");
			System.out.println();
		} else {
			System.out.println("Printing Results...");
			if (format == Formats.BRIEF) { // brief mode
				System.out.println(String.format("%1$-13s|%2$-30s", "Ad ID", "Title"));
				for (Map<String, String> result : results) {
					System.out.println(String.format("%1$-13s|%2$-30s", result.get("aid"), result.get("title")));
				}
				System.out.println();
			} else { // full mode
				String str = "%1$-13s|%2$-10s|%3$-10s|%4$-50s|%5$-6s|%6$-25s|%7$s";
				System.out.println(String.format(str, "Ad ID", "Date", "Location", "Title", "Price", "Category", "Description"));
				for (Map<String, String> result : results) {
					System.out.println(String.format(str, result.get("aid"), result.get("date"), result.get("loc"), result.get("title"),
							result.get("price"), result.get("cat"), result.get("desc")));
				}
				System.out.println();
			}
		}
	}
	
	private String createRegex() {
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

