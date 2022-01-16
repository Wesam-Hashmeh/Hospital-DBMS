import java.sql.*;

public class Roles {
	
	public static Connection login(String url) throws SQLException
	{
		String user = "", pass = "";
		Connection conn;
		
		System.out.print("Enter Username: ");
		user = BloodDonation.scan.next();
		System.out.print("\nEnter Password: ");
		pass = BloodDonation.scan.next();
		
		try {
			conn = DriverManager.getConnection(url, user, pass);
			
			return conn;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public static boolean createUser() throws SQLException
	{
		String user = "", pass = "";
		
		System.out.println("Enter Username: ");
		user = BloodDonation.scan.next();
		System.out.println("\nEnter Password: ");
		pass = BloodDonation.scan.next();
		
		String sql_create = " create user " + user + " with password " + "\'" + pass + "\'";
		//System.out.println(sql_create);
		
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_create)){
			
			if(preparedStatement.execute())
			{
				System.out.println("Successfully added user " + user + ".");
				preparedStatement.close();
				return true;
			}
			preparedStatement.close();
			return false;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public static boolean alterPass() throws SQLException
	{
		String pass = "";
		
		System.out.print("\nEnter new password: ");
		pass = BloodDonation.scan.next();
		
		String sql_create = " ALTER USER CURRENT_USER WITH PASSWORD \'" + pass + "\'";
		//System.out.println(sql_create);
		
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_create)){
			
			if(preparedStatement.execute())
			{
				System.out.println("Successfully changed password.");
				preparedStatement.close();
				return true;
			}
			preparedStatement.close();
			return false;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
}