import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;


public class main {



	static RandomAccessFile systemCatFile;

	public static void main(String args[]) throws IOException, InterruptedException {

		Scanner user = new Scanner(System.in);//FOR USER INPUT	
		boolean exit = false;//WHEN TRUE THE LOOP STOPS
		////////////////////////////INITIALIZATION OF SYSTEM CATALOG//////////////////////
		systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");
		systemCatFile.writeInt(5); // Write "5" to the writeLocation byte.
		for(int i =4;i<160000;i++)systemCatFile.write(0);
		////////////////////////////THE BYTE FOR THE NUMBER OF FULL PAGES IS WRITTEN//////////////////////
		systemCatFile.seek(4); 
		systemCatFile.write(1); 
		////////////////////////////THE LOOP//////////////////////

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

				String input = user.next();			// MENU NAVIGATION INPUT
				switch(input) {
				case "1": ////////////////////////////// CREATE A TYPE///////////////////////

					System.out.println("Enter name:");
					System.out.println("It should be alphanumeric. It should not contain non-ascii characters. e.g some turkish letters. ");


					String typeName;
					while((typeName=askForTypeInfo(user)).equals("")) {
						System.out.println("There is a type with same name.");
					}

					systemCatFile.seek(0); // 
					int writeLocation = systemCatFile.readInt();//} TO READ NUM FULL

					
					systemCatFile.seek(writeLocation); 
					

					for(int i=1;i<=32-typeName.getBytes().length;i++) {//FILL THE REST WITH ZEROS
						systemCatFile.write(0);
					}
					systemCatFile.write(typeName.getBytes()); // WRITE THE NAME
					System.out.println("Please enter number of fields that the type will have.(1-97)");
					int entered = user.nextInt();  // FIELD NUMBER ENTERED
					systemCatFile.write(entered);  // WRITE IT AFTER TYPENAME
					for(int i =1;i<=entered;i++) { // FOR FIELD NUMBER TIMES, ASK A FIELD NAME FROM USER AND WRITE IT TO THE SYSTEM CATALOG

						System.out.println("Enter field name "+i+ " (in the same form of typeName. Moreover this can be in length 16 maximum.)");
						String read = user.next(); // ENTERED NAME
						for(int j=1;j<=16-read.getBytes().length;j++) { // FILL THE REST WITH ZEROS
							systemCatFile.write(0);
						}
						systemCatFile.write(read.getBytes());  // WRITE THE FIELD NAME AFTER POSSIBLE ZEROS
					}

					systemCatFile.seek(0); // UPDATE THE FIRST PAGE HEADER'S WRITE LOCATION INTEGER
					int prev = systemCatFile.readInt(); // PRIOR VALUE
					systemCatFile.seek(0); // COME BACK TO WRITE 
					prev = prev+32+1+16*entered;
					systemCatFile.writeInt(prev); // WRITE THE NEW VALUE
					System.out.println("Created type: " + typeName + " with " + entered + " field(s).");
					System.out.println("---------------------------------------------------------------");
					break;
				case "2": ////////////////////////////////DELETE A TYPE/////////////////////////// 
					// Look for the given type and if found delete it. Also need to shift afterwards.
					System.out.println("Please enter the name of the type you want to delete....");
					String typeNamed = user.next(); // THE TYPE THAT USER WANTS TO DELETE
					int loc = 0; // BEGIN LOCATION OF THE TYPE
					systemCatFile.seek(4); // LOCATION OF NUMBER OF FULL PAGES
					int numFulld =  systemCatFile.read(); // NUMBER OF FULL PAGES
					HashSet<Integer> readPages = new HashSet<Integer>();
					
					int fieldNum=0;
					for(int totalRead=0;totalRead<(numFulld*1600);totalRead = totalRead +32+1 +fieldNum*16) {
						if(!readPages.contains(totalRead / 1600+1)) {
							System.out.println("Reading page " + ((totalRead/1600)+1) +  "..");
							readPages.add(totalRead / 1600+1);
						}
						//Thread.sleep(1000);
						if(totalRead<5) {
							totalRead= 5;  // WE HAVE 4 BYTES FOR WRITE LOCATION AND A BYTE FOR THE NUMBER OF FULL PAGES. SO START WRITING FROM 5th BYTE	
						}

							byte[] typename = new byte[32]; 

							systemCatFile.seek(totalRead); // GO TO totalRead th BYTE.
							systemCatFile.read(typename,0,32); // READ INTO typename

							String typename_ = new String(typename,StandardCharsets.UTF_8);  // CONVERSION INTO STRING 

							String typenameNew_=""; // THIS IS FOR REMOVING NULL CHARS AT THE BEGINNING OF typename_
							for(int i=0;i<typename_.length();i++) {
								if(typename_.charAt(i)!=0)typenameNew_ = typenameNew_+typename_.charAt(i); // WE EXCLUDE THE 0 VALUED CHARS FROM THE NEW STRING
							}

							if(typenameNew_.equals(typeNamed)) {
								loc = (int)systemCatFile.getFilePointer()-32; // IF WE FIND
								fieldNum=systemCatFile.read();
								break;
							}
							fieldNum=systemCatFile.read(); // ALSO READ FIELD NUMBER WE MAY NEED IT
							if(fieldNum==-1)break;
							
							 // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.

						


					}

					if(loc==0) { // If we weren't able to find 
						System.out.println("Cannot find the type: "+ typeNamed);
						break;
					}else { 
						System.out.println("Type found. Deleting....");
						//Thread.sleep(1000);
						// deletion
						systemCatFile.seek(loc);
						for(int i=1;i<=32;i++) {
							systemCatFile.write(0); // Write 32 0's onto it.
						}
						fieldNum= systemCatFile.read(); // Read field number to know how many iterations we will do
						systemCatFile.seek(systemCatFile.getFilePointer()-1);
						systemCatFile.write(0); // Delete field number byte
						
						int typeSize = fieldNum*16+32+1;
								
						for(int i=1;i<=fieldNum;i++) { // Then delete all the fields.
							for(int j=1;j<=16;j++) {
								systemCatFile.write(0);
							}
						}
						// shifting all the other records to the left.

						
						int prevPointer = (int)systemCatFile.getFilePointer();
						int pageNumber = (prevPointer /1600)+1;
						systemCatFile.seek(4);
						int numfull = systemCatFile.read();
						System.out.println("Reading pages:"+ pageNumber+ "-"+numfull + " for shifting..");
						
						int size = (numfull*1600)-prevPointer;

						byte[] willBeShifted = new byte[size];
						systemCatFile.seek(prevPointer);
						systemCatFile.read(willBeShifted);

						systemCatFile.seek(prevPointer-typeSize);
						systemCatFile.write(willBeShifted);
						
						systemCatFile.seek(0); // To update writeLocation
						int prevWL = systemCatFile.readInt();
						systemCatFile.seek(0);
						systemCatFile.writeInt(prevWL-typeSize);

					}

					System.out.println("Done with deletion.");
					System.out.println("---------------------------------------------------------------");


					break;
					
				case "3": //////////////////////////LIST ALL TYPES////////////////////////////
					systemCatFile.seek(4); // LOCATION OF NUMBER OF FULL PAGES
					numFulld =  systemCatFile.read(); // NUMBER OF FULL PAGES
					
					HashSet<Integer> readPages2 = new HashSet<Integer>();
					
					int fieldNum3=0;
					for(int totalRead=0;totalRead<(numFulld*1600);totalRead = totalRead +32+1 +fieldNum3*16) {
						if(!readPages2.contains(totalRead / 1600+1)) {
							System.out.println("Reading page " + ((totalRead/1600)+1) +  "..");
							readPages2.add(totalRead / 1600+1);
						}
						//Thread.sleep(1000);
						if(totalRead<5) {
							totalRead= 5;  // WE HAVE 4 BYTES FOR WRITE LOCATION AND A BYTE FOR THE NUMBER OF FULL PAGES. SO START WRITING FROM 5th BYTE	
						}

							byte[] typename = new byte[32]; 

							systemCatFile.seek(totalRead); // GO TO totalRead th BYTE.
							systemCatFile.read(typename,0,32); // READ INTO typename

							String typename_ = new String(typename,StandardCharsets.UTF_8);  // CONVERSION INTO STRING 

							String typenameNew_=""; // THIS IS FOR REMOVING NULL CHARS AT THE BEGINNING OF typename_
							for(int i=0;i<typename_.length();i++) {
								if(typename_.charAt(i)!=0)typenameNew_ = typenameNew_+typename_.charAt(i); // WE EXCLUDE THE 0 VALUED CHARS FROM THE NEW STRING
							}

							
							fieldNum3=systemCatFile.read(); // ALSO READ FIELD NUMBER WE MAY NEED IT
							if(fieldNum3==-1)break;
							
							
							if(typenameNew_.length()!=0)System.out.println("Typename:'"+ typenameNew_ +"' with "+fieldNum3+" fields.");
							
							for(int i=1;i<=fieldNum3;i++) {
								byte[] fieldName = new byte[16];

								systemCatFile.read(fieldName,0,16);

								String fieldname_ = new String(fieldName,StandardCharsets.UTF_8);

								String fieldnameNew_="";
								for(int j=0;j<fieldname_.length();j++) {
									if(fieldname_.charAt(j)!=0)fieldnameNew_ = fieldnameNew_+fieldname_.charAt(j);
								}



								if(fieldnameNew_.length()!=0)System.out.print("Field" +i + " Name:" + fieldnameNew_ + " ");
							}
							if(typenameNew_.length()!=0)System.out.println("");
							
							
							 // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.

						


					



					}
					System.out.println("----------------------------------------");

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
		boolean contains = false;
		
		String typeNamed = user.next(); // THE TYPE THAT USER WANTS TO DELETE
		systemCatFile.seek(4); // LOCATION OF NUMBER OF FULL PAGES
		int numFulld =  systemCatFile.read(); // NUMBER OF FULL PAGES
		
		HashSet<Integer> readPages = new HashSet<Integer>();
		
		int fieldNum=0;
		for(int totalRead=0;totalRead<(numFulld*1600);totalRead = totalRead +32+1 +fieldNum*16) {
			if(!readPages.contains(totalRead / 1600+1)) {
				System.out.println("Reading page " + ((totalRead/1600)+1) +  "..");
				readPages.add(totalRead / 1600+1);
			}
			//Thread.sleep(1000);
			if(totalRead<5) {
				totalRead= 5;  // WE HAVE 4 BYTES FOR WRITE LOCATION AND A BYTE FOR THE NUMBER OF FULL PAGES. SO START WRITING FROM 5th BYTE	
			}

				byte[] typename = new byte[32]; 

				systemCatFile.seek(totalRead); // GO TO totalRead th BYTE.
				systemCatFile.read(typename,0,32); // READ INTO typename

				String typename_ = new String(typename,StandardCharsets.UTF_8);  // CONVERSION INTO STRING 

				String typenameNew_=""; // THIS IS FOR REMOVING NULL CHARS AT THE BEGINNING OF typename_
				for(int i=0;i<typename_.length();i++) {
					if(typename_.charAt(i)!=0)typenameNew_ = typenameNew_+typename_.charAt(i); // WE EXCLUDE THE 0 VALUED CHARS FROM THE NEW STRING
				}

				if(typenameNew_.equals(typeNamed)) {
					contains = true;
					fieldNum = systemCatFile.read();
					break;
				}		
				
				fieldNum = systemCatFile.read();
				if(fieldNum==-1)break;
				 // +1 is for the fieldNum byte. totalRead is previously read position. And we have also fieldNum*16 bytes to advance.

		}
		if(!contains) {
			return typeNamed;
		}
		return "";

	}
}