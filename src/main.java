import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.Vector;


public class main {

	
	
	
	public static void main(String args[]) throws IOException, InterruptedException {
		boolean exit = false;
		RandomAccessFile systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");
		System.out.print(systemCatFile.length());
		
		Scanner user = new Scanner(System.in);
		System.out.println("Welcome to console storage manager! ");
		
		while(!exit) {
			
			System.out.println("Things you can do:");
			System.out.println("1.Create a type");
			System.out.println("2.Delete a type");
			System.out.println("3.List all types");
			System.out.println("4.Creating a record");
			System.out.println("5.Delete a record");
			System.out.println("6.Search for a record");
			System.out.println("7.List all records of a type");
			System.out.println("Enter a number to continue...");
			
			switch(user.next()) {
			case "1":
				System.out.println("Enter name:");
				System.out.println("It should be alphanumeric. It should not contain non-ascii characters. e.g some turkish letters. ");
				
				boolean enterInvalid = false;
				while(enterInvalid) {
				
				String typeName = user.next();
				
				if(typeName.length()>32 || !typeName.matches("\\A\\p{ASCII}*\\z")) {
					
					enterInvalid =true;
					
				}else {
					String[] types = listAllTypes();
				}
				
				
				}
				break;
			case "2":
				break;
			case "3":
				break;
			case "4":
				break;
			case "5":
				break;
			case "6":
				break;
			case "7":
				break;
			default:
				System.out.println("Please enter one of the options.");
				Thread.sleep(3000);
				break;
			}	
		}	
		
	}

	private static String[] listAllTypes() {
		// TODO Auto-generated method stub
		return null;
	}
}
