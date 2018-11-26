import com.sleepycat.db.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.*;
public class HandleQuerry {
	
	//Dataaarray = {addData,dateData,PriceData,termsData}
	public static HashSet<HashMap<String,String>> getPrice(String op, int Price, Database[] dataArray,boolean full) {
		//System.out.println("reached function");
				Database pPrice = dataArray[2];
				HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
				ArrayList<String> aids = new ArrayList<String>();
				Cursor cursor = null;
				try {
					cursor = pPrice.openCursor(null, null);
					DatabaseEntry fKey = new DatabaseEntry(String.format("%1$12s", String.valueOf(Price)).getBytes("UTF-8"));
					DatabaseEntry fData = new DatabaseEntry();
					//System.out.println("Looking for hit");
					if (cursor.getSearchKeyRange(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) { // hit
						//System.out.println("Hit" + cursor.count());
						if (op.compareTo("=") == 0) {
							//System.out.println("Equals");
							// extract aid
							if (fData.getData() != Price) {
								return set;
							}
							String sData = new String(fData.getData(), "UTF-8");
							String aid = sData.split(",")[0];
							// add entry
							aids.add(aid);
							// while entries have same key
							//System.out.println("searching more entries");
							while (cursor.getNextDup(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
								//System.out.println("Looping");
								// extract aid
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
						} else if (op.compareTo(">=") == 0) {
							// extract aidx
							String sData = new String(fData.getData(), "UTF-8");
							String aid = sData.split(",")[0];
							// add entry
							aids.add(aid);
			        		// TODO: test to see if arg is greater than maximal value
							// while not at end
							while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
								// extract aid
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
						} else if (op.compareTo("<=") == 0) {
							String sData, aid;
			        		//System.out.println("Moving to next non-dupe");
							if (fData.getData() == Price) {
								// extract aid
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
								cursor.getNextNoDup(fKey, fData, LockMode.DEFAULT);
							}
							//System.out.println("Looking back");
							while (cursor.getPrevNoDup(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
								//System.out.println("Looping");
								// extract aid
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
						} else if (op.compareTo(">") == 0) {
							String sData, aid;
							if (fData.getData() == Price) {
								cursor.getNextNoDup(fKey, fData, LockMode.DEFAULT);
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
							while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
			        		// TODO: test to see if arg is greater than maximal value
						} else if (op.compareTo("<") == 0) {
							String sData, aid;

							while (cursor.getPrev(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
							}
						}
					} else { // misses check bounds
						// TODO: fix for bounds
						//System.out.println("Miss");
						//System.out.println("Hit" + cursor.count());
						if (op.compareTo(">") == 0) { // search up from first
							String sData, aid;
							// do search of first, if fkey greater than key search up, else don't do anything
							cursor.getFirst(fKey, fData, LockMode.DEFAULT);
							if (fKey.getData() > Price) {
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
				        		while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
									sData = new String(fData.getData(), "UTF-8");
									aid = sData.split(",")[0];
									// add entry
									aids.add(aid);
				        		}
							}
						} else if (op.compareTo("<") == 0) { // search down from top
							String sData, aid;
							// do search of last, if fkey less than key search down, else don't do anything
							cursor.getLast(fKey, fData, LockMode.DEFAULT);
							if (fKey.getData() < Price) {
								sData = new String(fData.getData(), "UTF-8");
								aid = sData.split(",")[0];
								// add entry
								aids.add(aid);
				        		while (cursor.getPrev(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
									sData = new String(fData.getData(), "UTF-8");
									aid = sData.split(",")[0];
									// add entry
									aids.add(aid);
				        		}
							}
						}
					}
					//System.out.println(aids.toString());
					getData(set, aids, dataArray, full);
				} catch (Exception e) {
					System.err.println("Error");
					e.printStackTrace();
				} finally {
					try {
						cursor.close();
					} catch (DatabaseException e) {
						System.err.println("Error");
						e.printStackTrace();
					}
				}
				 
				return set;
	}
	public static HashMap<String,String> getBrief(String aid, Database addData) {
		HashMap<String, String> map = new HashMap<>(); 
		map.put("aid", aid);
	
		//Now find the title from the add database
		    DatabaseEntry foundKey2 = new DatabaseEntry();
		    DatabaseEntry foundData2 = new DatabaseEntry();
			try {
				Cursor aidCursor = null;
				aidCursor = addData.openCursor(null, null);
				while (aidCursor.getNext(foundKey2, foundData2, LockMode.DEFAULT) ==
				OperationStatus.SUCCESS) {
					String keyAdd = new String(foundKey2.getData(), "UTF-8");
					String dataString2 = new String(foundData2.getData(), "UTF-8");
					if(aid.compareTo(keyAdd) == 0) {
							Pattern p = Pattern.compile(Pattern.quote("<ti>") + "(.*?)" + Pattern.quote("</ti>"));
							Matcher m = p.matcher(dataString2);
					if(m.find()) {
						map.put("title",m.group(1));
						aidCursor.close();
						return map;
					}
					else {
						System.out.println("If this message prints, failed to add the hashmap to the set. check regex");
					}
    		
					}
				}
			}
			catch(Exception e) {
			    System.err.println("Error accessing the database: " + e);
			}
			
		return null;
	}
	
	public static HashMap<String,String> getFull(String aid, Database addData){
		HashMap<String, String> map = new HashMap<>(); 
		map.put("aid", aid);
		
		//Now find the title and more shitfrom the add database
		    DatabaseEntry foundKey2 = new DatabaseEntry();
		    DatabaseEntry foundData2 = new DatabaseEntry();
			try {
				Cursor aidCursor = null;
				aidCursor = addData.openCursor(null, null);
				while (aidCursor.getNext(foundKey2, foundData2, LockMode.DEFAULT) ==
				OperationStatus.SUCCESS) {
					//System.out.println("Second stage, iterating through getFull");
					//each iteration the cursor points to, KEY:DATA
					String keyAdd = new String(foundKey2.getData(), "UTF-8");
					//System.out.println(keyAdd);
					String dataString2 = new String(foundData2.getData(), "UTF-8");
					if(aid.compareTo(keyAdd) == 0) {
						//System.out.println("aid match found");
							Pattern padd = Pattern.compile(Pattern.quote("<aid>") + "(.*?)" + Pattern.quote("</aid>"));
							Pattern pdate = Pattern.compile(Pattern.quote("<date>") + "(.*?)" + Pattern.quote("</date>"));
							Pattern ploc = Pattern.compile(Pattern.quote("<loc>") + "(.*?)" + Pattern.quote("</loc>"));
							Pattern pcat = Pattern.compile(Pattern.quote("<cat>") + "(.*?)" + Pattern.quote("</cat>"));
							Pattern pti = Pattern.compile(Pattern.quote("<ti>") + "(.*?)" + Pattern.quote("</ti>"));
							Pattern pdesc = Pattern.compile(Pattern.quote("<desc>") + "(.*?)" + Pattern.quote("</desc>"));
							Pattern pprice = Pattern.compile(Pattern.quote("<price>") + "(.*?)" + Pattern.quote("</price>"));
							
							Matcher madd = padd.matcher(dataString2);
							Matcher mdate = pdate.matcher(dataString2);
							Matcher mloc = ploc.matcher(dataString2);
							Matcher mcat = pcat.matcher(dataString2);
							Matcher mti = pti.matcher(dataString2);
							Matcher mdesc = pdesc.matcher(dataString2);
							Matcher mprice = pprice.matcher(dataString2);



							if(madd.find() && mdate.find() && mloc.find() && mcat.find() && mti.find() && mdesc.find() && mprice.find()) {
								map.put("date",mdate.group(1));
								map.put("loc",mloc.group(1));
								map.put("cat",mcat.group(1));
								map.put("title",mti.group(1));
								map.put("desc",mdesc.group(1));
								map.put("price",mprice.group(1));
								aidCursor.close();
								//System.out.println(map.toString());
								return map;
							}
							else {
								System.out.println("If this message prints, failed to add the hashmap to the set. check regex");
							}
					}
				}
			}
			catch(Exception e) {
			    System.err.println("Error accessing the database: " + e);
			}
			//System.out.println("We are about to return null");
		return map;
	}
	public static HashSet<HashMap<String,String>> getLocation(String arg,Database[] dataArray,boolean full) {
		Database ads = dataArray[0];
		HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
		Cursor cursor = null;
		Pattern pattern = Pattern.compile(String.format("<loc>%s<\\/loc>", arg));
		
		Matcher matcher = null;
		try {
			cursor = ads.openCursor(null, null);
			DatabaseEntry fKey = new DatabaseEntry();
			DatabaseEntry fData = new DatabaseEntry();
			while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				String sData = new String(fData.getData(), "UTF-8").toLowerCase();
				matcher = pattern.matcher(sData);
				if (matcher.find()) { // match found retrieve data
					addData(set, fData, full);
				}
			}
		} catch (Exception e) {
			System.err.println("Error");
			e.printStackTrace();
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return set;
	}
	public static HashSet<HashMap<String,String>> getCategory(String arg,Database[] dataArray,boolean full) {
		Database ads = dataArray[0];
		HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
		Cursor cursor = null;
		Pattern pattern = Pattern.compile(String.format("<cat>%s<\\/cat>", arg));
		
		Matcher matcher = null;
		try {
			cursor = ads.openCursor(null, null);
			DatabaseEntry fKey = new DatabaseEntry();
			DatabaseEntry fData = new DatabaseEntry();
			while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				String sData = new String(fData.getData(), "UTF-8").toLowerCase();
				matcher = pattern.matcher(sData);
				if (matcher.find()) { // match found retrieve data
					addData(set, fData, full);
				}
			}
		} catch (Exception e) {
			System.err.println("Error");
			e.printStackTrace();
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return set;
	}
	
	public static HashSet<HashMap<String,String>> getDate(String op, String arg, Database[] dataArray,boolean full) {
		//System.out.println("reached function");
		Database pdate = dataArray[1];
		HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
		ArrayList<String> aids = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = pdate.openCursor(null, null);
			DatabaseEntry fKey = new DatabaseEntry(arg.getBytes("UTF-8"));
			DatabaseEntry fData = new DatabaseEntry();
			//System.out.println("Looking for hit");
			if (cursor.getSearchKeyRange(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) { // hit
				//System.out.println("Hit" + cursor.count());
				if (op.compareTo("=") == 0) {
					//System.out.println("Equals");
					// extract aid
					if (new String(fData.getData(), "UTF-8").compareTo(arg)!=0) {
						return set;
					}
					String sData = new String(fData.getData(), "UTF-8");
					String aid = sData.split(",")[0];
					// add entry
					aids.add(aid);
					// while entries have same key
					//System.out.println("searching more entries");
					while (cursor.getNextDup(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						//System.out.println("Looping");
						// extract aid
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
				} else if (op.compareTo(">=") == 0) {
					// extract aidx
					String sData = new String(fData.getData(), "UTF-8");
					String aid = sData.split(",")[0];
					// add entry
					aids.add(aid);
	        		// TODO: test to see if arg is greater than maximal value
					// while not at end
					while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						// extract aid
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
				} else if (op.compareTo("<=") == 0) {
					String sData, aid;
	        		//System.out.println("Moving to next non-dupe");
					if (new String(fData.getData(), "UTF-8").compareTo(arg) == 0) {
						// extract aid
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
						cursor.getNextNoDup(fKey, fData, LockMode.DEFAULT);
					}
					//System.out.println("Looking back");
					while (cursor.getPrevNoDup(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						//System.out.println("Looping");
						// extract aid
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
				} else if (op.compareTo(">") == 0) {
					String sData, aid;
					if (new String(fData.getData(), "UTF-8").compareTo(arg) == 0) {
						cursor.getNextNoDup(fKey, fData, LockMode.DEFAULT);
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
					while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
	        		// TODO: test to see if arg is greater than maximal value
				} else if (op.compareTo("<") == 0) {
					String sData, aid;

					while (cursor.getPrev(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
					}
				}
			} else { // misses check bounds
				// TODO: fix for bounds
				//System.out.println("Miss");
				//System.out.println("Hit" + cursor.count());
				if (op.compareTo(">") == 0) { // search up from first
					String sData, aid;
					// do search of first, if fkey greater than key search up, else don't do anything
					cursor.getFirst(fKey, fData, LockMode.DEFAULT);
					if (new String(fKey.getData(), "UTF-8").compareTo(arg) > 0) {
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
		        		while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
							sData = new String(fData.getData(), "UTF-8");
							aid = sData.split(",")[0];
							// add entry
							aids.add(aid);
		        		}
					}
				} else if (op.compareTo("<") == 0) { // search down from top
					String sData, aid;
					// do search of last, if fkey less than key search down, else don't do anything
					cursor.getLast(fKey, fData, LockMode.DEFAULT);
					if (new String(fKey.getData(), "UTF-8").compareTo(arg) < 0) {
						sData = new String(fData.getData(), "UTF-8");
						aid = sData.split(",")[0];
						// add entry
						aids.add(aid);
		        		while (cursor.getPrev(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
							sData = new String(fData.getData(), "UTF-8");
							aid = sData.split(",")[0];
							// add entry
							aids.add(aid);
		        		}
					}
				}
			}
			//System.out.println(aids.toString());
			getData(set, aids, dataArray, full);
		} catch (Exception e) {
			System.err.println("Error");
			e.printStackTrace();
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				System.err.println("Error");
				e.printStackTrace();
			}
		}
		 
		return set;
	}
	
	public static void getData(HashSet<HashMap<String, String>> set, ArrayList<String> aids, Database[] dbs, boolean full) {
		Database ad = dbs[0];
		Cursor cursor = null;
		try {
			cursor = ad.openCursor(null, null);
			DatabaseEntry fKey = new DatabaseEntry();
			DatabaseEntry fData = new DatabaseEntry();
			cursor.getFirst(fKey, fData, LockMode.DEFAULT);
			String temp = new String(fKey.getData(), "UTF-8").split(":")[0];
			if (aids.contains(temp)) {
				System.out.println("checking first part");
				addData(set, fData, full);
			}
			//System.out.println("Iterating now");
			while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				temp = new String(fKey.getData(), "UTF-8").split(":")[0];
				//System.out.println(temp);
				if (aids.contains(temp)) {
					//System.out.println("Added");
					addData(set, fData, full);
				}
			}
		} catch (Exception e) {
			System.out.println("Error");
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static HashSet<HashMap<String, String>> getTerm(String arg, Database[] dataArray, boolean full, boolean partial) {
		Database ads = dataArray[0];
		HashSet<HashMap<String, String>> set = new HashSet<HashMap<String, String>>();
		Cursor cursor = null;
		Pattern pattern;
		if (partial) {
			pattern = Pattern.compile(String.format("<ti(>|.*\\s)%s.*<\\/ti>|<desc(>|.*\\s)%s.*<\\/desc>", arg, arg));
		} else {
			pattern = Pattern.compile(String.format("<ti(>|.*\\s)%s\\s.*<\\/ti>|<desc(>|.*\\s)%s\\s.*<\\/desc>", arg, arg));
		}
		Matcher matcher = null;
		try {
			cursor = ads.openCursor(null, null);
			DatabaseEntry fKey = new DatabaseEntry();
			DatabaseEntry fData = new DatabaseEntry();
			while (cursor.getNext(fKey, fData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				String sData = new String(fData.getData(), "UTF-8").toLowerCase();
				matcher = pattern.matcher(sData);
				if (matcher.find()) { // match found retrieve data
					addData(set, fData, full);
				}
			}
		} catch (Exception e) {
			System.err.println("Error");
			e.printStackTrace();
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return set;
	}
	
	public static void addData(HashSet<HashMap<String, String>> set, DatabaseEntry fData, boolean full) {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			String sData = new String(fData.getData(), "UTF-8");
			if (full) {
				Pattern padd = Pattern.compile(Pattern.quote("<aid>") + "(.*?)" + Pattern.quote("</aid>"));
				Pattern pdate = Pattern.compile(Pattern.quote("<date>") + "(.*?)" + Pattern.quote("</date>"));
				Pattern ploc = Pattern.compile(Pattern.quote("<loc>") + "(.*?)" + Pattern.quote("</loc>"));
				Pattern pcat = Pattern.compile(Pattern.quote("<cat>") + "(.*?)" + Pattern.quote("</cat>"));
				Pattern pti = Pattern.compile(Pattern.quote("<ti>") + "(.*?)" + Pattern.quote("</ti>"));
				Pattern pdesc = Pattern.compile(Pattern.quote("<desc>") + "(.*?)" + Pattern.quote("</desc>"));
				Pattern pprice = Pattern.compile(Pattern.quote("<price>") + "(.*?)" + Pattern.quote("</price>"));
				
				Matcher madd = padd.matcher(sData);
				Matcher mdate = pdate.matcher(sData);
				Matcher mloc = ploc.matcher(sData);
				Matcher mcat = pcat.matcher(sData);
				Matcher mti = pti.matcher(sData);
				Matcher mdesc = pdesc.matcher(sData);
				Matcher mprice = pprice.matcher(sData);

				if(madd.find() && mdate.find() && mloc.find() && mcat.find() && mti.find() && mdesc.find() && mprice.find()) {
					map.put("aid", madd.group(1));
					map.put("date",mdate.group(1));
					map.put("loc",mloc.group(1));
					map.put("cat",mcat.group(1));
					map.put("title",mti.group(1));
					map.put("desc",mdesc.group(1));
					map.put("price",mprice.group(1));
					//System.out.println(map.toString());
					set.add(map);
				}
			} else {
				Pattern padd = Pattern.compile(Pattern.quote("<aid>") + "(.*?)" + Pattern.quote("</aid>"));
				Matcher madd = padd.matcher(sData);
				Pattern p = Pattern.compile(Pattern.quote("<ti>") + "(.*?)" + Pattern.quote("</ti>"));
				Matcher m = p.matcher(sData);
				if(m.find() & madd.find()) {
					map.put("aid", madd.group(1));
					map.put("title",m.group(1));
					set.add(map);
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
