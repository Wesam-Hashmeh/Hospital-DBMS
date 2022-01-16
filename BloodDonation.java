import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class BloodDonation {
	
	static Scanner scan = new Scanner(System.in); 
	static Scanner scani = new Scanner(System.in); 
	static Connection conn;
	
	public static int scanInt()
	{
		String ret = scani.next();
		try {
			return Integer.parseInt(ret);
		} catch(Exception e){
			System.out.print("Try again (Enter an integer value): ");
			return scanInt();
		}
	}
	
	public static String scanDate(String attr)
	{
		String forAttr = "";
		if(attr != null && !attr.equals(""))
			forAttr = "for " + attr;
		System.out.println("Enter a date " + forAttr + " in the format YYYY-MM-DD (or enter T for today's date): ");
		String date = scani.next();
		
		if(date.equals("null"))
			return null;
		
		if(date.equals("T")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			return LocalDate.now().format(formatter);
		}
			
		
		if(date.length() != 10)
			return scanDate(attr);
		for(int i = 0; i < date.length(); i++) {
			char c = date.charAt(i);
			if(i != 4 && i != 7) {
				if(!Character.isDigit(c))
					return scanDate(attr);
			}
			else
				if(c != '-')
					return scanDate(attr);
		}
		return date;
	}
	
	public static String scanBlood()
	{
		System.out.println("Enter a valid value for blood_type (such as A-):");
		String blood_type = scani.next();
		if(blood_type.length() != 2 && blood_type.length() != 3)
				return scanBlood();
		if(blood_type.length() == 2) {
			if(blood_type.charAt(0) != 'A' && blood_type.charAt(0) != 'B' && blood_type.charAt(0) != 'O')
				return scanBlood();
			if(blood_type.charAt(1) != '+' && blood_type.charAt(1) != '-' )
				return scanBlood();
		}
		if(blood_type.length() == 3) {
			if(!blood_type.substring(0,2).equals("AB"))
				return scanBlood();
			if(blood_type.charAt(2) != '+' && blood_type.charAt(2) != '-' )
				return scanBlood();
		}
		return blood_type;
	}
	
	public static void main(String[] args) throws SQLException {
		String url = "jdbc:postgresql://localhost:5433/CS426";
		
		scan.useDelimiter(System.lineSeparator());
		scani.useDelimiter(System.lineSeparator());
		
		int input = 0; //contains user input option
		
		while(conn == null)
			conn = Roles.login(url);
		
		do {
            System.out.println("Enter the integer with the corrisponding menu option:\n"
            		+ "1. Organ Donor List \n"
            		+ "2. Blood Donor List \n"
            		+ "3. Operations Report \n"  
            		+ "4. Income Report \n"
            		+ "5. Donor Match List \n"
            		+ "6. Database Settings \n"
            		+ "7. Exit");
            
            input = scanInt(); //Declares input variable and gathers input from user
            
            switch(input) {
            
            case 1:
            	Queries.organDonorList();
            	break;
            	
            case 2:
            	Queries.bloodDonorList();
            	break;
            	
            case 3:
            	Queries.operationReport();
            	break;
            	
            case 4:
            	Queries.incomeReport();
            	break;
	
            case 5:
            	Queries.donorMatchList();
            	break;
            	
            case 6: //Database Settings
            	System.out.println("What operation would you like to perform:\n"
            			+ "1. Add new user \n"
            			+ "2. Change password of current user login \n"
            			+ "3. Add new entry to existing table \n"
            			+ "4. View an existing table");
            	int sys_input = scanInt();
            	
            	switch(sys_input) {
            	case 1:
            		Roles.createUser();
            		break;
            	case 2:
            		Roles.alterPass();
            		break;
            	case 3:
            		Tables.addEntry(Tables.enterTable(), null);
            		break;
            	case 4:
            		Tables.viewEntries(Tables.enterTable());
            		break;
            	}
            	break;
            }
            
		} while(input != 7); // While loop that ends when the input is 7
        scan.close(); //closing the scanner object
        System.out.println("Goodbye!");
		
	}
}
