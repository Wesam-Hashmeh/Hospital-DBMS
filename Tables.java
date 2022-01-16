import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class Tables {
	
	static ArrayList<String> multiAttr = new ArrayList<String>(); //will hold multi attr tables 
	
	public static int generateID(String table, String idName) throws SQLException
	{
		String sql_id = "SELECT " + idName + " FROM " + table;
		ArrayList<Integer> id = new ArrayList<Integer>();
		
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_id)){
			ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            	id.add(resultSet.getInt(idName));
			preparedStatement.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return -1;
		}//print out all tables
		
		Collections.sort(id);
		if(id.size() == 0)
			return 1111;
		return id.get(id.size()-1) + 1;
	}
	
	private static String getRefTable(String uConstraintName)
	{
		String sql_table = "select table_name from  information_schema.KEY_COLUMN_USAGE where constraint_name = \'" + uConstraintName + "\'";
		//System.out.println(sql_table);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			String table_name = null;
			
			while (resultSet.next())
				table_name = resultSet.getString("table_name");
			preparedStatement.close();
            return table_name;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	private static String getUCon(String constraintName)
	{
		String sql_table = "select unique_constraint_name from  information_schema.referential_constraints where constraint_name = \'" + constraintName + "\'";
		//System.out.println(sql_table);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			String uConstraintName = null;
			
			while (resultSet.next())
				uConstraintName = resultSet.getString("unique_constraint_name");
			preparedStatement.close();
            return  getRefTable(uConstraintName);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}

	}
	
	//returns null if it is not an fkey, else returns referenced table
	private static String getCon(String table, String colName) throws SQLException
	{
		String refTable = null, fkey = null;
		ArrayList<String> relatedCol = new ArrayList<String>();
		String sql_fkey = "select column_name, constraint_name from information_schema.KEY_COLUMN_USAGE where table_name = " + "\'" + table + "\'";
		
		//System.out.println(sql_fkey);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_fkey)){
			String constraint_name;
			ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
            	constraint_name = resultSet.getString("constraint_name");
            	if(resultSet.getString("column_name").equals(colName))
            		if(constraint_name.contains("fkey")) {
            			fkey = constraint_name;
            			refTable = getUCon(constraint_name);
            			continue;
            		}
            	if(fkey != null)
            		if(constraint_name.equals(fkey))
            			relatedCol.add(resultSet.getString("column_name"));
            }
			preparedStatement.close();
			if(refTable == null)
				return null;
			return refTable + "#" + String.join(",", relatedCol);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	private static boolean isPkey(String table, String colName) throws SQLException
	{
		String sql_pkey = "select column_name, constraint_name from information_schema.KEY_COLUMN_USAGE where table_name = " + "\'" + table + "\'";
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_pkey)){
			String constraint_name;
			ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
            	constraint_name = resultSet.getString("constraint_name");
            	if(resultSet.getString("column_name").equals(colName))
            		if(constraint_name.contains("pkey"))
            			return true;
            }
			preparedStatement.close();
			return false;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	//returns a listing of all tables associated tables, used by show listings to join tables
	private static ArrayList<String> getRefTables(String table) throws SQLException
	{
		ArrayList<String> refTables = new ArrayList<String>();
		String sql_table = "select column_name, data_type, character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name = \'" + table + "\'"
				+ "order by ORDINAL_POSITION;";
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
			{
				String refTable;
				if((refTable = getCon(table, resultSet.getString("column_name"))) != null) {
					//System.out.println(refTable);
					refTable = refTable.substring(0, refTable.indexOf('#'));
					if(!refTables.contains(refTable))
						refTables.add(refTable);
				}
			}
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		return refTables;
	}
	
	//for formatting showListings
	public static String makeSpace(String output, int space)
	{
		if(output == null)
			output = "null";
		int outputsize = output.length();;
		for(int i = 0; i < space - outputsize; i++)
			output += " ";
		return output;
	}
	
	//shows listings on table with select
	public static void showListings(String table, String select, boolean grabRef, String join, String where) throws SQLException
	{
		ArrayList<String> refTables = new ArrayList<String>();
		
		if(grabRef) {
			refTables = getRefTables(table);
			if(refTables.size() > 0)
				table += " natural join " + String.join(" natural join  ", refTables);
		}
		if(join != null && !join.equals(""))
			table += "  natural join " + join + " ";
		
		
		
		String sql_select = "Select  " + select + " From " + table;
		
		if(where != null && !where.equals(""))
			sql_select += " where " + where;
		
		System.out.println(sql_select);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_select)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnsNumber = metadata.getColumnCount();
			
			for(int i = 1; i <= columnsNumber; i++) System.out.print(makeSpace(metadata.getColumnLabel(i), 30));
			System.out.println();

            while (resultSet.next()) {
            		for (int i = 1; i <= columnsNumber; i++)
            			System.out.print(makeSpace(resultSet.getString(i), 30));
            		System.out.println();
            }
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	//displays all the tables (other than multi attr tables), lets the user choose one and returns it
	//NOTE, enterTable fills in multiAttr list so call it before addEntry or viewEntry
	public static String enterTable() throws SQLException
	{
		String table = "";
		multiAttr.clear();
		
		String sql_table = "SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';";
		
		System.out.println("List of tables in database:");
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
            	String tablename = resultSet.getString("tablename");
            	if(tablename.contains("organ_donor_")) {//no need to print out organ_donor multi-attr
            		multiAttr.add(tablename);
            		continue;
            	}
            	System.out.println(tablename);
            }
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}//print out all tables
		
		System.out.println("Enter desired table: ");
		table = BloodDonation.scan.next();
		return table;
	}

	//takes a table, adds in user input as values
	//any presentPkey values (col_name, val) is entered in automatically
	public static boolean addEntry(String table, Hashtable<String, String> presetPkey) throws SQLException
	{
		ArrayList<String> col = new ArrayList<String>(); //will hold col vals for new entry
		Hashtable<String, String> pkey = new Hashtable<String, String>(); //will hold pkeys of table
		
		
		String sql_table = "select column_name, data_type, character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name = \'" + table + "\'"
				+ "order by ORDINAL_POSITION;";
		
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if(!resultSet.next())
			{
				System.out.println("Table empty or non-existant.");
				return false;
			}else do {
				String col_name = resultSet.getString("column_name");
				String type = resultSet.getString("data_type");
				String val, retRELATED; 
				boolean isPkey = false;
				
				//save the pkeys
				if(isPkey(table, col_name)) isPkey = true;
					
				//how to handle input
				if(presetPkey != null && presetPkey.containsKey(col_name)) //was the pkey given to us? (from multivariate)
					val = presetPkey.get(col_name);
				else if((retRELATED = getCon(table, col_name)) != null) { //is this an fkey - get its listings
					int del =  retRELATED.indexOf('#');
					String refTable = retRELATED.substring(0, del);
					String otherCol = retRELATED.substring(del+1, retRELATED.length());
					if(!otherCol.equals(""))
						otherCol = col_name + "," + otherCol;
					else otherCol = col_name;
            		showListings(refTable, otherCol, false, "", null);
            		System.out.println("Enter in a value for " + col_name + " from the above options or 0 to exit: ");
            		val = BloodDonation.scan.next();
            	}	
            	else if(col_name.contains("_id"))//is it an id?
            		val = String.valueOf(generateID(table, col_name));
            	else {
            		if(col_name.equals("blood_type"))
            			val = BloodDonation.scanBlood();
            		else if(type.equals("date")) {
            			val = BloodDonation.scanDate(col_name);
            		}
            		else {
	            		System.out.println("Enter in a value for " + col_name + " (" + type + ") or 0 to exit: ");
	            		val = BloodDonation.scan.next();
            		}
            	}
				if(isPkey) pkey.put(col_name, val); //remember the pkey incase of multi attr
				
				if(val != null && val.equals("0")) //sentinel for quick exit
					return false;
            	//formating
            	if(val != null && (type.equals("character varying") || type.equals("date")) && !val.equals("null"))//if it is a character type
            		val = "\'" + val + "\'";
            	
            	col.add(val);
			} while (resultSet.next());
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}//enter in vals for each col
		
		sql_table = "INSERT INTO " + table + " VALUES ( " +  String.join(", ", col).toLowerCase() + ") RETURNING *";
		
		//System.out.println(sql_table);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_table)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData metadata = resultSet.getMetaData();
			ArrayList<String> val = new ArrayList<String>();

            while (resultSet.next()) {
            	for(int i = 0; i < metadata.getColumnCount(); i++)
            		val.add(resultSet.getString(metadata.getColumnName(i+1)));
            }
            System.out.println("Adding the entry to table " + table + " with values (" + String.join(", ", val) + ")");
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}//insert into table
		
		if(table.equals("organ_donor")) { //organ_donor has multi attr!
			for(int i = 0; i < multiAttr.size(); i++) {
				int input = 1;
				System.out.println("Would you like to add an entry (multivariate) for this " + table + " in " + multiAttr.get(i).replaceFirst("organ_donor_", "")
						+ " (1 for yes, 0 for no): ");
				input = BloodDonation.scanInt();
				while(input == 1) {
					addEntry(multiAttr.get(i), pkey);
					System.out.println("Would you like to add another entry (multivariate) for this " + table + " in " + multiAttr.get(i).replaceFirst("organ_donor_", "")
							+ " (1 for yes, 0 for no): ");
					input = BloodDonation.scanInt();
				}
			}
		}
		
		return true;
	}
	
	public static void viewEntries(String table) throws SQLException
	{
		showListings(table, "*", true, null, null);
		if(table.equals("organ_donor")) {
			for(int i = 0; i < multiAttr.size(); i++) {
				int input = 1;
				System.out.println("Would you like to view the " + multiAttr.get(i).replaceFirst("organ_donor_", "") + " (multivariate) listing for " + table
						+ " (1 for yes, 0 for no): ");
				input = BloodDonation.scanInt();
				while(input == 1) {
					System.out.println("Enter the donor_id of the donor whose multivariate attribute you wish to see: ");
					int donor_id = BloodDonation.scanInt();
					showListings(multiAttr.get(i), "*", false, null, " donor_id = " + donor_id);
					System.out.println("Would you like to view the " + multiAttr.get(i).replaceFirst("organ_donor_", "") + " (multivariate) listing for another donor"
							+ " (1 for yes, 0 for no): ");
					input = BloodDonation.scanInt();
				}
			}
		}
	}
}
