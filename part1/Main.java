import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	
	static List<String> Alines = new ArrayList<>();
	static List<String> Dlines = new ArrayList<>();
	static List<String> Tlines = new ArrayList<>();
	static List<String> PRlines = new ArrayList<>();
	
	public static void main(String[] args) {
		
		//check file input
		if (args.length < 1) {
			System.out.println("input file not detected");
			System.exit(1);
		}
		Path tfile = Paths.get("terms.txt");
		Path dfile = Paths.get("pdates.txt");
		Path pfile = Paths.get("prices.txt");
		Path afile = Paths.get("ads.txt");
		
		try {
			Files.deleteIfExists(tfile);
			Files.deleteIfExists(dfile);
			Files.deleteIfExists(pfile);
			Files.deleteIfExists(afile);
			
			File file = new File(args[0]);
			Scanner fp = new Scanner(file);
			
			while(fp.hasNextLine()) {
				String line = fp.nextLine();
				parseLine(line);
			}
			fp.close();
			
			Files.write(dfile, Dlines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			Files.write(pfile, PRlines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			Files.write(tfile, Tlines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			Files.write(afile, Alines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void parseLine(String line) {
		String price = null;
		//if this doesnt work, use regex indices
		String Oad = "<ad>";
		String Cad = "</ad>";
		
		Matcher m = Pattern.compile(Pattern.quote(Oad) + "(.*?)" +  Pattern.quote(Cad)).matcher(line);
		//Matcher m = p.matcher(line);
		if (!m.find()) {
			return;
		}
		String ad = m.group(1);
		
		// find aid
		String Oaid = "<aid>";
		String Caid = "</aid>";
		Matcher Maid = Pattern.compile(Pattern.quote(Oaid) + "(.*?)" + Pattern.quote(Caid)).matcher(line);
	
		if (!Maid.find()) {
			return;
		}
		
		String aid = Maid.group(1);
		
		// find date
		String Odate = "<date>";
		String Cdate = "</date>";
		Matcher Mdate = Pattern.compile(Pattern.quote(Odate) + "(.*?)" + Pattern.quote(Cdate)).matcher(ad);
		
		if (!Mdate.find()) {
			return;
		}
		
		String date = Mdate.group(1);
		
		// find location
		String Oloc = "<loc>";
		String Cloc = "</loc>";
		Matcher Mloc = Pattern.compile(Pattern.quote(Oloc) + "(.*?)" + Pattern.quote(Cloc)).matcher(ad);
		
		if (!Mloc.find()) {
			return;
		}
		
		String loc = Mloc.group(1);
		
		// find category
		String Ocat = "<cat>";
		String Ccat = "</cat>";
		Matcher Mcat = Pattern.compile(Pattern.quote(Ocat) + "(.*?)" + Pattern.quote(Ccat)).matcher(ad);
		
		if (!Mcat.find()) {
			return;
		}
		
		String cat = Mcat.group(1);
		
		//find description
		String Odesc = "<desc>";
		String Cdesc = "</desc>";
		Matcher Mdesc = Pattern.compile(Pattern.quote(Odesc) + "(.*?)" + Pattern.quote(Cdesc)).matcher(ad);
		
		if (!Mdesc.find()) {
			return;
		}
		
		String desc = Mdesc.group(1);
		
		//find title
		String Oti = "<ti>";
		String Cti = "</ti>";
		Matcher Mtitle = Pattern.compile(Pattern.quote(Oti) + "(.*?)" + Pattern.quote(Cti)).matcher(ad);
		
		if (!Mtitle.find()) {
			return;
		}
		
		String title = Mtitle.group(1);
		
		//find price
		String Oprice = "<price>";
		String Cprice = "</price>";
		Matcher Mprice = Pattern.compile(Pattern.quote(Oprice) + "(.*?)" + Pattern.quote(Cprice)).matcher(ad);
		
		if (Mprice.find()) {
			price = Mprice.group(1);
		} 
		
		
		try {
			terms(title, desc, aid);
			pdates(date, cat, loc, aid);
			prices(price, cat, loc, aid);
			ads(aid, line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void terms(String title, String desc, String aid) throws IOException {
		//CHECK FOR SPICIFIC REGEX
		//template for creating file or find another on stackoverflow
		List<String> terms = new ArrayList<>();
		
		String s1 = "&#";
		String s2 = ";";
		String s3 = "(" + Pattern.quote(s1) + "(.*?)" + Pattern.quote(s2) + ")";
		String p1 = "&";
		String p2 = ";";
		String a = "apos";
		String q = "quot";
		String am = "amp";
		
		String Pa = "(" + Pattern.quote(p1) + Pattern.quote(a) + Pattern.quote(p2) + ")";
		String Pq = "(" + Pattern.quote(p1) + Pattern.quote(q) + Pattern.quote(p2) + ")";
		String Pam = "(" + Pattern.quote(p1) +Pattern.quote(am)+ Pattern.quote(p2) + ")";
		String PE = "(" + Pattern.quote(p1) +Pattern.quote(am + ";" + q)+ Pattern.quote(p2) + ")";
		
		
		title = title.replaceAll(PE, " ").replaceAll(Pq, " ").replaceAll(Pa, " ").replaceAll(Pam, " ");
		String words[] = title.trim().replaceAll(s3, "").replaceAll("[^A-Za-z0-9_-]" , " ").split("\\s+");
			for (String w : words) {
				if (w.length() > 2) {
						terms.add(w.toLowerCase());
				}
			}
			
		desc = desc.replaceAll(PE, " ").replaceAll(Pq, " ").replaceAll(Pa, " ").replaceAll(Pam, " ");
		String dwords[] = desc.trim().replaceAll(s3, "").replaceAll("[^A-Za-z0-9_-]" , " ").split("\\s+");
			for (String w : dwords) {
				if (w.length() > 2) {
					terms.add(w.toLowerCase());
				}
			}
			
		for (String s : terms) {
			Tlines.add(s + ":" + aid);
			}
		
		}
	
	private static void pdates(String date, String cat, String loc, String aid) throws IOException {
		
		Dlines.add(date + ":" + aid + "," + cat + "," + loc);
		
	}
	
	private static void prices(String price, String cat, String loc, String aid) throws IOException {
		if(price == null) {
			return;
		}
		int j = 12 - price.length();
		String pad = "";
		for(int i = 0; i < j; i++) {
			pad = pad + " ";
		}
		//
		//TODO check this for non empty price before price is passed to this function
		
		PRlines.add(pad + price + ":" + aid + "," + cat + "," + loc);
		}
	
	private static void ads(String aid, String line) throws IOException {		
		Alines.add(aid + ":" + line);
		
	}
}
