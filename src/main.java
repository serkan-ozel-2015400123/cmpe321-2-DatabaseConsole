import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;


public class main {
	
	
	
	
	public static int TypeFieldNum;
	
	static RandomAccessFile systemCatFile;

	public static void main(String args[]) throws IOException, InterruptedException {
		systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");
		
		boolean exit = false;
		
		systemCatFile.seek(11); // 0-1599 0-10 first 11 byte for page header writeLoc, 11 is the number of full pages byte.
		systemCatFile.write(1); // Write 1 to number of full pages right now.
		// System.out.print(systemCatFile.length());
		
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
			try{
				String input = user.next();
			switch(input) {
			case "1":
				
				System.out.println("Enter name:");
				System.out.println("It should be alphanumeric. It should not contain non-ascii characters. e.g some turkish letters. ");
				
				while(askForTypeInfo(user)) {
					System.out.println("Invalid name. Enter again.");
				}
				System.out.println("Please enter number of fields that the type will have.");
				while(askForFieldNumber(user)) {
					System.out.println("Invalid number. Enter again.");
				}
				for(int i =1;i<=TypeFieldNum;i++) {
					System.out.println("Enter field name " + i);
					while(askFieldName(user)) {
						System.out.println("Invalid name. Enter again.");
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
			}catch(Exception e ) {
				System.out.println("Invalid input. Try again.");
				
			}
		}	
		
	}

	public static boolean askFieldName(Scanner user) throws IOException {
		
		boolean enterInvalid = false;		
		
		String typeName = user.next();
		
		if(typeName.length()>16 || !typeName.matches("\\A\\p{ASCII}*\\z")) {
			
			enterInvalid =true;
			
		}else {
			boolean contains = false;
			
			
			
			systemCatFile.seek(1);
			int numFull =  systemCatFile.read();
			
			for(int page=0;page<numFull; page++) {
				System.out.println("Reading page " + (page+1) +  "..");
				if(page ==0) { 
					int totalRead = 12;
					while(totalRead <1600) { // When we finish page
						byte[] typename = new byte[32];
						
						systemCatFile.seek(totalRead);
						int full = systemCatFile.read(typename,0,32);
						if(full==-1)break;
						String typename_ = "";
						for(int c = 0;c<32;c++) {
							typename_.concat(Byte.toString(typename[c]));	
						}
						if(typename_.equals(typeName)) contains = true;
						
						int fieldNum=systemCatFile.read();
						if(fieldNum==-1)break;
						totalRead = totalRead +1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
					}
						
					
					
				}else{ 
					int totalRead = 11;
					while(totalRead <1600) { // When we finish page
						byte[] typename = new byte[32];
						
						systemCatFile.seek(totalRead);
						int full = systemCatFile.read(typename,0,32);
						if(full==-1)break;
						String typename_ = "";
						for(int c = 0;c<32;c++) {
							typename_.concat(Byte.toString(typename[c]));	
						}
						if(typename_.equals(typeName)) contains = true;
						
						int fieldNum=systemCatFile.read();
						if(fieldNum==-1)break;
						totalRead = totalRead +1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
					}
							
				}
				
			}
			
			if(contains)enterInvalid = true;
		}
		
		return enterInvalid;
		
		
	}

	public static boolean askForFieldNumber(Scanner user) {
		boolean enterInvalid = false;		
		boolean hasnttakenInt = true;
		
		while(hasnttakenInt) {
		
			String fieldNum = user.next();
			
		try{
			
			
			if(Integer.parseInt(fieldNum) > 127 || Integer.parseInt(fieldNum) < 0) {
				
				enterInvalid =true;
			
			}
			TypeFieldNum =Integer.parseInt(fieldNum);
			hasnttakenInt = false;
		}catch (Exception e) {
			System.out.println("Error. Try again.");
		}
		
		}
		
		
		
		
		return enterInvalid;
	}

	
	public static boolean askForTypeInfo(Scanner user) {
		
		boolean enterInvalid = false;		
		
		String typeName = user.next();
		
		if(typeName.length()>32 || !typeName.matches("\\A\\p{ASCII}*\\z") || typeName.contains(" ")) {
			
			enterInvalid =true;
			
		}else {
			HashSet<String> types = new HashSet<String>();
			if(types.contains(typeName)) {
				enterInvalid = true;
			}
		}
		return enterInvalid;
	}
}
