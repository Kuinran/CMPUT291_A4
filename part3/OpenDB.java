import com.sleepycat.db.*;

public class OpenDB {
	
	public static Database[] Open() {
		Database ad = null;
		Database date = null;
		Database price = null;
		Database terms = null;
		
		try {
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			ad = new Database ("ad.idx", null, dbConfig);
			date = new Database("da.idx", null, dbConfig);
			price = new Database("pr.idx", null, dbConfig);
			terms = new Database("te.idx", null, dbConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Database[]{ad, date, price, terms};
	}
}
