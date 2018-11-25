import com.sleepycat.db.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.*;
public class HandleQuerry {
	
	//Dataaarray = {addData,dateData,PriceData,termsData}
	public static HashSet<HashMap<String,String>> getPrice(String op, int price, Database[] Dataarray,boolean full) {
		//System.out.println("getPrice Reached!!" + full);
		HashSet<HashMap<String,String>> hashout = new HashSet<HashMap<String,String>>();
		Cursor myCursor = null;
		Database PriceData = Dataarray[2];
		Database addData = Dataarray[0];
		try {
		    myCursor = PriceData.openCursor(null, null);
		    // cursors return every record as a pair of objects of class DatabaseEntry
		    DatabaseEntry foundKey = new DatabaseEntry();
		    DatabaseEntry foundData = new DatabaseEntry();
		    
		    // A call to getNext() fetches the next record, until it returns
		    // with a status that is not OperationStatus.SUCCESS
			//each iteration the cursor points to, KEY:DATA
		    while (myCursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
		        OperationStatus.SUCCESS) {
		    	//System.out.println("If this prints, we are iterating through the price data");
		        Pattern p = Pattern.compile("[A-Za-z0-9]*");
		    	String keyPrice = new String(foundKey.getData(), "UTF-8");
		    	//System.out.println(keyPrice);
		    	keyPrice = keyPrice.replaceAll("\\s","");
		        String dataString = new String(foundData.getData(), "UTF-8");
		        String[] parts = dataString.split("\\s*,\\s*");
		        String aid = parts[0];
		        //System.out.println("The aid is: " + aid);
		        if(op.compareTo(">") == 0) {
		        	if(Integer.parseInt(keyPrice) > price) {
		        		
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(aid, addData);
		        		}
		        		else {
		        			map = getBrief(aid,addData);
		        		}
		     		    //Now add the hashmap to the set
		     		    hashout.add(map);
		        	}
		        
		        }
		        
		        else if(op.compareTo(">=") == 0) {
		        	if(Integer.parseInt(keyPrice) >= price) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(aid, addData);
		        		}
		        		else {
		        			map = getBrief(aid,addData);
		        		}
		     		    //Now add the hashmap to the set
		     		    hashout.add(map);
		        	}
		        }
		    
