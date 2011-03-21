package qa.conceptnet.wrapper.blah;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DatabaseMetaData;
 
public class HelloDatabase
{
	public static void main (String[] args) throws Exception
	{
		// register the driver 
		String sDriverName = "org.sqlite.JDBC";
		Class.forName(sDriverName);
		 
		
		String sTempDb = "db/conceptnet/ConceptNet.db";
		String sJdbc = "jdbc:sqlite";
		String sDbUrl = sJdbc + ":" + sTempDb;
		// which will produce a legitimate Url for SqlLite JDBC :
		// jdbc:sqlite:hello.db
		int iTimeout = 30;
		String sMakeSelect = "SELECT text FROM conceptnet_concept";
		
		Connection conn = DriverManager.getConnection(sDbUrl);
		try {
			Statement stmt = conn.createStatement();
			try {
				stmt.setQueryTimeout(iTimeout);
				//stmt.executeUpdate( sMakeTable );
				//stmt.executeUpdate( sMakeInsert );
				ResultSet rs = stmt.executeQuery(sMakeSelect);
				try {
					for (int i = 0; i < 10; ++i, rs.next()) {
					String sResult = rs.getString("text");
					System.out.println(sResult);
					
					}
				} finally {
				    try { rs.close(); } catch (Exception ignore) {}
				}
			} finally {
			    try { stmt.close(); } catch (Exception ignore) {}
			}
		} finally {
		    try { conn.close(); } catch (Exception ignore) {}
		}
		
		/*// now we set up a set of fairly basic string variables to use in the body of the code proper
		String sTempDb = "hello.db";
		String sJdbc = "jdbc:sqlite";
		String sDbUrl = sJdbc + ":" + sTempDb;
		// which will produce a legitimate Url for SqlLite JDBC :
		// jdbc:sqlite:hello.db
		int iTimeout = 30;
		String sMakeTable = "CREATE TABLE dummy (id numeric, response text)";
		String sMakeInsert = "INSERT INTO dummy VALUES(1,'Hello from the database')";
		String sMakeSelect = "SELECT response from dummy";
		 
		// create a database connection
		Connection conn = DriverManager.getConnection(sDbUrl);
		try {
		Statement stmt = conn.createStatement();
		try {
		stmt.setQueryTimeout(iTimeout);
		stmt.executeUpdate( sMakeTable );
		stmt.executeUpdate( sMakeInsert );
		ResultSet rs = stmt.executeQuery(sMakeSelect);
		try {
		while(rs.next())
		{
		String sResult = rs.getString("response");
		System.out.println(sResult);
		}
		} finally {
		    try { rs.close(); } catch (Exception ignore) {}
		}
		} finally {
		    try { stmt.close(); } catch (Exception ignore) {}
		}
		} finally {
		    try { conn.close(); } catch (Exception ignore) {}
		}*/
	}
 
}
