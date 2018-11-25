import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Driver {
	Main_States state;
	Formats format;
	String currLine;
	Scanner scanner;
	Pattern pattern;
	List<Expression> expressions;
	Set<Map<String, String>> result;
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
		state = Main_States.PARSING;
		format = Formats.BRIEF;
		this.scanner = scanner;
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
				processParse();
				state = Main_States.QUERY;
				break;
			case QUERY:
				// for each expression sends a query and then intersects the result
				processQuery();
				state = Main_States.PARSING;
				break;
			case PRINT:
				// outputs results
				processPrint();
				break;
			default:
				break;
			}
		}
	}
	
	
	private void processParse() {
		String input = scanner.nextLine().toLowerCase();
		Matcher matcher = pattern.matcher(input);
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
				if (matcher.group().endsWith("%")) { // partial match
					expressions.add(this.new Expression(Query_Type.PARTTERM, "NA", matcher.group(12)));
				} else { // full match
					expressions.add(this.new Expression(Query_Type.TERM,"NA", matcher.group(12)));
				}
			}
		}
	}
	
	private void processQuery() {
		result = new HashSet<Map<String, String>>();
		Set<Map<String, String>> temp = new HashSet<Map<String, String>>();
		for (Expression expression : expressions) {
			// uses a function to search db then saves results in a set
			switch(expression.type) {
			case PRICE:
				//System.out.println("Sending price query " + expression.op);
				break;
			case LOCATION:
				//System.out.println("Sending location query " + expression.op);
				break;
			case DATE:
				//System.out.println("Sending date query " + expression.op);
				break;
			case CAT:
				//System.out.println("Sending cat query " + expression.op);
				break;
			case TERM:
				//System.out.println("Sending term query " + expression.op);
				break;
			case PARTTERM:
				//System.out.println("Sending partial term query " + expression.op);
				break;
			}
			if (result.isEmpty()) { // if no results results are what were returned
				result = temp;
			} else { // else find the intersection of results
				result.retainAll(temp);
			}
		}
	}
	
	private static void processPrint() {
		
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

