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



	static RandomAccessFile systemCatFile;

	public static void main(String args[]) throws IOException, InterruptedException {

		systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");
		for(int i =0;i<160000;i++) {
			if(i%1600 ==0) {
				systemCatFile.writeInt(5);
			}else if(i%1600>=5) {
				systemCatFile.write(0);
			}
			
		}
		systemCatFile.seek(0);
		
		
		
		boolean exit = false;
		

		systemCatFile.seek(4); // 0-1599 0-10 first 11 byte for page header writeLoc, 11 is the number of full pages byte.
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
					
					
					String typeName;
					while((typeName=askForTypeInfo(user)).equals("")) {
						System.out.println("There is a type with same name.");
					}
					
					
					systemCatFile.seek(4);
					int numFull = systemCatFile.read();
					
					systemCatFile.seek((numFull-1)*1600);
					System.out.println((numFull-1)*1600);
					int writeLocation = systemCatFile.readInt();
					systemCatFile.seek(writeLocation);
					
					System.out.println(32-typeName.getBytes().length);
					for(int i=1;i<=32-typeName.getBytes().length;i++) {
						systemCatFile.write(0);
					}
					systemCatFile.write(typeName.getBytes());
					System.out.println("Please enter number of fields that the type will have.(1-127)");
					int entered = user.nextInt();
					systemCatFile.write(entered);
					for(int i =1;i<=entered;i++) {
						
						System.out.println("Enter field name "+i+ " (in the same form of typeName. Moreover this can be in length 16 maximum.)");
						String read = user.next();
						System.out.println(16-read.getBytes().length);
						for(int j=1;j<=16-read.getBytes().length;j++) {
							systemCatFile.write(0);
						}
						systemCatFile.write(read.getBytes());
					}
					
					systemCatFile.seek((numFull-1)*1600);
					int prev = systemCatFile.readInt();
					systemCatFile.seek((numFull-1)*1600);
					systemCatFile.writeInt(prev+33+16*entered);
					break;
				case "2": // Look for the given type and if found delete it. Also need to shift afterwards.
					String typeNamed = user.next();
					int loc = 0;
					systemCatFile.seek(4);
					int numFulld =  systemCatFile.read();
					
					for(int page=0;page<numFulld; page++) {
						System.out.println("Reading page " + (page+1) +  "..");
						if(page ==0) { 
							int totalRead = 5;
							while(totalRead <1600) { // When we finish page
								byte[] typename = new byte[32];

								systemCatFile.seek(totalRead);
								int full = systemCatFile.read(typename,0,32);
								if(full==-1)break;
								String typename_ = "";
								for(int c = 0;c<32;c++) {
									typename_.concat(Byte.toString(typename[c]));	
								}
								if(typename_.equals(typeNamed)) loc = (int)systemCatFile.getFilePointer()-32;

								int fieldNum=systemCatFile.read();
								if(fieldNum==-1)break;
								totalRead = totalRead +32+1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
							}



						}else{ 
							int totalRead = 4;
							while(totalRead <1600) { // When we finish page
								byte[] typename = new byte[32];

								systemCatFile.seek(totalRead);
								int full = systemCatFile.read(typename,0,32);
								if(full==-1)break;
								String typename_ = "";
								for(int c = 0;c<32;c++) {
									typename_.concat(Byte.toString(typename[c]));	
								}
								if(typename_.equals(typeNamed)) loc = (int)systemCatFile.getFilePointer()-32;

								int fieldNum=systemCatFile.read();
								if(fieldNum==-1)break;
								totalRead = totalRead +32+1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
							}

						}
						
					}
					
					
					
					if(loc==0) {
						System.out.println("Cannot find the type: "+ typeNamed);
						break;
					}else { 
						// deletion
						systemCatFile.seek(loc);
						for(int i=1;i<=32;i++) {
							systemCatFile.write(0);
						}
						int fieldNum= systemCatFile.read();
						for(int i=1;i<=fieldNum;i++) {
							for(int j=1;j<=16;j++) {
								systemCatFile.write(0);
							}
						}
						// shifting
						int prevPointer = (int)systemCatFile.getFilePointer();
						int size = 160000 - (int)systemCatFile.getFilePointer();
						
						byte[] willBeShifted = new byte[size];
						systemCatFile.read(willBeShifted);
						
						systemCatFile.seek(prevPointer-32);
						systemCatFile.write(willBeShifted);
						
					}
					
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
				e.printStackTrace();
				System.out.println("Invalid input. Try again.");

			}
		}	

	}

	public static String askForTypeInfo(Scanner user) throws IOException {
		String typeName = user.next();
			boolean contains = false;


			systemCatFile.seek(4);
			int numFull =  systemCatFile.read();

			for(int page=0;page<numFull; page++) {
				System.out.println("Reading page " + (page+1) +  "..");
				if(page ==0) { 
					int totalRead = 5;
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
						totalRead = totalRead +32+1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
					}



				}else{ 
					int totalRead = 4;
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
						totalRead = totalRead +32+1 +fieldNum*16; // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.
					}

				}
				
			}
			if(!contains) {
				return typeName;
			}
			return "";
			
		
		
	}
}
