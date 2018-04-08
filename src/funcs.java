import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Retention;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

public class funcs {
	public static int askForTypeIndex =2;
	public static int createTypeIndex =2;
	public static int askRecordInfoIndex =2;
	public static void printAllTypes(RandomAccessFile systemCatFile) throws IOException {//print All Types

		systemCatFile.seek(4); // LOCATION OF NUMBER OF FULL PAGES
		int numFull =  systemCatFile.read(); // NUMBER OF FULL PAGES
		int fieldNum=0;

		for(int i=0;i<numFull;i++) {
			System.out.println("Reading page" +(i+1)+"...");
			systemCatFile.seek(i*1600);
			int writeLocation = systemCatFile.readInt();
			systemCatFile.seek(i*1600+5);

			while(true) {

				byte[] typename = new byte[32];
				systemCatFile.read(typename,0,32); // READ INTO typename

				String typename_ = new String(typename,StandardCharsets.UTF_8);  // CONVERSION INTO STRING 

				String typenameNew_=""; // THIS IS FOR REMOVING NULL CHARS AT THE BEGINNING OF typename_
				for(int j=0;j<typename_.length();j++) {
					if(typename_.charAt(j)!=0)typenameNew_ = typenameNew_+typename_.charAt(j); // WE EXCLUDE THE 0 VALUED CHARS FROM THE NEW STRING
				}
				fieldNum = systemCatFile.read();
				if(typenameNew_.length()!=0)System.out.println("Typename:'"+ typenameNew_ +"' with "+fieldNum+" fields.");

				for(int j=1;j<=fieldNum;j++) {
					byte[] fieldName = new byte[16];

					systemCatFile.read(fieldName,0,16);

					String fieldname_ = new String(fieldName,StandardCharsets.UTF_8);

					String fieldnameNew_="";
					for(int k=0;k<fieldname_.length();k++) {
						if(fieldname_.charAt(k)!=0)fieldnameNew_ = fieldnameNew_+fieldname_.charAt(k);
					}



					if(fieldnameNew_.length()!=0)System.out.print("Field" +j + " Name:" + fieldnameNew_ + " ");
				}

				if(typenameNew_.length()!=0)System.out.println("");

				if(systemCatFile.getFilePointer()>1568||systemCatFile.getFilePointer()==writeLocation) {
					break;
				}
			}



		}

	}
	public static void deleteType(RandomAccessFile systemCatFile,String typeName) throws IOException {
		askForTypeIndex=2;
		type b = askForTypeInfo(systemCatFile, typeName);
		RandomAccessFile bb = new RandomAccessFile(b.which, "rw");
		if(b==null) { // If we weren't able to find 
			System.out.println("Cannot find the type: "+ typeName);
			bb.close();
			return;
		}else { 
			System.out.println("Type found. Deleting....");
			// deletion
			bb.seek(b.loc);
			for(int i=1;i<=32;i++) {
				bb.write(0); // Write 32 0's onto it.
			}

			bb.write(0); // Delete field number byte

			for(int i=1;i<=b.fieldN;i++) { // Then delete all the fields.
				for(int j=1;j<=16;j++) {
					bb.write(0);
				}
			}
			int afterDeletePos = (int) bb.getFilePointer();
			byte[] rest = new byte[1600-afterDeletePos];
			bb.read(rest);
			bb.seek(afterDeletePos);
			for(int i=1;i<=1600-afterDeletePos;i++){
				bb.write(0);
			}
			bb.seek(b.loc);
			bb.write(rest);



			bb.seek((b.loc/1600)*1600);
			int writeLocation = bb.readInt();//page header writelocation is read
			bb.seek((b.loc/1600)*1600);
			bb.writeInt(writeLocation-(b.fieldN*16+32+1));


			if(writeLocation-(b.fieldN*16+32+1)==5) { //page is empty now, we may delete the file then.
				boolean fileIsEmptyNow = true;
				for(int i=0;i<100;i++) {
					bb.seek(i*1600);
					if(bb.readInt()!=5)fileIsEmptyNow= false;
				}
				bb.close();
				if(fileIsEmptyNow) {
					b.which.delete();
					String refined = b.which.getName().substring(10);
					refined = refined.substring(0, refined.length()-4);


					File possibleNextFile = new File("syscatalog"+(Integer.parseInt(refined)+1)+".dat");
					if(possibleNextFile.exists()) {
						if(refined.equals("")) { // first file is deleted
							int counter =2;
							File nextFile = new File("syscatalog2.dat");
							File renameFileFirst = new File("syscatalog.dat");
							if(nextFile.exists())nextFile.renameTo(renameFileFirst);

							nextFile = new File("syscatalog3.dat");

							while(nextFile.exists()) {
								File renameFile = new File("syscatalog"+counter+".dat");
								if(nextFile.renameTo(renameFile)) {
									System.out.println("Error on renaming files, please rename them yourself accordingly(attempted to delete first file and rename the others, was renaming to "+ counter+ ")");
									System.exit(0);
								}
								nextFile = new File("syscatalog"+(counter+2)+".dat");
								counter++;
							}		

						}else {// Intermediate


							int counter =Integer.parseInt(refined)+1;
							File nextFile = new File("syscatalog"+counter+".dat");

							while(nextFile.exists()) {
								File renameFile = new File("syscatalog"+(counter-1)+".dat");
								nextFile.renameTo(renameFile);
								nextFile = new File("syscatalog"+(counter+1)+".dat");
								counter++;
							}
						}

					}
					main.space--;
				}
			}

		}
	}
	public static boolean createType(RandomAccessFile systemCatFile,String typeName,String[] fieldNames) throws IOException {

		int reqSize = 33+fieldNames.length*16;

		for(int k=0;k<100;k++) { 
			systemCatFile.seek(1600*k);
			int writeLocation = systemCatFile.readInt();

			if(1600-writeLocation>=reqSize) {
				systemCatFile.seek(1600*k+writeLocation);
				for(int p=1;p<=32-typeName.getBytes().length;p++) {//FILL THE REST WITH ZEROS
					systemCatFile.write(0);
				}
				systemCatFile.write(typeName.getBytes());
				systemCatFile.write(fieldNames.length);
				for(int u=0;u<fieldNames.length;u++) {
					for(int j=1;j<=16-fieldNames[u].getBytes().length;j++) { // FILL THE REST WITH ZEROS
						systemCatFile.write(0);
					}
					systemCatFile.write(fieldNames[u].getBytes());
				}
				systemCatFile.seek(1600*k);
				systemCatFile.writeInt(writeLocation+reqSize);
				System.out.println("Created type: " + typeName + " with " + fieldNames.length + " field(s).");
				return true;
			}

		}

		File next = new File("syscatalog"+createTypeIndex+".dat");
		RandomAccessFile x = new RandomAccessFile("syscatalog"+createTypeIndex+".dat","rw");

		if(next.isFile()) {
			askForTypeIndex++;
			boolean ret = createType(x, typeName, fieldNames);
			return ret;
		}else if(main.space +1 <=65) { // create new catalog file.
			next.createNewFile();

			initFile(x); //Initialize
			x.seek(5);
			for(int m=1;m<=32-typeName.length();m++) {
				x.write(0);
			}

			x.write(typeName.getBytes());
			x.write(fieldNames.length);
			for(int yy=0;yy<fieldNames.length;yy++) {
				for(int xx=1;xx<16-fieldNames[yy].length();xx++) {
					x.write(0);
				}
				x.write(fieldNames[yy].getBytes());
			}
			x.close();
			main.space++;
			return true;
		}else {
			System.out.println("Not enough space to create a new catalog file. You may try to write");
			System.out.print(" shorter type definitions or delete some types or delete some records to");
			System.out.print(" to cause a record file to get deleted.");
		}
		x.close();
		return false;

	}
	public static type askForTypeInfo(RandomAccessFile systemCatFile,String typeName) throws IOException { 
		systemCatFile.seek(4); // LOCATION OF NUMBER OF FULL PAGES
		int numFull =  systemCatFile.read(); // NUMBER OF FULL PAGES
		int fieldNum=0;
		File returnedFile;
		if(askForTypeIndex==2) {// ?
			returnedFile = new File("syscatalog.dat");
		}else {
			returnedFile = new File("syscatalog"+ (askForTypeIndex-1)+ ".dat");
		}
		for(int i=0;i<numFull;i++) {
			System.out.println("Reading page" +(i+1)+"...");
			systemCatFile.seek(i*1600);
			int writeLocation = systemCatFile.readInt();
			systemCatFile.seek(i*1600+5);

			while(true) {

				byte[] typename = new byte[32];
				systemCatFile.read(typename,0,32); // READ INTO typename

				String typename_ = new String(typename,StandardCharsets.UTF_8);  // CONVERSION INTO STRING 

				String typenameNew_=""; // THIS IS FOR REMOVING NULL CHARS AT THE BEGINNING OF typename_
				for(int j=0;j<typename_.length();j++) {
					if(typename_.charAt(j)!=0)typenameNew_ = typenameNew_+typename_.charAt(j); // WE EXCLUDE THE 0 VALUED CHARS FROM THE NEW STRING
				}
				fieldNum = systemCatFile.read();
				if(typenameNew_.equals(typeName))return new type(typeName,fieldNum,(int)systemCatFile.getFilePointer()-33,returnedFile);

				for(int j=1;j<=fieldNum;j++) {
					byte[] fieldName = new byte[16];

					systemCatFile.read(fieldName,0,16);

					String fieldname_ = new String(fieldName,StandardCharsets.UTF_8);

					String fieldnameNew_="";
					for(int k=0;k<fieldname_.length();k++) {
						if(fieldname_.charAt(k)!=0)fieldnameNew_ = fieldnameNew_+fieldname_.charAt(k);
					}

				}

				if(systemCatFile.getFilePointer()>1568||systemCatFile.getFilePointer()==writeLocation) {
					break;
				}
			}



		}

		File next = new File("syscatalog"+askForTypeIndex+".dat");
		if(next.isFile()) {
			RandomAccessFile x = new RandomAccessFile("syscatalog"+askForTypeIndex+".dat","rw");
			askForTypeIndex++;
			type ret = askForTypeInfo(x, typeName);
			if(ret==null) {
				return null;
			}else {
				return ret;
			}
		}

		return null;



	}
	public static boolean isThereValue(RandomAccessFile file,int value,int fieldNum) throws IOException {

		file.seek(4); // LOCATION OF NUMBER OF FULL PAGES
		int numFull =  file.read(); // NUMBER OF FULL PAGES

		for(int i=0;i<numFull;i++) {
			System.out.println("Reading page" +(i+1)+"...");
			file.seek(i*1600);
			int writeLocation = file.readInt();
			file.seek(i*1600+5);
			while(file.getFilePointer()<writeLocation) {
				if(file.readInt()==value) return true;
				file.seek(file.getFilePointer()+4*(fieldNum-1));
			}
		}


		return false;
	}
	public static void initFile(RandomAccessFile file) throws IOException {
		for(int i =0;i<160000;i++) {
			if(i%1600==3) {
				file.write(5);
			}else file.write(0);
		}
		file.seek(4); 
		file.write(1);
	}
	public static record askRecordInfo(RandomAccessFile file, int primaryKey, String typeName,int fn) throws IOException {

		file.seek(4); // LOCATION OF NUMBER OF FULL PAGES
		int numFull =  file.read(); // NUMBER OF FULL PAGES
		
		
		
		for(int i=0;i<numFull;i++) {
			System.out.println("Reading page" +(i+1)+"...");
			file.seek(i*1600);
			int writeLocation = file.readInt();
			
				for(int j=1;file.getFilePointer()<writeLocation;j+=fn*4) {
					file.seek(i*1600+5+j*fn*4);
					int val = file.readInt();
					File x;
					if(i==0) {
						 x = new File(typeName+".dat");
					}else {
						 x = new File(typeName+i+".dat");
					}
					if(val==primaryKey)return new record(x,typeName,i*1600+5+j*fn*4,fn,primaryKey);
				}
				
		}
		
		
			return null;
		
		
		
		
	}
	
}