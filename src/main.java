import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;


public class main {

	static int space =1; // The disk have totally 65 files. ~10MB
	static RandomAccessFile systemCatFile;


	public static void main(String args[]) throws IOException, InterruptedException {

		Scanner user = new Scanner(System.in);//FOR USER INPUT	
		boolean exit = false;//WHEN TRUE THE LOOP STOPS
		////////////////////////////INITIALIZATION OF SYSTEM CATALOG//////////////////////
		systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");

		// Write "5" to the writeLocation byte.
		for(int i =0;i<160000;i++) {
			if(i%1600==3) {
				systemCatFile.write(5);
			}else systemCatFile.write(0);
		}
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

					String typeName =user.next();
					while((funcs.askForTypeInfo(systemCatFile,typeName)!=null)) { // Search for every catalog file. If exist ask again.
						System.out.println("There is a type with same name.Enter a name again:");
						typeName =user.next();
						funcs.askForTypeIndex =2;
					}

					System.out.println("Please enter number of fields that the type will have.(1-97)");
					int entered = user.nextInt();  // FIELD NUMBER ENTERED

					String[] fieldNames = new String[entered];	

					for(int i =0;i<entered;i++) { // FOR FIELD NUMBER TIMES, ASK A FIELD NAME FROM USER AND WRITE IT TO THE SYSTEM CATALOG

						System.out.println("Enter field name "+(i+1)+ " (in the same form of typeName. Moreover this can be in length 16 maximum.)");
						fieldNames[i] = user.next(); // ENTERED NAME
					}

					funcs.createType(systemCatFile,typeName,fieldNames);

					System.out.println("---------------------------------------------------------------");
					break;
				case "2": ////////////////////////////////DELETE A TYPE/////////////////////////// 

					System.out.println("Please enter the name of the type you want to delete....");
					String typeNamed = user.next(); // THE TYPE THAT USER WANTS TO DELETE
					funcs.deleteType(systemCatFile, typeNamed);
					System.out.println("Done with deletion.");
					System.out.println("---------------------------------------------------------------");
					break;

				case "3": //////////////////////////LIST ALL TYPES////////////////////////////
					funcs.printAllTypes(systemCatFile);
					System.out.println("----------------------------------------");
					//systemCatFile.close();
					break;

				case "4": /////////////////////////////////CREATING A RECORD///////////////////////////////
					System.out.println("Please enter type name:");
					String type =user.next();
					funcs.askForTypeIndex=2;
					type a = funcs.askForTypeInfo(systemCatFile, type);
					while(a==null) {
						System.out.println("There is not a type named "+ type + " enter again:");
						user.next();
					}

					int fN =a.fieldN ;


					File f= new File(type+".dat");
					if(!f.exists())f.createNewFile();

					Vector<File> fs = new Vector<File>();
					for(int i=2;i<=127;i++) {
						File fx= new File(type+i+".dat");
						if(fx.exists()) {
							fs.add(f);
						}
					}

					int reqSize = fN*4;
					int[] recordValues = new int[fN];

					boolean error = false; // Is primary key unique
					boolean written = false; // Is there enough space 
					for(int i=0;i<fN;i++) {
						System.out.println("Please enter field value "+i);
						if(i==0) {
							System.out.print("---Primary key, so this must be unique.");
						}else {
							int value = user.nextInt();
							recordValues[i] = value;
						}

					}

					for(int i=0;i<fs.size();i++) { // all files
						System.out.println("Opening file.."+(i+1)+"...");
						RandomAccessFile file = new RandomAccessFile(fs.get(i).getName(), "rw"); 

						if(funcs.isThereValue(file, recordValues[0], fN)) { // uniqueness test
							error = true;
							break;
						}

						for(int j =0;j<100;j++) { // traverse all pages
							System.out.println("Reading page "+j+"...");
							file.seek(1600*j);
							int writeLocation = file.readInt();


							if(1600-writeLocation>reqSize) { // If enough space write
								file.seek(writeLocation+1600*j);
								for(int x :recordValues) {
									file.writeInt(x);
								}
								file.seek(1600*j); // update writeLocation of that page.
								file.writeInt(writeLocation+reqSize);
								written = true;
								break;
							}

						}
						if(written) {
							break;
						}else if(space + 1 <= 65) { // No same key but not enough space ----> create new file and write 
							System.out.println("New file is created with name:"+ type + fs.size()+1+".dat");
							File nextFileF = new File(type + fs.size()+1+".dat");
							nextFileF.createNewFile();
							RandomAccessFile nextFile = new RandomAccessFile(type + fs.size()+1+".dat","rw");
							funcs.initFile(nextFile);
							nextFile.seek(5);
							for(int x :recordValues) {
								nextFile.writeInt(x);
							}
							nextFile.seek(0);
							nextFile.writeInt(5+reqSize);
							nextFile.close();
							space += 1;
						}else {
							System.out.println("Not enough space, delete some types or records.");
							break;
						}
						
				}
					


					if(!error) {
						System.out.println("Done.");
					}else {
						System.out.println("There is a primary key with the same value");
					}

					break;	


				case "5":///////////// Delete a record///////////////

					System.out.println("Please enter typename:");
					String tN = user.next();
					
					funcs.askForTypeIndex=2;
					RandomAccessFile catFile = new RandomAccessFile("syscatalog.dat","rw");
					type ret = funcs.askForTypeInfo(catFile, tN);
					
					if(ret ==null ) {
						System.out.println("Type not found.");
						break;
					}
					System.out.println("Enter the primary key:");
					int pK = user.nextInt();
					
					RandomAccessFile rF= new RandomAccessFile(tN+".dat", "rw");
					record x = funcs.askRecordInfo(rF,pK,tN,ret.fieldN);
					
					if(x ==null) {
						System.out.println("Record not found.");
						break;
					}
					RandomAccessFile recF = new RandomAccessFile(x.which.getName()+".dat","rw");
					int pageN = x.loc/1600 +1;
					recF.seek(x.loc);
					for(int i=1;i<=x.fieldN;i++) {
						recF.writeInt(0);
					}
					int size = x.fieldN *4;
					
					recF.seek(5);
					int numF = recF.read();
					
					
					recF.seek(1600*(pageN-1));
					int writeLoc = recF.readInt();
					recF.seek(1600*(pageN-1));
					recF.writeInt(writeLoc-size);
					boolean emptyFile = true;
					if(writeLoc-size==5) {// page is empty now so check every page that if they are empty also.
						
						for(int i=0;i<numF;i++) {

							recF.seek(i*1600);
							int writeL = recF.readInt();
							if(writeL!=5)emptyFile=false;
						}
						
						
						if(emptyFile) {
							
								x.which.delete();
								String refined = x.which.getName().substring(x.typeName.length()+1);
								refined = refined.substring(0, refined.length()-4);


								File possibleNextFile = new File(x.typeName+(Integer.parseInt(refined)+1)+".dat");
								if(possibleNextFile.exists()) {
									if(refined.equals("")) { // first file is deleted
										int counter =2;
										File nextFile = new File(x.typeName+"2.dat");
										File renameFileFirst = new File(x.typeName+".dat");
										if(nextFile.exists())nextFile.renameTo(renameFileFirst);

										nextFile = new File(x.typeName+"3.dat");

										while(nextFile.exists()) {
											File renameFile = new File(x.typeName+counter+".dat");
											if(nextFile.renameTo(renameFile)) {
												System.out.println("Error on renaming files, please rename them yourself accordingly(attempted to delete first file and rename the others, was renaming to "+ counter+ ")");
												System.exit(0);
											}
											nextFile = new File(x.typeName+(counter+2)+".dat");
											counter++;
										}		

									}else {// Intermediate


										int counter =Integer.parseInt(refined)+1;
										File nextFile = new File(x.typeName+counter+".dat");

										while(nextFile.exists()) {
											File renameFile = new File(x.typeName+(counter-1)+".dat");
											nextFile.renameTo(renameFile);
											nextFile = new File(x.typeName+(counter+1)+".dat");
											counter++;
										}

								}
								main.space--;
							}
						}
						
						
								
					}
					
					
					
					
					break;
				case "6": ///////////Search for a record /////////////
					
					System.out.println("Please enter typename:");
					String tN2 = user.next();
					
					funcs.askForTypeIndex=2;
					RandomAccessFile catFile2 = new RandomAccessFile("syscatalog.dat","rw");
					type ret2 = funcs.askForTypeInfo(catFile2, tN2);
					
					if(ret2 ==null ) {
						System.out.println("Type not found.");
						break;
					}
					System.out.println("Enter the primary key:");
					int pK2 = user.nextInt();
					
					RandomAccessFile rF2= new RandomAccessFile(tN2+".dat", "rw");
					record x2 = funcs.askRecordInfo(rF2,pK2,tN2,ret2.fieldN);
					
					rF2.seek(x2.loc);
					int[] values = new int[x2.fieldN];
					for(int i=1;i<=x2.fieldN;i++) values[i-1] = rF2.readInt();

					if(x2 ==null) {
						System.out.println("The record does not exist in files.");
					}else {
						System.out.println("Record found:");
						System.out.println("Values:");
						for(int i=1;i<=x2.fieldN;i++)System.out.print("Field"+i+":"+values[i-1]);
					}
					
					break;
				case "7": ///////////// List all records of a type//////
					
					System.out.println("Please enter typename:");
					String tN3 = user.next();
					
					funcs.askForTypeIndex=2;
					RandomAccessFile catFile3 = new RandomAccessFile("syscatalog.dat","rw");
					type ret3 = funcs.askForTypeInfo(catFile3, tN3);
					
					if(ret3 ==null ) {
						System.out.println("Type not found.");
						break;
					}
					RandomAccessFile rF3= new RandomAccessFile(tN3+".dat", "rw");
					rF3.seek(5); //TODO Let files update their first page 5 th byte instead of 1605 2605 etc.
					int numFull3 =rF3.read();
					for(int i =0;i<numFull3;i++) {
						rF3.seek(i*1600);
						int writeLoc3 = rF3.readInt();
						rF3.seek(i*1600+5);
						int counter =1;
						while(rF3.getFilePointer()<writeLoc3) {
							int[] valueArray = new int[ret3.fieldN];
							for(int p=1;p<=ret3.fieldN;p++)valueArray[p-1] = rF3.readInt();
							System.out.println("Record "+counter+"fields:");
							for(int p=1;p<=ret3.fieldN;p++)System.out.print("Field"+p+": "+valueArray[p-1]);
							counter++;
						}
					}
					
					
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

}