import java.sql.*;
import java.util.ArrayList;

public class Queries {
	
	public static void organDonorList() throws SQLException
	{
		System.out.println("What would you like to search by : \n1. Region \n2. Organ\n3. Specialized Doctor \nIf you would like to search by more than on category, write each respective number in one input"); 
        String ODL_in = BloodDonation.scan.next();
        
        ArrayList<String> where = new ArrayList<String>();
        
        if (ODL_in.contains("1")) { //SQL code for searching by region of requested organ
        	System.out.println("Enter Desired City:");
        	String temp = "";
            temp += " city = " + "\'" + BloodDonation.scan.next() + "\'";
            System.out.println("Enter Desired State:");
            temp += " AND state = " + "\'" +  BloodDonation.scan.next() + "\'";
            where.add(temp);
        }
    
        if (ODL_in.contains("2")) { //SQL code for searching by organ of requested organ
        	System.out.println("Enter Desired Organ:");
        	String temp = "organ_name = " + "\'" + BloodDonation.scan.next() + "\'";
        	where.add(temp);
        }
        
        if (ODL_in.contains("3")) { //SQL code for searching by Specialized Doctor of requested organ
        	System.out.println("Enter Specialized Doctor (by organ):");
        	String temp = "doctor.organ_name = " + "\'" + BloodDonation.scan.next() + "\'";
        	where.add(temp);
        }
        
        String final_where = String.join("  AND  ", where);
        //System.out.println(final_where);
        Tables.showListings("organ_donor", "*", true, "hospital", final_where);
	}
	
	public static void bloodDonorList() throws SQLException // 2
	{

		System.out.println("What would you like to search by : \n1. Region \n2. Blood Type\n3. Availabiltiy \n4. Age group \nIf you would like to search by more than on category, write each respective number in one input"); 
         String ODL_in = BloodDonation.scan.next();
            
        ArrayList<String> where2 = new ArrayList<String>();
        
        if (ODL_in.contains("1")) { //SQL code for searching by region of requested organ
        	System.out.println("Enter Desired City:");
        	String temp = "";
            temp += " city = " + "\'" + BloodDonation.scan.next() + "\'";
            System.out.println("Enter Desired State:");
            temp += " AND state = " + "\'" +  BloodDonation.scan.next() + "\'";
            where2.add(temp);
        }
    
        if (ODL_in.contains("2")) { //SQL code for searching by bloodtype of requested organ
           	System.out.println("Enter Desired Blood Type: ");
           	String temp = " blood_type = " + "\'" + BloodDonation.scan.next() + "\'";
           	where2.add(temp);
        }
        
        if (ODL_in.contains("3")) { //SQL code for searching by availability of requested organ
        	String temp = " avail_date >= " + "\'" + BloodDonation.scanDate("availability") + "\'";
        	where2.add(temp);
        }
        
        if (ODL_in.contains("4")) { //SQL code for searching by age group of requested organ
        	System.out.println("Enter Desired Age: ");
        	int age = BloodDonation.scanInt();
        	int lower = (age / 10) * 10;
        	int upper = lower + 10;
         	String temp = " age >= " + lower + " AND age <= " + upper;
         	where2.add(temp);
         }
        
        String final_where2 = String.join(" AND ", where2);
        Tables.showListings("organ_donor_organs", "*", true, "", final_where2);
	}
	
    private static boolean BloodDonationCompat(String Recipient, String Donor){
        
        if(Recipient == null || Donor == null)
            return false;
        
        Recipient = Recipient.toUpperCase();
        Donor = Donor.toUpperCase();
        
        //System.out.println(Recipient + " " + Donor);
        
        if(Recipient.equals("A+"))
            if(Donor.equals("A+") || Donor.equals("A-")  || Donor.equals("O+")  || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("A-"))
            if(Donor.equals("A-") || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("B+"))
            if(Donor.equals("B+") || Donor.equals("B-")  || Donor.equals("O+")  || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("B-"))
            if(Donor.equals("B-") || Donor.equals("O-")) 
                return true;
                
        if(Recipient.equals("AB+"))
            if(Donor.equals("AB+") || Donor.equals("A+") || Donor.equals("A-")  || Donor.equals("B+") || Donor.equals("B-")  || Donor.equals("O+")  || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("AB-"))
            if(Donor.equals("AB-") || Donor.equals("A-") ||Donor.equals("B-") || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("O+"))
            if(Donor.equals("O+") || Donor.equals("O-"))
                return true;
                
        if(Recipient.equals("O-"))
            if(Donor.equals("O-"))
                return true;
        
        return false;
    } 
    
    public static void donorMatchList() throws SQLException
    {
    	String select = "patient_name, patient_id, patient.organ_name, donor_name, organ_donor.donor_id, patient.blood_type, organ_donor.blood_type";
    	String table = "organ_donor natural join organ_donor_organs "
    			+ "right join patient on patient.organ_name = organ_donor_organs.organ_name";
    	
    	String sql_select = "Select " + select + "  from " + table;
		
		System.out.println(sql_select);
		try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_select)){
			
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnsNumber = metadata.getColumnCount();
			
			for(int i = 1; i <= columnsNumber; i++) System.out.print(Tables.makeSpace(metadata.getColumnLabel(i), 30));
			System.out.println();

            while (resultSet.next()) {
            	String rec = resultSet.getString(6);
            	String don = resultSet.getString(7);

            	if(!BloodDonationCompat(rec, don)) //if the blood_types do not match, then skip line
            		continue;
            	for (int i = 1; i <= columnsNumber; i++)
            		System.out.print(Tables.makeSpace(resultSet.getString(i), 30));
            	System.out.println();
            }
			preparedStatement.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
    }
	
	public static void incomeReport() throws SQLException
	{
		Tables.showListings("hospital", "hosp_name, hosp_state, hosp_city", false, "", null);
		System.out.println("Enter desired hospital name from the above options:");
    	String temp = "";
    	String h_name = BloodDonation.scan.next();
        temp += " hospital.hosp_name = " + "\'" + h_name + "\'";
        System.out.println("Enter desired hospital state from the above options:");
        temp += " AND hospital.hosp_state = " + "\'" +  BloodDonation.scan.next() + "\'";
    	System.out.println("Enterdesired hospital city from the above options:");
    	temp += " AND hospital.hosp_city = " + "\'" +  BloodDonation.scan.next() + "\'";

        ArrayList<String> where3 = new ArrayList<String>();
        where3.add(temp);
        String final_where3 = String.join(" AND ", where3);
        String sql_select3 = " SELECT * FROM patient natural join hospital WHERE "
        		+ final_where3;
        
        //System.out.println(sql_select3);
        
        try (PreparedStatement preparedStatement = BloodDonation.conn.prepareStatement(sql_select3)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            
            int total_income = 0;
            while (resultSet.next()) {
            	total_income += resultSet.getInt("hosp_cost");
            }
            System.out.println("Total Income from " + h_name + ": $" + total_income + ".00");

        } catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void operationReport() throws SQLException
	{
		String where;
		
		System.out.println("Enter the doctor_id of the desired doctor's operation report or 0 to see all reports: ");
		String doctor_id = BloodDonation.scan.next();
		String table = "(SELECT doctor_name, doctor_id, count(patient_id) as operations_count"
						+ " FROM doctor natural join patient  "
						+ " GROUP BY doctor_name, doctor_id "
						+ " ORDER BY count(patient_id) asc) as foo";
		if(!doctor_id.equals("0"))
			where = "doctor_id = " + doctor_id;
		else
			where = "";
			
		Tables.showListings(table, "*",false, "", where);
	}
}