		        else if(op.compareTo("<") == 0) {
		        	if(Integer.parseInt(keyPrice) < price) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(aid, addData);
		        		}
		        		else {
		        			map = getBrief(aid,addData);
		        		}
		     		    //Now add the hashmap to the set
		     		    hashout.add(map);
		        	}
		        }
		        
		        else if(op.compareTo("<=") == 0) {
		        	if(Integer.parseInt(keyPrice) <= price) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(aid, addData);
		        		}
		        		else {
		        			map = getBrief(aid,addData);
		        		}
		     		    //Now add the hashmap to the set
		     		    hashout.add(map);
		        	}
		        }
		        else if(op.compareTo("=") == 0) {
		        	if(Integer.parseInt(keyPrice) == price) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			//System.out.println("Hello?");
		        			map = getFull(aid, addData);
		        		}
		        		else {
		        			map = getBrief(aid,addData);
		        		}
		     		    //Now add the hashmap to the set
		     		    hashout.add(map);
		        	}
		        }
		    }
		} catch (Exception e) {
		    System.err.println("Error accessing the database: " + e);
		} finally {
		    try {
		        if (myCursor != null) {
		            myCursor.close();
		        }
		    } catch(DatabaseException dbe) {
		        System.err.println("Error in close: " + dbe.toString());
		    }
		}
		return hashout;
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
						//TODO: Michael put this.Verify if it works
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
					String dataString2 = new String(foundData2.getData(), "UTF-8");
					if(aid.compareTo(keyAdd) == 0) {
						//System.out.println("aid match found");
						//TODO: Michael put this.Verify if it works
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
			System.out.println("We are about to return null");
		return map;
	}
	public static HashSet<HashMap<String,String>> getLocation(String loc,Database[] Dataarray,boolean full) {
		HashSet<HashMap<String,String>> hashout = new HashSet<HashMap<String,String>>();
		Cursor myCursor = null;
		Database addData = Dataarray[0];
		
		try {
		    myCursor = addData.openCursor(null, null);
		    // cursors return every record as a pair of objects of class DatabaseEntry		 
		    
		    // A call to getNext() fetches the next record, until it returns
		    // with a status that is not OperationStatus.SUCCESS
			//each iteration the cursor points to, KEY:DATA
		   
		    //add the stupid spaces in key
		    int size = loc.length();
		    for (int i = 0;i < 12 - size;i++) {
		    	loc = " " + loc;
		    }
		    
		    while (true) {
		    	//myCursor.getSearchKey(foundKey.setData(data);, foundData, LockMode.DEFAULT) ==
				       // OperationStatus.SUCCESS
			    DatabaseEntry foundKey = new DatabaseEntry();
			    DatabaseEntry foundData = new DatabaseEntry();
			    foundKey.setData(loc.getBytes());
			    foundKey.setSize(loc.length());
			    System.out.println("This is the location:" + loc);
			    if(myCursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
		    	System.out.println("The keys are a match!");
			    	String keyAdd = new String(foundKey.getData(), "UTF-8");
			    	System.out.println("This is the key:" + keyAdd);
		    	keyAdd = keyAdd.replaceAll("\\s","");
		        String dataString = new String(foundData.getData(), "UTF-8");
		        Pattern ploc = Pattern.compile(Pattern.quote("<loc>") + "(.*?)" + Pattern.quote("</loc>"));
		        Pattern padd = Pattern.compile(Pattern.quote("<aid>") + "(.*?)" + Pattern.quote("</aid>"));
		        
		        Matcher mloc = ploc.matcher(dataString);
		        Matcher madd = padd.matcher(dataString);
		        if(mloc.find() && madd.find()) {
		        	if(mloc.group(1).compareTo(loc) == 0) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(madd.group(1),addData);
		        		}
		        		
		        		else {
		        			map = getBrief(madd.group(1),addData);
		        		}
		        		hashout.add(map);
		        	}
		        }
		    	
		    	}
			    else {
			    	break;
			    }
		}
		    return hashout;
		}
		catch(Exception e) {
			System.err.println("Error accessing the database: " + e);
		}
		finally {
		    try {
		        if (myCursor != null) {
		            myCursor.close();
		        }
		    } catch(DatabaseException dbe) {
		        System.err.println("Error in close: " + dbe.toString());
		    }
		}
		System.out.println("We are about to return null");  
		return null;
	}
	public static HashSet<HashMap<String,String>> getCategory(String category,Database[] Dataarray,boolean full) {
		HashSet<HashMap<String,String>> hashout = new HashSet<HashMap<String,String>>();
		Cursor myCursor = null;
		Database catData = Dataarray[0];
		
		try {
		    myCursor = catData.openCursor(null, null);
		    // cursors return every record as a pair of objects of class DatabaseEntry
		    DatabaseEntry foundKey = new DatabaseEntry();
		    DatabaseEntry foundData = new DatabaseEntry();
		 
		    // A call to getNext() fetches the next record, until it returns
		    // with a status that is not OperationStatus.SUCCESS
			//each iteration the cursor points to, KEY:DATA
		    while (myCursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
		        OperationStatus.SUCCESS) {
		    	
		    	String keyCat = new String(foundKey.getData(), "UTF-8");
		    	keyCat = keyCat.replaceAll("\\s","");
		        String dataString = new String(foundData.getData(), "UTF-8");
		        Pattern pcat = Pattern.compile(Pattern.quote("<cat>") + "(.*?)" + Pattern.quote("</cat>"));
		        Pattern padd = Pattern.compile(Pattern.quote("<aid>") + "(.*?)" + Pattern.quote("</aid>"));
		        
		        Matcher mcat = pcat.matcher(dataString);
		        Matcher madd = padd.matcher(dataString);
		        if(mcat.find() && madd.find()) {
		        	if(mcat.group(1).compareTo(category) == 0) {
		        		HashMap<String, String> map;
		        		if(full) {
		        			map = getFull(madd.group(1),catData);
		        		}
		        		
		        		else {
		        			map = getBrief(madd.group(1),catData);
		        		}
		        		hashout.add(map);
		        	}
		        }
		    	
		    	}
		    return hashout;
		}
		catch(Exception e) {
			System.err.println("Error accessing the database: " + e);
		}
		finally {
		    try {
		        if (myCursor != null) {
		            myCursor.close();
		        }
		    } catch(DatabaseException dbe) {
		        System.err.println("Error in close: " + dbe.toString());
		    }
		}
		System.out.println("We are about to return null");  
		return null;
	}
	public static HashSet<HashMap<String,String>> getDate(String category,Database[] Dataarray,boolean full) {
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		 
		 
		return null;
	}
}
